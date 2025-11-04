# API Routes (FULL)

Base URL: http://localhost:8080
Auth: HTTP Basic (MVP) para backoffice y superadmin
CORS: http://localhost:5173

---

## Público (sin auth)

- GET /api/public/empresas
  - Query: categoriaId?, page?, size?
  - 200 Respuesta:
  ```json
  [
    { "id": 1, "nombre": "Peluquería X", "descripcion": "", "telefono": "", "email": "", "categoriaId": 2, "visibilidadPublica": true, "activo": true }
  ]
  ```

- GET /api/public/empresas/{id}
  - 200 Respuesta (detalle):
  ```json
  {
    "id": 1,
    "nombre": "Peluquería Moderna",
    "descripcion": "Los mejores cortes de la ciudad",
    "direccion": "Av. Corrientes 1234",
    "telefono": "+54 11 1234-5678",
    "email": "contacto@peluqueriamoderna.com",
    "categoriaId": 2,
    "visibilidadPublica": true,
    "activo": true,
    "bannerUrl": null,
    "fotoUrl": null
  }
  ```

- GET /api/public/empresas/{empresaId}/servicios
  - Query: soloActivos?, page?, size?
  - 200 Respuesta:
  ```json
  [
    { "id": 10, "empresaId": 1, "nombre": "Corte", "descripcion": "", "duracionMinutos": 30, "requiereEspacioLibre": false, "costo": 1000, "requiereSena": false, "activo": true, "categoriaId": 2 }
  ]
  ```

- POST /api/public/turnos
  - Body (TurnoCreateRequest):
  ```json
  {
    "servicioId": 10,
    "empleadoId": 5,
    "empresaId": 1,
    "clienteId": null,
    "clienteNombre": "Juan",
    "clienteApellido": "Pérez",
    "clienteTelefono": "+54 11 2222-3333",
    "clienteDni": "12345678",
    "clienteEmail": "juan@example.com",
    "fechaHoraInicio": "2025-10-25T10:00:00",
    "observaciones": "Sin perfume"
  }
  ```
  - 200 Respuesta (Turno):
  ```json
  {
    "id": 99,
    "servicioId": 10,
    "empleadoId": 5,
    "empresaId": 1,
    "clienteId": null,
    "clienteNombre": "Juan",
    "clienteApellido": "Pérez",
    "clienteTelefono": "+54 11 2222-3333",
    "clienteDni": "12345678",
    "clienteEmail": "juan@example.com",
    "telefonoValidado": false,
    "fechaHoraInicio": "2025-10-25T10:00:00",
    "fechaHoraFin": "2025-10-25T10:30:00",
    "estado": "PENDIENTE",
    "requiereValidacion": true,
    "observaciones": "Sin perfume"
  }
  ```

---

## Auth

- POST /api/auth/register
  - Body:
  ```json
  { "nombre":"Admin", "apellido":"User", "email":"admin@fixa.local", "telefono":"000000", "password":"admin123", "rol":"SUPERADMIN" }
  ```
  - 200 Respuesta: Usuario

- POST /api/auth/login
  - Body:
  ```json
  { "email":"admin@fixa.local", "password":"admin123" }
  ```
  - 200 Respuesta:
  ```json
  { "id": 1, "email":"admin@fixa.local", "rol":"SUPERADMIN" }
  ```
  - Usar Basic Auth en siguientes requests

---

## Mi perfil (multi-tenant)

- GET /api/me/empresas
  - 200 Respuesta:
  ```json
  [ { "id": 1, "nombre": "Mi Empresa", "activo": true, "visibilidadPublica": true } ]
  ```

---

## Empresas (backoffice / superadmin)

- GET /api/empresas
  - Query: visibles?, activo?, categoriaId?, page?, size?
  - 200 Respuesta: Empresa[]

- GET /api/empresas/{id}
  - 200: Empresa | 404

- POST /api/empresas
  - Body (EmpresaRequest):
  ```json
  {
    "nombre":"Mi Empresa",
    "descripcion":"",
    "direccion":"Av. Siempre Viva 742",
    "telefono":"11-1234-5678",
    "email":"contacto@miempresa.com",
    "categoriaId":2,
    "permiteReservasSinUsuario":true,
    "requiereValidacionTelefono":false,
    "requiereAprobacionTurno":true,
    "mensajeValidacionPersonalizado":null,
    "visibilidadPublica":true,
    "activo":true
  }
  ```
  - 200: Empresa

- PUT /api/empresas/{id}
  - Body: EmpresaRequest (mismos campos)
  - 200: Empresa | 404

- PATCH /api/empresas/{id}/activar?activo=true|false
  - 204 | 404

---

## Empleados (por empresa)

- GET /api/empresas/{empresaId}/empleados
  - Query: activo?, page?, size?
  - 200: Empleado[]

- POST /api/empresas/{empresaId}/empleados
  - Body:
  ```json
  { "nombre":"Ana", "apellido":"García", "rol":"ESTILISTA", "activo":true }
  ```
  - 200: Empleado

- GET /api/empresas/{empresaId}/empleados/{id}
  - 200: Empleado | 404
  - Ejemplo 200:
  ```json
  { "id": 5, "empresaId": 1, "nombre":"Ana", "apellido":"García", "rol":"ESTILISTA", "activo": true }
  ```

- PUT /api/empresas/{empresaId}/empleados/{id}
  - Body:
  ```json
  { "nombre":"Ana", "apellido":"G.", "rol":"ESTILISTA", "activo": true }
  ```
  - 200: Empleado | 404

---

## Servicios (por empresa)

- GET /api/empresas/{empresaId}/servicios
  - Query: activo?, page?, size?
  - 200: Servicio[]

- POST /api/empresas/{empresaId}/servicios
  - Body:
  ```json
  { "nombre":"Corte", "descripcion":"", "duracionMinutos":30, "requiereEspacioLibre":false, "costo":1000, "requiereSena":false, "activo":true, "categoriaId":2 }
  ```
  - 200: Servicio

- GET /api/empresas/{empresaId}/servicios/{id}
  - 200: Servicio | 404
  - Ejemplo 200:
  ```json
  { "id": 10, "empresaId": 1, "nombre":"Corte", "descripcion":"", "duracionMinutos":30, "requiereEspacioLibre":false, "costo":1000, "requiereSena":false, "activo":true, "categoriaId":2 }
  ```

- PUT /api/empresas/{empresaId}/servicios/{id}
  - Body:
  ```json
  { "nombre":"Corte clásico", "descripcion":"Con navaja", "duracionMinutos":30, "costo":1200, "requiereSena":false, "categoriaId":2, "activo":true }
  ```
  - 200: Servicio | 404

---

## Disponibilidad (por empleado)

- GET /api/empleados/{empleadoId}/disponibilidad
  - 200: Disponibilidad[]

- POST /api/empleados/{empleadoId}/disponibilidad
  - Body:
  ```json
  { "diaSemana":"LUNES", "horaInicio":"09:00", "horaFin":"18:00" }
  ```
  - 200: Disponibilidad

---

## Turnos (backoffice)

- GET /api/turnos
  - Query: empresaId?, empleadoId?, estado?, desde?, hasta?, page?, size?
  - 200: Turno[]

- GET /api/turnos/{id}
  - 200: Turno | 404

- POST /api/turnos
  - Body: igual a público (autenticado)
  - 200: Turno

- POST /api/turnos/{id}/aprobar
  - 200: Turno

- POST /api/turnos/{id}/cancelar
  - Body opcional:
  ```json
  { "motivo": "Cliente no puede asistir" }
  ```
  - 200: Turno

- POST /api/turnos/{id}/completar
  - 200: Turno

---

## SuperAdmin (requiere rol SUPERADMIN)

### Usuarios

- GET /api/superadmin/users
  - 200: Usuario[]

- POST /api/superadmin/users
  - Body:
  ```json
  { "nombre":"Ana", "apellido":"García", "email":"ana@empresa.com", "telefono":"", "rol":"EMPRESA", "activo":true }
  ```
  - 200: Usuario

- PUT /api/superadmin/users/{id}
  - Body (parcial):
  ```json
  { "nombre":"Ana", "apellido":"G.", "telefono":"", "rol":"SUPERADMIN", "activo":true }
  ```
  - 200: Usuario | 404

- PATCH /api/superadmin/users/{id}/activar?activo=true|false
  - 204 | 404

### Relaciones Usuario ↔ Empresa

- GET /api/superadmin/relaciones?usuarioId=&empresaId=
  - 200:
  ```json
  [ { "usuarioId": 1, "empresaId": 10, "rolEmpresa": "OWNER", "activo": true } ]
  ```

- POST /api/superadmin/relaciones
  - Body:
  ```json
  { "usuarioId": 1, "empresaId": 10, "rolEmpresa": "OWNER", "activo": true }
  ```
  - 200 (echo):
  ```json
  { "usuarioId": 1, "empresaId": 10, "rolEmpresa": "OWNER", "activo": true }
  ```

- DELETE /api/superadmin/relaciones?usuarioId=&empresaId=
  - 204

---

## Notas
- Todos los endpoints backoffice y superadmin requieren Basic Auth.
- Si `allowCredentials=true` en CORS, no usar `*` en `allowedOrigins`.
- Paginación: `page` 0-based, `size` > 0.
