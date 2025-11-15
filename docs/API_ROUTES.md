-# API Routes

Listado de endpoints expuestos actualmente por la API.

Base URL (local): `http://localhost:8080`

---

## Público (sin autenticación)

- GET `/api/public/empresas`
  - Query params: `categoriaId`, `page`, `size`
  - Devuelve empresas visibles

- GET `/api/public/empresas/destacadas`
  - Query params: `categoriaId?`, `limit?` (default=10, máx. 50)
  - Devuelve empresas visibles y públicas ordenadas por score de valoración (promedio + cantidad de valoraciones)
  - Respuesta 200 (ejemplo):
    ```json
    [
      {
        "id": 1,
        "nombre": "Peluquería Moderna",
        "slug": "peluqueria-moderna",
        "descripcion": "Los mejores cortes",
        "telefono": "+54 11 1234-5678",
        "email": "contacto@peluqueria.com",
        "categoriaId": 2,
        "promedioValoracion": 4.7,
        "totalValoraciones": 32
      }
    ]
    ```

- GET `/api/public/empresas/{id}`
  - Respuesta 200: Detalle de empresa pública (banner/SEO)

- GET `/api/public/empresas/slug/{slug}`
  - Respuesta 200: Detalle de empresa por slug (URLs amigables)
  - Ejemplo: `/api/public/empresas/slug/peluqueria-lola`

- GET `/api/public/empresas/slug/{slug}/empleados`
  - Respuesta 200: Lista de empleados públicos de la empresa
  - Solo retorna empleados con `trabajaPublicamente = true` y `activo = true`

- GET `/api/public/empresas/slug/{slug}/valoraciones/resumen`
  - Igual a la versión por ID pero resolviendo empresa por slug

- GET `/api/public/empresas/slug/{slug}/valoraciones`
  - Query params: `soloConResena` (default=false), `limit` (default=20, máx. 100)
  - Lista de valoraciones activas ordenadas por fecha (más recientes primero)

- GET `/api/public/empresas/{empresaId}/servicios`
  - Query params: `soloActivos` (default=true), `page`, `size`

- GET `/api/public/empresas/{empresaId}/valoraciones/resumen`
  - Respuesta 200: Promedio (1 decimal), totales y métricas básicas
  - Respuesta 404: Empresa no encontrada o no visible

- GET `/api/public/empresas/{empresaId}/valoraciones`
  - Query params: `soloConResena` (default=false), `limit` (default=20, máx. 100)
  - Respuesta 200: Lista de valoraciones activas (puntuación, reseña, fecha)
  - Respuesta 404: Empresa no encontrada o no visible

- GET `/api/public/categorias`
  - Query params: `tipo` (opcional, ej. `empresa` o `servicio`)
  - Devuelve categorías activas para poblar el Home y selects del Front
  - Respuesta 200 (ejemplo):
    ```json
    [
      { "id": 2, "nombre": "Peluquerías", "slug": "peluquerias", "tipo": "empresa", "icono": null }
    ]
    ```

- GET `/api/public/servicios/recomendados`
  - Query params: `categoriaId?`, `limit?` (default=10, máx. 50)
  - Devuelve servicios recomendados ordenados por score de valoración (promedio + cantidad de valoraciones)
  - Respuesta 200 (ejemplo):
    ```json
    [
      {
        "id": 10,
        "empresaId": 1,
        "empresaNombre": "Peluquería Moderna",
        "nombre": "Corte clásico",
        "descripcion": "Con navaja",
        "duracionMinutos": 30,
        "precio": 1200.0,
        "promedioValoracion": 4.7,
        "totalValoraciones": 32
      }
    ]
    ```

- POST `/api/public/turnos`
  - Body: TurnoCreateRequest (datos de cliente anónimo y turno)
  - Response: TurnoPublicoResponse con campos:
    - `turnoId`: Long
    - `estado`: String (CONFIRMADO, PENDIENTE, etc.)
    - `requiresValidation`: Boolean (indica si requiere validación telefónica)
    - `verificationId`: Long (ID de la verificación creada si requiere validación, null si no)
    - `message`: String (mensaje descriptivo del resultado)
  - **IMPORTANTE**: Si `requiresValidation=true`, el sistema crea automáticamente una verificación telefónica y envía el SMS con el código

---

## Verificación Telefónica (público)

- POST `/api/public/verificaciones`
  - Body: 
    ```json
    {
      "telefono": "+5491112345678",
      "canal": "sms",
      "turnoId": 15
    }
    ```
  - Response 200:
    ```json
    {
      "id": 1,
      "telefono": "+5491112345678",
      "canal": "sms",
      "fechaEnvio": "2025-11-10T14:00:00",
      "fechaExpiracion": "2025-11-10T14:05:00",
      "validado": false,
      "turnoId": 15,
      "message": "Código de verificación enviado por sms. Válido por 5 minutos."
    }
    ```
  - **IMPORTANTE**: En desarrollo, el código se loguea en consola (mock mode). En producción, se envía por SMS/WhatsApp vía Twilio.

- POST `/api/public/verificaciones/{id}/confirm`
  - Body:
    ```json
    {
      "codigo": "123456"
    }
    ```
  - Response 200:
    ```json
    {
      "id": 1,
      "telefono": "+5491112345678",
      "canal": "sms",
      "fechaEnvio": "2025-11-10T14:00:00",
      "fechaExpiracion": "2025-11-10T14:05:00",
      "validado": true,
      "turnoId": 15,
      "message": "Código verificado exitosamente. Tu turno ha sido confirmado."
    }
    ```
  - Response 400: Código inválido, expirado o verificación no encontrada

---

## Auth

- POST `/api/auth/register`
  - Body: `{ nombre, apellido, email, telefono, password, rol }`

- POST `/api/auth/login`
  - Body: `{ email, password }`
  - Respuesta: `{ id, email, rol }`
  - Para llamadas protegidas usar Basic Auth (Authorization: Basic base64(email:password))

---

## BackOffice (requiere auth + empresa asociada)

**IMPORTANTE**: Todos los endpoints `/api/backoffice/*` están protegidos por `BackofficeAccessFilter` que valida:
- Usuario autenticado
- Usuario tiene al menos 1 empresa asociada activa
- Si no cumple: retorna `403 Forbidden` con `{ code: "NO_EMPRESA_ASOCIADA", message: "No estás asociado a ninguna empresa" }`

- GET `/api/backoffice/empresa`
  - Respuesta 200: Empresa activa del usuario autenticado
  - Respuesta 403: Usuario no tiene empresa asociada

- GET `/api/backoffice/calendario`
  - Query params:
    - `desde`: Fecha/hora inicio (ISO 8601, opcional, default: inicio del mes actual)
    - `hasta`: Fecha/hora fin (ISO 8601, opcional, default: fin del mes actual)
    - `empleadoId`: Filtrar por empleado (opcional)
    - `estados`: Lista de estados a incluir (opcional, ej: CONFIRMADO,PENDIENTE)
  - Response 200: Array de eventos en formato FullCalendar
    ```json
    [
      {
        "id": 15,
        "title": "Juan Pérez - Corte de cabello",
        "start": "2025-11-10T14:00:00",
        "end": "2025-11-10T15:00:00",
        "backgroundColor": "#28a745",
        "borderColor": "#28a745",
        "textColor": "#ffffff",
        "allDay": false,
        "estado": "CONFIRMADO",
        "clienteNombre": "Juan Pérez",
        "clienteTelefono": "+5491112345678",
        "servicioNombre": "Corte de cabello",
        "empleadoNombre": "Manuel García",
        "empleadoId": 5,
        "servicioId": 1,
        "observaciones": null,
        "requiereValidacion": true,
        "telefonoValidado": true
      }
    ]
    ```
  - **Colores por estado**:
    - CONFIRMADO: Verde (#28a745)
    - PENDIENTE: Amarillo (#ffc107)
    - CANCELADO: Rojo (#dc3545)
    - COMPLETADO/REALIZADO: Gris (#6c757d)
    - Otros: Azul (#007bff)

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
  - Body: EmpleadoRequest (ahora incluye campo `trabajaPublicamente`)

- GET `/api/empresas/{empresaId}/empleados/{id}`
  - Respuesta 200: `Empleado` | `404`

- PUT `/api/empresas/{empresaId}/empleados/{id}`
  - Body: `EmpleadoRequest`
  - Respuesta 200: `Empleado` | `404`

---

## Backoffice – Servicios

- GET `/api/empresas/{empresaId}/servicios`
  - Query params: `activo`, `page`, `size`

- POST `/api/empresas/{empresaId}/servicios`
  - Body: ServicioRequest (ahora incluye campo `patronBloques` opcional)

- GET `/api/empresas/{empresaId}/servicios/{id}`
  - Respuesta 200: `Servicio` | `404`

- PUT `/api/empresas/{empresaId}/servicios/{id}`
  - Body: `ServicioRequest`
  - Respuesta 200: `Servicio` | `404`

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
## Perfil del Usuario (`/api/me`)

- GET `/api/me/empresas`
  - Devuelve las empresas asociadas al usuario autenticado

- GET `/api/me/turnos`
  - Query params: `estado`, `page`, `size`
  - Devuelve los turnos del usuario autenticado ordenados por fecha (más recientes primero)
  - Cada elemento incluye, además de los datos del turno, el flag `yaValorado: true|false` para indicar si el turno ya tiene una valoración registrada

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

- PATCH `/api/superadmin/relaciones/activar?usuarioId=&empresaId=&activo=true|false`
  - 204 | 404

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

---

## Value Lists (para selects en el Frontend)

Objetivo: el Frontend no debe tipear IDs/FKs manualmente. Debe poblar selects con valuelists y enviar solo el `id` seleccionado en los payloads.

- Categorías (global)
  - GET `/api/superadmin/valuelist/categorias?tipo=`
  - Respuesta 200:
  ```json
  [ { "id": 2, "nombre": "Peluquerías", "tipo": "empresa", "activo": true } ]
  ```
  - Uso FE:
    - Alta/edición de Empresa: `categoriaId` proviene de esta lista.
    - Alta/edición de Servicio: `categoriaId` opcional también proviene de esta lista (si aplica).

- Usuarios (búsqueda para asignaciones SA)
  - GET `/api/superadmin/valuelist/usuarios?q=&page=&size=`
  - Respuesta 200 (paginada):
  ```json
  { "content": [ { "id": 12, "email": "ana@empresa.com", "nombre": "Ana", "apellido": "García", "activo": true } ], "page": 0, "size": 20, "totalElements": 1, "totalPages": 1 }
  ```
  - Uso FE:
    - Asignar relaciones Usuario↔Empresa en `/admin/relaciones`.

- Empleados (por empresa) como valuelist
  - GET `/api/empresas/{empresaId}/empleados?activo=true&page=0&size=100`
  - Uso FE: selects para asignar turnos, filtrar por empleado, etc.

- Servicios (por empresa) como valuelist
  - GET `/api/empresas/{empresaId}/servicios?activo=true&page=0&size=100`
  - Uso FE: selects para reservas y backoffice.

Notas importantes para payloads:
- `EmpresaRequest.categoriaId`: obtener de `valuelist/categorias`.
- `ServicioRequest.categoriaId` (si aplica): obtener de `valuelist/categorias`.
- `Relaciones.rolEmpresa`: usar opciones de UI predefinidas (OWNER/ADMIN/EMPLEADO), no enviar textos libres.
- Estados de turno: consumir `GET /api/turnos/estados` (si se publica) para filtros.
