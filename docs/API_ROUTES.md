# API Routes

Listado de endpoints expuestos actualmente por la API.

Base URL (local): `http://localhost:8080`

---

## Público (sin autenticación)

- GET `/api/public/empresas`
  - Query params: `categoriaId`, `page`, `size`
  - Devuelve empresas visibles

- GET `/api/public/empresas/{empresaId}/servicios`
  - Query params: `soloActivos` (default=true), `page`, `size`

- POST `/api/public/turnos`
  - Body: TurnoCreateRequest (datos de cliente anónimo y turno)

---

## Auth

- POST `/api/auth/register`
  - Body: `{ nombre, apellido, email, telefono, password, rol }`

- POST `/api/auth/login`
  - Body: `{ email, password }`
  - Respuesta: `{ id, email, rol }`
  - Para llamadas protegidas usar Basic Auth (Authorization: Basic base64(email:password))

---

## Backoffice – Empresas

- GET `/api/empresas`
  - Query params: `visibles`, `activo`, `categoriaId`, `page`, `size`

- GET `/api/empresas/{id}`

- GET `/api/me/empresas`
  - Devuelve las empresas asignadas al usuario autenticado (multi-tenant)

---

## Backoffice – Empleados

- GET `/api/empresas/{empresaId}/empleados`
  - Query params: `activo`, `page`, `size`

- POST `/api/empresas/{empresaId}/empleados`
  - Body: EmpleadoRequest

---

## Backoffice – Servicios

- GET `/api/empresas/{empresaId}/servicios`
  - Query params: `activo`, `page`, `size`

- POST `/api/empresas/{empresaId}/servicios`
  - Body: ServicioRequest

---

## Turnos (protegido)

- GET `/api/turnos`
  - Query params: `empresaId`, `empleadoId`, `estado`, `desde`, `hasta`, `page`, `size`

- GET `/api/turnos/{id}`

- POST `/api/turnos`
  - Body: TurnoCreateRequest

- POST `/api/turnos/{id}/aprobar`

- POST `/api/turnos/{id}/cancelar`
  - Body opcional: `{ motivo }`

- POST `/api/turnos/{id}/completar`

---

## Salud

- GET `/health`

---

## Seguridad y CORS

- Autenticación: HTTP Basic (Fase 1)
  - Backoffice protegido por roles: SUPERADMIN, EMPRESA, EMPLEADO (según recurso)
- CORS permitido para Front local: `http://localhost:5173`
  - Métodos: GET, POST, PUT, PATCH, DELETE, OPTIONS
  - Headers: Authorization, Content-Type, Accept, Origin
  - Credentials: true
