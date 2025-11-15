# Roadmap de Seguridad: JWT + Google + Roles

## 1. Decisiones de diseño

- **Formato JWT**
  - **Algoritmo**: HS256 o RS256 (según si quieres clave simétrica o par de claves).
  - **Claims mínimos**:
    - `sub`: ID interno del usuario (`Long id` de `Usuario`).
    - `email`: email del usuario.
    - `rol`: rol lógico del dominio (`SUPERADMIN`, `EMPRESA`, `EMPLEADO`, `CLIENTE`, etc.).
    - `empresaId` (opcional): ID de empresa asociada, si aplica.
  - **Expiración**:
    - `exp` = ahora + **1 hora**.

- **Almacenamiento del token en el front**
  - **Objetivo final**: usar **cookies HttpOnly** (más seguro) para access/refresh tokens.
    - Pros: no accesible por JS → más protección frente a XSS.
    - Contras: hay que gestionar bien CSRF (SameSite, tokens anti-CSRF, etc.).
  - **Estrategia práctica**:
    - Diseño de API preparado para ambas opciones:
      - Fases iniciales: se puede usar `Authorization: Bearer` con token (p.ej. en `localStorage/sessionStorage`) para simplificar.
      - Fase avanzada: migrar a **cookies HttpOnly** (access + refresh), con `SameSite` y protección CSRF.

- **Modelo de usuarios**
  - **Clientes**:
    - **Registro**: siempre vía **Google** (no hay registro clásico con contraseña propia).
    - **Login**: principalmente con Google.
  - **Usuarios internos / backoffice / SUPERADMIN**:
    - Mantener **login clásico email+password** (cuentas locales) para roles internos.
    - Estos usuarios NO se registran por endpoints públicos; se crean/asignan desde backoffice o scripts internos.
  - **Roles privilegiados (`SUPERADMIN`, `EMPRESA`, `EMPLEADO`)**:
    - Nunca asignados desde endpoints públicos.
    - Solo se asignan/cambian en **casos de uso backoffice** protegidos por rol.

---

## 2. Roadmap por fases

### Fase 0 – Hardening rápido del esquema actual

**Objetivo**: cerrar vulnerabilidades críticas sin cambiar aún a JWT/Google.

- **0.1. Registro siempre como CLIENTE**
  - Modificar `RegisterRequest` para **eliminar el campo `rol`**.
  - Actualizar `AuthService.register(...)` para:
    - Ignorar cualquier rol entrante.
    - Asignar siempre `rol = "CLIENTE"` (o equivalente de dominio) para registros públicos.

- **0.2. Revisar puntos de asignación de rol**
  - Buscar en servicios/adapters dónde se crea o actualiza el `rol` de `Usuario`.
  - Asegurar que:
    - Solo endpoints backoffice (protegidos por `hasRole("SUPERADMIN")` o similar) pueden cambiar roles.
    - No hay ningún endpoint público que permita fijar/editar roles arbitrarios.

- **0.3. Documentación**
  - Actualizar `DEVELOPMENT.md`:
    - Explicar que:
      - Registro público asigna siempre `CLIENTE`.
      - Roles privilegiados solo se tocan desde backoffice.

---

### Fase 1 – Autenticación stateless con JWT (sin Google aún)

**Objetivo**: reemplazar Basic Auth por JWT stateless, manteniendo login email/password para usuarios internos y, de momento, también para clientes.

- **1.1. Infraestructura JWT**
  - Crear un componente `JwtTokenProvider` (en `infrastructure.security` o `infrastructure.config`) que:
    - Genere JWT con claims:
      - `sub` = id de `Usuario`.
      - `email`, `rol`, `empresaId?`.
      - `exp` = ahora + **1 hora**.
    - Valide tokens:
      - Firma.
      - Expiración (`exp`).
      - Opcional: issuer y otros claims.

- **1.2. Filtro de autenticación JWT**
  - Crear `JwtAuthenticationFilter`:
    - Extrae token de:
      - Cabecera `Authorization: Bearer <token>`, y
      - (en fases posteriores) cookie `accessToken` si se usa.
    - Valida token con `JwtTokenProvider`.
    - Crea un `Authentication` (`UsernamePasswordAuthenticationToken` o custom) con:
      - Principal = email o id.
      - Authorities derivadas de `rol`.
    - Coloca el `Authentication` en `SecurityContextHolder`.

- **1.3. Adaptar `SecurityConfig` a modelo stateless**
  - En `protectedFilterChain`:
    - Deshabilitar sesiones de servidor (SessionCreationPolicy.STATELESS).
    - Quitar `httpBasic(...)`.
    - Añadir `JwtAuthenticationFilter` antes de `UsernamePasswordAuthenticationFilter`.
    - Mantener reglas de autorización:
      - `/api/superadmin/**` → `hasRole("SUPERADMIN")`.
      - `/api/backoffice/**` → `.authenticated()`, etc.
  - Mantener `publicFilterChain` para `/api/public/**`, `/api/auth/**`, etc.

- **1.4. Adaptar `AuthController.login`**
  - `POST /api/auth/login`:
    - Usa `AuthService.login` para validar credenciales con `AuthenticationManager`.
    - Genera un **JWT de 1 hora** con `JwtTokenProvider`.
    - Devuelve `LoginResponse` con:
      - Datos básicos del usuario.
      - `accessToken` (JWT).
    - En fases posteriores, se podrá:
      - Incluir `refreshToken`.
      - O setear cookies HttpOnly en la respuesta.

- **1.5. Front-end (mínimo viable)**
  - Consumir el nuevo `LoginResponse`:
    - Guardar `accessToken` (inicialmente puede ser en `sessionStorage`/`localStorage`).
  - Añadir en cada request protegida:
    - `Authorization: Bearer <accessToken>`.

---

### Fase 2 – Integrar Google como origen de identidad para clientes

**Objetivo**: registro de clientes **solo con Google**; login principal de clientes también con Google.

- **2.1. Configuración de Google**
  - Crear proyecto en Google Cloud Console.
  - Configurar OAuth/Identity:
    - `GOOGLE_CLIENT_ID`.
    - Origins permitidos (por ejemplo: `http://localhost:5173` en desarrollo).
  - Guardar `GOOGLE_CLIENT_ID` (y si aplica `GOOGLE_CLIENT_SECRET`) en config:
    - Variables de entorno.
    - `application.yml`/`application.properties`.

- **2.2. Front-end – Login con Google**
  - Integrar Google Identity Services:
    - Botón "Continuar con Google".
    - Obtener **ID Token** tras login.
  - Mandar al backend:  
    `POST /api/auth/google` con `{"idToken": "<id_token_de_google>"}`.

- **2.3. Backend – Verificación de ID Token**
  - Crear servicio `GoogleTokenVerifierService`:
    - Valida el ID Token usando las claves públicas de Google (JWKS o librería).
    - Comprueba:
      - `aud` == `GOOGLE_CLIENT_ID`.
      - `iss` == `https://accounts.google.com` o similar.
      - No expirado.
    - Devuelve info verificada:
      - `email`.
      - `name`.
      - `picture`, etc.

- **2.4. Backend – Endpoint `/api/auth/google`**
  - En `AuthController`:
    - Añadir `POST /api/auth/google`.
  - En la capa de aplicación (`AuthService` o un servicio específico):
    - Verificar ID Token con `GoogleTokenVerifierService`.
    - Buscar `Usuario` por `email`.
      - Si existe: usar ese usuario.
      - Si no existe: crear nuevo `Usuario` con:
        - `rol = CLIENTE`.
        - Datos básicos de nombre/email.
    - Generar **JWT de 1 hora** con `JwtTokenProvider`.
    - Devolver `LoginResponse` con:
      - Usuario (id, email, rol, etc.).
      - `accessToken` (y en el futuro `refreshToken` o cookies).

- **2.5. Distinción clientes vs internos**
  - Documentar:
    - Clientes: usan `/api/auth/google`.
    - Internos/SUPERADMIN/backoffice: usan `/api/auth/login` (email+password) o gestión interna.

---

### Fase 3 – Roles privilegiados solo desde backoffice

**Objetivo**: blindar la asignación y gestión de roles.

- **3.1. Centralizar la lógica de roles**
  - Crear un caso de uso en `application/usecase`:
    - `AssignUserRoleUseCase` o similar.
  - Implementar servicio en `application/service` que:
    - Valide que el rol destino es válido.
    - Aplique reglas de negocio (por ejemplo, no bajar a un SUPERADMIN si es el único).

- **3.2. Adapter de backoffice**
  - Endpoint tipo:
    - `PATCH /api/backoffice/usuarios/{id}/rol`.
  - Proteger con:
    - `@PreAuthorize("hasRole('SUPERADMIN')")` o reglas equivalentes en `SecurityConfig`.
  - Asegurar que no quedan otros puntos donde se pueda cambiar el rol directamente sin pasar por este caso de uso.

---

### Fase 4 – Cookies HttpOnly + Refresh Tokens (modelo más seguro)

**Objetivo**: mejorar la protección frente a XSS y ofrecer una mejor experiencia de sesión.

- **4.1. Modelo de tokens**
  - `accessToken` (JWT de 1h) en cookie HttpOnly `accessToken`.
  - `refreshToken` (vida más larga, p.ej. 7-30 días) en cookie HttpOnly `refreshToken`.

- **4.2. Cambios en el backend**
  - Endpoints de login (`/api/auth/login`, `/api/auth/google`):
    - En lugar (o además) de devolver el token en el body, setear cookies HttpOnly.
  - Endpoint `/api/auth/refresh`:
    - Lee `refreshToken` desde cookie.
    - Valida y genera nuevo `accessToken` (y opcionalmente nuevo `refreshToken`).

- **4.3. Seguridad adicional**
  - Configurar cookies con:
    - `HttpOnly` = true.
    - `Secure` = true (en producción con HTTPS).
    - `SameSite` = `Lax` o `Strict`, según el caso.
  - Implementar protección CSRF si se usan cookies en peticiones que cambian estado.

---

### Fase 5 – Testing y observabilidad

**Objetivo**: asegurar que la nueva seguridad funciona y es mantenible.

- **5.1. Tests de integración**
  - Casos para:
    - Acceso a endpoints públicos vs protegidos sin token.
    - Acceso con JWT válido/inválido/expirado.
    - Acceso a `/api/backoffice/**` con distintos roles.
    - Registro/login con Google (mockeando el verificador de token de Google).

- **5.2. Logging y auditoría**
  - Loguear eventos importantes:
    - Login correcto y fallido.
    - Cambios de rol.
    - Operaciones sensibles de backoffice.

- **5.3. Monitorización**
  - Añadir métricas (si se usa Actuator/Prometheus) sobre:
    - Fallos de autenticación.
    - Uso de endpoints de login.
