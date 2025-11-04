# 🗺️ ROADMAP – Implementación RFC v1.0

**Home tipo "PedidosYa" + BackOffice estilo Setmore**

Última actualización: 2025-11-04  
Autor: Francisco López

---

## 📊 Estado actual del proyecto

### ✅ Lo que ya tenemos implementado:

#### Backend
- ✅ Arquitectura hexagonal básica (domain/application/infrastructure)
- ✅ Entidades principales: Usuario, Empresa, Empleado, Servicio, Turno, Disponibilidad, VerificacionTelefono
- ✅ Endpoints públicos básicos:
  - `GET /api/public/empresas` (con filtros por categoría, paginación)
  - `GET /api/public/empresas/{id}` (por ID, no por slug)
  - `GET /api/public/empresas/{empresaId}/servicios`
  - `POST /api/public/turnos`
- ✅ Auth básico: `POST /api/auth/login`, `POST /api/auth/register`
- ✅ Multi-tenant: `GET /api/me/empresas`
- ✅ CRUD Empleados: `GET/POST/PUT /api/empresas/{empresaId}/empleados`
- ✅ CRUD Servicios: `GET/POST/PUT /api/empresas/{empresaId}/servicios`
- ✅ CRUD Turnos: `GET/POST /api/turnos` + `/aprobar`, `/cancelar`, `/completar`
- ✅ Modelo VerificacionTelefono existe

### ❌ Lo que falta implementar según nuevo RFC:

#### A. Modelo de datos (campos faltantes)
- ❌ `Empresa.slug` (para URLs amigables `/empresa/:slug`)
- ❌ `Empleado.trabajaPublicamente` (para filtrar empleados visibles)
- ❌ `Servicio.patronBloques` (JSON para configuración avanzada)
- ❌ Estados de turno: actualizar de `completado` a `realizado`, agregar `no_asistio`

#### B. Endpoints públicos faltantes
- ❌ `GET /api/empresas/{slug}` (buscar por slug en lugar de ID)
- ❌ `GET /api/empresas/{slug}/empleados` (empleados públicos filtrados)
- ❌ `POST /api/verificaciones` (crear/enviar código SMS)
- ❌ `POST /api/verificaciones/{id}/confirm` (validar código)

#### C. BackOffice completo (con validación empresa asociada)
- ❌ Middleware de validación: verificar que usuario tiene empresa asociada
- ❌ Reestructurar endpoints bajo `/api/backoffice/*`:
  - `GET /api/backoffice/empresa` (empresa activa del usuario)
  - `GET /api/backoffice/empleados` (reemplaza el actual)
  - `POST/PUT/DELETE /api/backoffice/empleados/{id}`
  - `GET /api/backoffice/servicios`
  - `POST/PUT/DELETE /api/backoffice/servicios/{id}`
  - `GET /api/backoffice/turnos` (con filtros: fechaDesde, fechaHasta, empleadoId, estado)
  - `PATCH /api/backoffice/turnos/{id}/aceptar`
  - `PATCH /api/backoffice/turnos/{id}/rechazar`
  - `POST /api/backoffice/turnos` (crear turno manual)
  - **`GET /api/backoffice/calendario`** (formato para FullCalendar)
  - `PUT /api/backoffice/configuracion` (actualizar settings empresa)

#### D. SuperAdmin
- ❌ `POST /api/admin/relaciones` (asociar usuario ↔ empresa)

#### E. Lógica de negocio
- ❌ Validación telefónica: integrar con Twilio/WhatsApp
- ❌ Bloqueo transaccional de slots (evitar doble reserva)
- ❌ Validación de `patron_bloques` al calcular horarios disponibles
- ❌ Response diferenciado al crear turno público (si requiere validación → `requires_validation: true`)

---

## 🎯 SPRINT 1 (1-2 semanas) – PRIORIDAD ALTA

**Objetivo**: Home público funcional + EmpresaDetalle + Reserva básica

### Backend - Tareas

#### 1.1 Modelo de datos - Migraciones DB
- [x] **Agregar campo `slug` a tabla `empresa`**
  - Columna: `slug VARCHAR(200) UNIQUE NOT NULL`
  - Generar slug automático al crear empresa (ej: "peluqueria-lola" → normalizar nombre)
  - Crear índice único en `slug`
  - Migración: `V1__add_slug_to_empresa.sql` (si usas Flyway) o actualizar entidades

- [x] **Agregar campo `trabaja_publicamente` a tabla `empleado`**
  - Columna: `trabaja_publicamente BOOLEAN DEFAULT true`
  - Actualizar entidad `Empleado.java` y `EmpleadoEntity.java`

- [x] **Agregar campo `patron_bloques` a tabla `servicio`**
  - Columna: `patron_bloques JSON NULL` (o TEXT para MySQL <5.7)
  - Actualizar entidad `Servicio.java` y `ServicioEntity.java`

- [ ] **Actualizar enum estados de `turno`**
  - Cambiar: `completado` → `realizado`
  - Agregar: `no_asistio`
  - Actualizar constantes en código

#### 1.2 Endpoints públicos

- [x] **`GET /api/empresas/{slug}` - Detalle empresa por slug**
  - Crear método en `EmpresaRepository`: `Optional<Empresa> findBySlug(String slug)`
  - Crear en `PublicEmpresaController`:
    ```java
    @GetMapping("/slug/{slug}")
    public ResponseEntity<EmpresaDetalleDTO> obtenerPorSlug(@PathVariable String slug)
    ```
  - DTO Response debe incluir:
    - Datos empresa (id, nombre, descripcion, direccion, telefono, categoria)
    - Lista de servicios activos
    - Lista de empleados públicos (trabaja_publicamente=true)
    - Horarios base de disponibilidad

- [x] **`GET /api/empresas/{slug}/empleados` - Empleados públicos**
  - Filtrar solo empleados con `trabajaPublicamente = true` y `activo = true`
  - Response: `List<EmpleadoPublicoDTO>` (id, nombre, apellido, rol)

- [x] **Actualizar `POST /api/public/turnos` - Response mejorado**
  - Si `empresa.requiereValidacionTelefono == true`:
    - NO confirmar turno inmediatamente
    - Crear registro en `verificacion_telefono` (sin enviar SMS aún)
    - Response: `{ turnoId, estado: "pendiente_validacion", requiresValidation: true, verificationId }`
  - Si NO requiere validación:
    - Si `empresa.requiereAprobacionTurno == true` → estado: `pendiente_aprobacion`
    - Si NO → estado: `confirmado`
    - Response: `{ turnoId, estado }`

#### 1.3 Middleware BackOffice

- [x] **Crear `BackofficeAccessFilter` o Interceptor**
  - Interceptar todas las rutas `/api/backoffice/*`
  - Verificar que usuario autenticado tiene al menos 1 empresa asociada activa
  - Si NO tiene empresa → retornar `403 Forbidden` con mensaje:
    ```json
    {
      "code": "NO_EMPRESA_ASOCIADA",
      "message": "No estás asociado a ninguna empresa",
      "details": null
    }
    ```

- [x] **`GET /api/backoffice/empresa` - Empresa activa del usuario**
  - Obtener empresa del usuario autenticado (desde `UsuarioEmpresa`)
  - Si tiene múltiples empresas, retornar la primera activa (o la seleccionada en header/contexto)
  - Response: `EmpresaDTO` completo

#### 1.4 Documentación

- [x] **Actualizar `docs/API_ROUTES.md`** con nuevos endpoints
- [ ] **Crear/actualizar Swagger** con ejemplos de payloads
- [ ] **Documentar formato de `patron_bloques` JSON** (estructura esperada)

### Frontend - Tareas

- [ ] **Home pública (`/`)**
  - Grid/carrusel de empresas
  - Consumir: `GET /api/empresas?page=0&size=12`
  - Filtros: categoría (sidebar o dropdown)
  - Cards con: logo, nombre, descripción corta, categoría

- [ ] **EmpresaDetalle (`/empresa/:slug`)**
  - Consumir: `GET /api/empresas/{slug}`
  - Mostrar: info empresa, servicios, empleados públicos
  - Botón "Reservar" por cada servicio

- [ ] **Flujo de Reserva básico (sin validación telefónica)**
  - Formulario: nombre, teléfono, email (opcional), DNI (opcional)
  - Selector de fecha/hora (slots disponibles)
  - Consumir: `POST /api/public/turnos`
  - Manejar response: si `requiresValidation=false` → mostrar confirmación

### Criterios de aceptación Sprint 1

- ✅ Home muestra empresas públicas con paginación
- ✅ Click en empresa abre `/empresa/:slug` con servicios y empleados
- ✅ Usuario puede crear turno público y recibir confirmación (sin validación telefónica)
- ✅ BackOffice rechaza acceso si usuario no tiene empresa asociada (403)
- ✅ Swagger actualizado con nuevos endpoints

---

## 🎯 SPRINT 2 (1-2 semanas) – PRIORIDAD ALTA

**Objetivo**: Validación telefónica + BackOffice calendario + Bloqueo de slots

### Backend - Tareas

#### 2.1 Validación telefónica

- [x] **`POST /api/public/verificaciones` - Crear/enviar código**
  - Body: `{ telefono: string, canal?: string, turnoId?: number }`
  - Lógica:
    - Generar código aleatorio de 6 dígitos
    - Guardar en `verificacion_telefono` con `fecha_expiracion` (5 min)
    - Enviar SMS via Twilio/WhatsApp (implementado con mock para desarrollo)
    - Rate limit: máximo 3 intentos por teléfono cada 5 minutos (TODO: implementar en Sprint 3)
  - Response: `{ verificationId: number, expiresAt: datetime }`

- [x] **`POST /api/public/verificaciones/{id}/confirm` - Validar código**
  - Body: `{ codigo: string }`
  - Validar:
    - Código coincide
    - No expirado
    - No usado previamente
  - Marcar `validado = true`
  - Si hay `fk_turno`:
    - Actualizar turno: `telefono_validado = true`
    - Cambiar estado: `pendiente_validacion` → `confirmado` o `pendiente_aprobacion`
  - Response: `{ success: true, turnoId?, message }`

- [x] **Integrar verificación en `POST /api/public/turnos`**
  - Si `empresa.requiereValidacionTelefono == true`:
    - Crear turno con estado `pendiente_validacion`
    - Crear verificación automáticamente y enviar SMS
    - Response incluye `verificationId`

- [x] **Configurar servicio de SMS**
  - Twilio SDK (o WhatsApp Cloud API)
  - Variables de entorno: `TWILIO_ACCOUNT_SID`, `TWILIO_AUTH_TOKEN`, `TWILIO_FROM_NUMBER`
  - Mock mode para desarrollo (loguea código en consola)

#### 2.2 BackOffice – Calendario

- [x] **`GET /api/backoffice/calendario` - Eventos para FullCalendar**
  - Query params: `desde`, `hasta`, `empleadoId?`, `estados?`
  - Obtener turnos de la empresa en ese rango
  - Response formato FullCalendar compatible:
    - Campos: id, title, start, end, backgroundColor, borderColor
    - Extended props: clienteNombre, servicioNombre, empleadoNombre, estado, etc.
    - Colores por estado (verde=confirmado, amarillo=pendiente, rojo=cancelado, gris=completado)

- [ ] **Frontend - Componente Calendario**
  - Usar FullCalendar v6
  - Vista: mes, semana, día
  - Click en evento → modal con detalle del turno
  - Botones: Aprobar, Rechazar, Cancelar (según estado)

#### 2.3 Bloqueo transaccional de slots

- [x] **Mejorar `CrearTurnoUseCase` con bloqueo optimista**
  - `@Transactional` con nivel de aislamiento `REPEATABLE_READ`
  - Validación de solapamiento dentro de transacción
  - Comentarios documentando estrategia de bloqueo
  - Opción futura: `@Lock(LockModeType.PESSIMISTIC_WRITE)` para alta concurrencia

- [ ] **Opcional: Agregar versionado optimista**
  - Columna `version` en `turno` (JPA `@Version`)
  - Detectar conflictos y reintentar

### Criterios de aceptación Sprint 2

- ✅ Usuario recibe SMS con código al crear turno (empresa con validación activa)
- ✅ Usuario puede confirmar código y ver turno confirmado
- ✅ BackOffice muestra calendario con turnos en formato FullCalendar
- ✅ No se permiten turnos solapados (bloqueo transaccional funciona)
- ✅ Documentación actualizada con nuevos endpoints

---
{{ ... }}
