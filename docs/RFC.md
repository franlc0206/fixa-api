# 🧾 RFC – Alineamiento FE / BE: Home pública tipo "PedidosYa" + BackOffice estilo Setmore

Versión: 1.0  
Autor: Francisco López  
Fecha: 2025-11-04

---

## 1. Objetivo del RFC

Alinear al equipo Frontend (React + Vite) y Backend (Java Spring Boot) sobre la implementación y correcciones necesarias para que:

- La **Home** muestre todas las empresas y los servicios que ofrecen (estilo marketplace tipo PedidosYa).
- La **ficha pública de empresa** muestre servicios, empleados y permita reservar turnos (lógica tipo Setmore por empresa).
- El **BackOffice** (solo para usuarios con empresa asociada) permita ABM de empleados, configuración de disponibilidad, calendario y gestión de turnos (estilo Setmore).
- El **SuperAdmin** gestione empresas, relaciones usuario↔empresa y categorías.

---

## 2. Alcance

### Frontend
- Ajustes en Home, EmpresaDetalle, Reserva, BackOffice y rutas de login/roles.

### Backend
- Endpoints faltantes/ajustes, validaciones de seguridad/roles y lógica de negocio (turnos, validación telefónica, asociación usuario→empresa).

### Integración
- Definición de contratos API (request/response) y criterios de aceptación.

---

## 3. Roles y reglas básicas

### Roles

- **ANONYMOUS** (visitante) — puede ver Home/empresa y solicitar turnos públicos.
- **CLIENTE** — cuenta registrada, ver historial.
- **EMPRESA_ADMIN** — administrador de una empresa (acceso a BackOffice).
- **EMPLEADO** — empleado con permisos limitados (ver su agenda).
- **SUPERADMIN** — administración global.

### Reglas clave

- **BackOffice** solo visible si usuario está vinculado a al menos 1 empresa. Si no, mostrar mensaje: *"No estás asociado a ninguna empresa"*.
- **Home**: muestra empresas y sus servicios que tengan `visibilidad_publica = true`.
- **EmpresaDetalle**: muestra servicios activos y empleados que `trabaja_publicamente = true`.
- **Reserva**: puede hacerse como anónimo con nombre+teléfono (opcional DNI/email). Si `empresa.requiere_validacion_telefono == true` → enviar código y exigir validación antes de confirmar.
- **Turno estado**: `pendiente_aprobacion`, `confirmado`, `cancelado`, `realizado`, `no_asistio`.
- Si empresa usa aprobación manual, la reserva queda `pendiente_aprobacion` hasta que un EMPRESA_ADMIN la acepte en BackOffice.

---

## 4. Flujo funcional (resumen)

### A. Home (usuario)

1. Usuario entra a `/` → ve carrusel de categorías + lista/grid de empresas filtrable.
2. Click en empresa → `/empresa/:slug` → ver servicios y empleados.
3. Selecciona servicio → ver horarios disponibles por empleado (si aplica).
4. Click reservar → formulario (nombre, teléfono, email opcional, DNI opcional).
5. Si requiere validación telefónica → se envía código y se valida (`POST /api/verificaciones`).
6. Según la configuración: turno queda `confirmado` o `pendiente_aprobacion`.

### B. BackOffice (empresa)

1. Usuario con `EMPRESA_ADMIN` y relación activa entra a `/backoffice`.
2. Panel con: Dashboard, Empleados, Servicios, Calendario, Configuración.
3. **Empleados**: ABM + definir horarios (`disponibilidad_empleado`) y asignar servicios.
4. **Servicios**: ABM (duración_total_min, patron_bloques o patrones simples).
5. **Calendario**: mostrar todos los turnos por empleado; permitir aceptar/rechazar solicitudes pendientes; crear turnos manuales.

### C. SuperAdmin

1. Admin entra a `/admin` → ABM empresas, ABM usuarios↔empresa, ABM categorías.
2. Puede crear empresas manualmente para onboarding.

---

## 5. Modelo / Tablas relevantes (resumen)

### 🧑‍💻 usuario
- id, nombre, email, telefono, rol, activo

### 🏢 empresa
- id, nombre, slug, fk_usuario_admin, categoria, visibilidad_publica, requiere_validacion_telefono, permite_reservas_sin_usuario, ...

### 🧍‍♂️ empleado
- id, fk_empresa, fk_usuario?, nombre, apellido, trabaja_publicamente, estado

### 💇‍♂️ servicio
- id, fk_empresa, nombre, duracion_minutos, patron_bloques JSON opcional, requiere_seña, activo

### 🕓 disponibilidad_empleado
- id, fk_empleado, dia_semana, hora_inicio, hora_fin, intervalo_turnos_min

### 📅 turno
- id, fk_servicio, fk_empleado, fk_empresa, fk_cliente NULLABLE, cliente_nombre, cliente_telefono, fecha_hora_inicio, fecha_hora_fin, estado, telefono_validado

### 📱 verificacion_telefono
- id, telefono, codigo, fecha_envio, fecha_expiracion, validado, fk_turno NULLABLE

### 🏷️ categoria
- id, nombre, slug

---

## 6. Contrato API sugerido (endpoints mínimos y payloads)

**Nota**: Los endpoints deben devolver errores estándar (400/401/403/404/500) y un body con `code`, `message`, `details` cuando aplique.

### Public (no auth)

#### `GET /api/empresas`
- Query params: `q`, `categoria`, `page`, `size`, `lat`, `lng`
- Response: listado empresas (id, nombre, slug, categoria, logo, excerptServicios)

#### `GET /api/empresas/{slug}`
- Response: empresa detallada + servicios[] + empleados_publicos[] + horarios_base

#### `GET /api/empresas/{slug}/valoraciones`
- Query params: `soloConResena` (default=false), `limit` (default=20)
- Response: valoraciones activas ordenadas por fecha (puntuación + reseña opcional)

#### `GET /api/empresas/{slug}/valoraciones/resumen`
- Response: `{ promedio, totalValoraciones, totalConResena, totalSinResena }`
- Usa escala 0-5 con un decimal para UI estilo marketplace

#### `GET /api/empresas/{slug}/servicios`
- Response: servicios con duración, precio, visibilidad

#### `POST /api/turnos` — crear solicitud de turno pública
- Body: `{ fk_servicio, fk_empleado?, fecha_hora_inicio, cliente_nombre, cliente_telefono, cliente_dni?, cliente_email? }`
- Response: `{ turnoId, estado }`
- Lógica: si `empresa.requiere_validacion_telefono` → crear `verificacion_telefono` y retornar `requires_validation: true` + `verificacionId`

#### `POST /api/verificaciones` — crear/reenviar código
- Body: `{ telefono, fk_turno? }`
- Response: `{ verificationId }`

#### `POST /api/verificaciones/{id}/confirm` — confirmar código
- Body: `{ codigo }`
- Response: `{ success: true, turnoEstado: 'confirmado'|'pendiente_aprobacion' }`

### Auth (JWT / OAuth)

- `POST /api/auth/login`
- `POST /api/auth/register`
- `GET /api/auth/me`

### BackOffice (auth + empresa relation check)

**All under auth; middleware**: verificar rol + empresa asociada

- `GET /api/backoffice/empresa` → datos empresa vinculada al usuario
- `GET /api/backoffice/empleados`
- `POST /api/backoffice/empleados`
- `PUT /api/backoffice/empleados/{id}`
- `DELETE /api/backoffice/empleados/{id}`
- `GET /api/backoffice/servicios`
- `POST /api/backoffice/servicios`
- `PUT /api/backoffice/servicios/{id}`
- `DELETE /api/backoffice/servicios/{id}`
- `GET /api/backoffice/turnos` → filtros: `fechaDesde`, `fechaHasta`, `empleadoId`, `estado`
- `PATCH /api/backoffice/turnos/{id}/aceptar`
- `PATCH /api/backoffice/turnos/{id}/rechazar`
- `POST /api/backoffice/turnos` → crear turno manual (por empresa)
- `GET /api/backoffice/calendario` → Response: estructura lista para calendar component: eventos con `empleadoId`, `estado`, `title`, `start`, `end`, `meta`
- `PUT /api/backoffice/configuracion` → campos: `permite_reservas_sin_usuario`, `requiere_validacion_telefono`, `visibilidad_publica`, `mensaje_validacion_personalizado`

### SuperAdmin (auth + role check)

- `GET /api/admin/empresas`
- `POST /api/admin/empresas`
- `PUT /api/admin/empresas/{id}`
- `GET /api/admin/usuarios`
- `POST /api/admin/relaciones` → `{ userId, empresaId, role }`
- `POST /api/admin/categorias` etc.

---

## 7. Seguridad – Roadmap (OAuth2.0 + MFA)

- **Fase 1 (MVP actual)**
  - HTTP Basic en backoffice (BCrypt en backend)
  - Roles: SUPERADMIN, EMPRESA, EMPLEADO, CLIENTE
  - CORS: permitido `http://localhost:5173`
  - Endpoints públicos: `/api/public/**`, `/health`, `/api/auth/*`
- **Fase 2**
  - OAuth2 (Google, Facebook)
  - Alta automática del usuario cliente tras login social
- **Fase 3**
  - MFA por SMS/WhatsApp
  - Auditoría de logins
  - Rate limit y recaptcha
  - Revocación de tokens

---

## 8. Validaciones / Reglas detalladas a implementar (BE + FE)

### 8.1 BackOffice access

- **BE**: middleware que, en cada endpoint `/api/backoffice/*`, valida que `auth.user` tenga al menos 1 empresa activa asociada (o la empresaId que se pasa). Si no, responder 403 con mensaje claro.
- **FE**: al cargar `/backoffice`, consumir `GET /api/backoffice/empresa` y si 403 → mostrar pantalla informativa *"No estás asociado a ninguna empresa"*. No mostrar menús.

### 8.2 Creación de turno y bloqueo de slots

- **BE**: al crear turno, validar conflictos con `disponibilidad_empleado` y turnos existentes (lock transaccional). Retornar 409 si conflicto.
- **FE**: al mostrar horarios, pedir al BE disponibilidad por franjas considerando `patron_bloques` del servicio.

### 8.3 Validación telefónica

- **BE**: crear `verificacion_telefono` y enviar código (Twilio). Guardar expires.
- **FE**: flujo UI para ingresar código; bloquear reenvíos por X segundos.

### 8.4 Patrones de servicio (duración/espacios)

- Modelar `patron_bloques` o reglas simples: `duracion_total_min`, `intervalo_turnos_min`, `requiere_espacio_libre` para que FE pueda mostrar slots correctos.

### 8.5 Seguridad adicional

- **Autenticación**: JWT / OAuth2.0.
- **CSRF**: no aplicable para API stateless; CORS restringido por dominios front.
- **Rate limit**: en endpoints públicos (`POST /api/turnos`, `/api/verificaciones`) para evitar abuso (Bucket4J o similar).
- **Contraseñas**: hashear con BCrypt.

---

## 9. Tareas concretas por equipo

### Backend (prioridad alta)

1. Implementar middleware de validación de cuenta→empresa para rutas `/api/backoffice/*`.
2. Revisar/crear endpoints del listado anterior (especialmente `/api/empresas/{slug}`, `/api/turnos`, `/api/backoffice/turnos`, `/api/verificaciones`).
3. Implementar bloqueo/validación de conflictos al crear turnos (transacción + checks).
4. Implementar lógica de verificación telefónica (persistir `verificacion_telefono` y envío de SMS).
5. Exponer `GET /api/backoffice/calendario` con formato listo para librería calendar (`id`, `title`, `start`, `end`, `color`, `estado`, `empleadoId`).
6. Documentación OpenAPI.

### Frontend (prioridad alta)

1. **Home**: grid/carrusel empresas y filtro por categoría/ubicación. Consumir `GET /api/empresas`.
2. **EmpresaDetalle**: consumir `GET /api/empresas/{slug}`, mostrar servicios, empleados y calendario de slots.
3. **Reserva Flow**: crear UI de reserva pública con ruta clara a validación de teléfono (si aplica).
4. **BackOffice**: ruta `/backoffice` que primero consulta `GET /api/backoffice/empresa`. Si 403 → mostrar mensaje. Si OK → mostrar sidebar + módulos (Empleados, Servicios, Turnos, Calendario, Config).
5. **Calendario FE**: usar FullCalendar / React Big Calendar y consumir `/api/backoffice/calendario`.
6. **Manejador de errores**: show modals/toasts para 403/409/422.
7. Añadir loading states y manejo de concurrencia para reservas (deshabilitar botón mientras se crea).

---

## 10. Entregables

- **BE**: Swagger actualizado + pruebas de integración para endpoints críticos (turnos/validaciones).
- **FE**: Prototype funcional (Vite) con Home, EmpresaDetalle, Reserva, BackOffice con calendario y ABM de empleados/servicios.

---

## 11. Criterios de aceptación (mínimos)

1. **Home** muestra empresas públicas y al click en empresa abre `/empresa/:slug` con los servicios listados.
2. **Reserva pública**: Usuario puede seleccionar servicio y horario plausible; si la empresa exige validación, se debe enviar/confirmar código antes de confirmar.
3. **BackOffice**: Al entrar, el BE obliga a la validación de relación empresa↔usuario; si no vinculados → FE muestra pantalla informativa.
4. **Calendario FE** muestra eventos con estados correctos y filtros por empleado.
5. **Conflictos**: Si dos usuarios intentan reservar el mismo slot, el BE debe impedir la doble reserva y devolver 409; FE muestra mensaje claro.
6. **SuperAdmin ABM**: poder crear empresa y asignar usuario a empresa.

---

## 12. Tests recomendados

- **BE**: integración Testcontainers para DB que pruebe: creación de turno, bloqueo de slot, verificación telefónica, permiso backoffice.
- **FE**: test e2e (Playwright/Cypress) para flujo de reserva pública + validación telefónica + acceso backoffice con y sin empresa asociada.

---

## 13. Roadmap de correcciones (prioridad / sugerido)

### Sprint 1 (1–2 semanas)
- **BE**: middleware backoffice check + endpoints `/api/empresas/{slug}` y `/api/turnos`.
- **FE**: Home + EmpresaDetalle + Reserva básica (sin validación telefónica).
- Documentación OpenAPI.

### Sprint 2 (1–2 semanas)
- **BE**: verificación telefónica + bloqueo de slots + `/api/backoffice/calendario`.
- **FE**: flujo de validación telefónica; BackOffice: vista básica calendario y mensaje "no asociado".

### Sprint 3 (1–2 semanas)
- **BE**: ABM empleados/servicios + SuperAdmin endpoints.
- **FE**: ABM empleados/servicios en BackOffice; SuperAdmin panel básico.

---

## 14. Observaciones y recomendaciones

- Mantener contratos API estables; versionar si cambian.
- Mantener una tabla de logs/auditoria para acciones críticas (crear turno, aceptar/rechazar).
- Por seguridad y UX, limitar intentos de envío de códigos y aplicar reCAPTCHA para evitar abuso.
- Documentar ejemplos de payloads en Swagger para que el FE los tenga claros.

---

## 15. Arquitectura técnica

- **Frontend** (público y backoffice): React + Vite + TypeScript
- **Backend**: Java Spring Boot
- **Base de datos**: MySQL
- **Mensajería**: Twilio / WhatsApp Cloud API
- **Infraestructura**: AWS / Railway / DonWeb / Render
- **Arquitectura**: Hexagonal (Domain / Application / Infrastructure)

### Estructura de paquetes:
```
src/main/java/com/fixa/turnero/
  domain/
    model/
    repository/
  application/
    usecase/
  infrastructure/
    in/
      web/
      messaging/
    out/
      persistence/
      sms/
      email/
    config/
```

---

## 16. Referencias

- Frontend Roadmap: `docs/FRONTEND_ROADMAP.md`
- Rutas API: `docs/API_ROUTES.md`, `docs/API_ROUTES_FULL.md`
- Guía de scaffolding: `README.md`
- Configuración: `src/main/resources/application.yml`
