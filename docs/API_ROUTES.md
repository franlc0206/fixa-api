-# API Routes

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

---

## SuperAdmin (requiere rol SUPERADMIN)

### Usuarios

- GET `/api/superadmin/users`
  - Respuesta 200: `Usuario[]`

- POST `/api/superadmin/users`
  - Body:
  ```json
  {
    "nombre": "Ana",
    "apellido": "García",
    "email": "ana@empresa.com",
    "telefono": "+54 11 5555-5555",
    "rol": "EMPRESA",
    "activo": true
  }
  ```
  - Respuesta 200: `Usuario`

- PUT `/api/superadmin/users/{id}`
  - Body (parcial):
  ```json
  {
    "nombre": "Ana",
    "apellido": "G.",
    "telefono": "",
    "rol": "SUPERADMIN",
    "activo": true
  }
  ```

- PATCH `/api/superadmin/users/{id}/activar?activo=true|false`

### Relaciones Usuario ↔ Empresa

- GET `/api/superadmin/relaciones?usuarioId=&empresaId=`
  - Si se envía `usuarioId` o `empresaId`: retorna lista simple de relaciones.
  - Si no se envían filtros: retorna listado global paginado por query `page` (0-based) y `size`.
  - Respuesta 200 (con filtros):
  ```json
  [
    { "usuarioId": 1, "empresaId": 10, "rolEmpresa": "OWNER", "activo": true }
  ]
  ```
  - Respuesta 200 (global paginado):
  ```json
  {
    "content": [ { "usuarioId": 1, "empresaId": 10, "rolEmpresa": "OWNER", "activo": true } ],
    "page": 0,
    "size": 20,
    "totalElements": 123,
    "totalPages": 7
  }
  ```

- POST `/api/superadmin/relaciones`
  - Body:
  ```json
  { "usuarioId": 1, "empresaId": 10, "rolEmpresa": "OWNER", "activo": true }
  ```
  - Respuesta 200 (echo):
  ```json
  { "usuarioId": 1, "empresaId": 10, "rolEmpresa": "OWNER", "activo": true }
  ```

- DELETE `/api/superadmin/relaciones?usuarioId=&empresaId=`

### Categorías

- GET `/api/superadmin/categorias`
  - Respuesta 200: `Categoria[]`

- POST `/api/superadmin/categorias`
  - Body:
  ```json
  { "tipo": "empresa", "nombre": "Peluquerías", "descripcion": "", "activo": true }
  ```
  - Respuesta 200: `Categoria`

- PUT `/api/superadmin/categorias/{id}`
  - Body (parcial):
  ```json
  { "tipo": "empresa", "nombre": "Peluquerías y Barberías", "descripcion": "" }
  ```

- PATCH `/api/superadmin/categorias/{id}/activar?activo=true|false`

### Empresas

- POST `/api/superadmin/empresas`
  - Body: EmpresaRequest
  - Respuesta 200: `Empresa`

- PUT `/api/superadmin/empresas/{id}`
  - Body: EmpresaRequest
  - Respuesta 200: `Empresa` | 404

- PATCH `/api/superadmin/empresas/{id}/activar?activo=true|false`
  - 204 | 404

Nota: Por seguridad, `POST/PUT/PATCH /api/empresas/**` están restringidos a SUPERADMIN. Las empresas pueden leer sus datos, pero el alta/edición se gestiona desde el panel de SuperAdmin.
