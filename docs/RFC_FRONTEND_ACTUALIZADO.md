# 🚀 RFC Frontend v2.0 - Fixa Web App

**Última actualización**: 2025-11-04  
**Backend**: Sprint 1, 2 y 3 completados  
**Base URL**: `http://localhost:8080`

---

## 📋 Resumen Ejecutivo

Sistema de gestión de turnos web con **3 áreas principales**:
- 🌐 **Público**: Catálogo de empresas, reserva anónima con validación telefónica
- 🏢 **BackOffice**: Gestión multi-tenant de empresa (turnos, empleados, servicios, calendario)
- 👑 **SuperAdmin**: Administración global del sistema

**Backend Status**: ✅ **100% funcional** con 51 archivos implementados en arquitectura hexagonal.

---

## 🎯 Stack Técnico Frontend (Recomendado)

```
- Build: Vite + React 18 + TypeScript
- Router: React Router v6.22+
- Data: React Query (TanStack Query) + Zustand
- UI: Tailwind CSS + shadcn/ui o Chakra UI
- Forms: React Hook Form + Zod
- HTTP: Axios con interceptors
- Calendar: FullCalendar v6 (BackOffice)
- Tests: Vitest + React Testing Library + MSW
```

---

## 🔐 Autenticación

**Implementación Actual** (MVP):
- **HTTP Basic Auth** (email:password en Base64)
- Header: `Authorization: Basic base64(email:password)`

**Para Producción** (futuro):
- Migrar a JWT + Refresh Tokens

---

## 📡 ENDPOINTS DISPONIBLES - BACKEND COMPLETADO

### 🌐 MÓDULO PÚBLICO (Sin autenticación)

#### 1. Listar Empresas
```http
GET /api/public/empresas?categoriaId=&page=0&size=12
```
**Response 200**:
```json
[
  {
    "id": 1,
    "nombre": "Peluquería Lola",
    "slug": "peluqueria-lola",
    "descripcion": "...",
    "direccion": "...",
    "telefono": "+5491112345678",
    "categoriaId": 2,
    "visibilidadPublica": true,
    "activo": true
  }
]
```

#### 2. Obtener Empresa por Slug ⭐ NUEVO
```http
GET /api/public/empresas/slug/{slug}
```
**Response 200**:
```json
{
  "id": 1,
  "nombre": "Peluquería Lola",
  "slug": "peluqueria-lola",
  "descripcion": "Los mejores cortes de la zona",
  "direccion": "Av. Corrientes 1234",
  "telefono": "+5491112345678",
  "email": "info@peluquerialola.com",
  "categoriaId": 2,
  "usuarioAdminId": 10,
  "visibilidadPublica": true,
  "requiereValidacionTelefono": true,
  "requiereAprobacionTurno": false,
  "activo": true
}
```

#### 3. Listar Empleados Públicos por Slug ⭐ NUEVO
```http
GET /api/public/empresas/slug/{slug}/empleados
```
**Response 200**: Solo empleados con `trabajaPublicamente=true` y `activo=true`
```json
[
  {
    "id": 5,
    "empresaId": 1,
    "nombre": "Manuel",
    "apellido": "García",
    "rol": "Peluquero Senior",
    "trabajaPublicamente": true,
    "activo": true
  }
]
```

#### 4. Listar Servicios de Empresa
```http
GET /api/public/empresas/{empresaId}/servicios?soloActivos=true&page=0&size=20
```
**Response 200**:
```json
[
  {
    "id": 10,
    "empresaId": 1,
    "nombre": "Corte de cabello",
    "descripcion": "Corte profesional",
    "precio": 5000.00,
    "duracionMinutos": 30,
    "activo": true
  }
]
```

#### 5. Crear Turno Anónimo (con validación telefónica opcional)
```http
POST /api/public/turnos
Content-Type: application/json

{
  "servicioId": 10,
  "empleadoId": 5,
  "empresaId": 1,
  "clienteNombre": "Juan",
  "clienteApellido": "Pérez",
  "clienteTelefono": "+5491112345678",
  "clienteEmail": "juan@example.com",
  "clienteDni": "12345678",
  "fechaHoraInicio": "2025-11-10T14:00:00",
  "observaciones": "Primera vez"
}
```

**Response 200** (sin validación telefónica):
```json
{
  "turnoId": 15,
  "estado": "CONFIRMADO",
  "requiresValidation": false,
  "verificationId": null,
  "message": "Turno confirmado exitosamente."
}
```

**Response 200** (con validación telefónica requerida):
```json
{
  "turnoId": 16,
  "estado": "PENDIENTE",
  "requiresValidation": true,
  "verificationId": 1,
  "message": "Turno creado. Hemos enviado un código de verificación a +5491112345678. Por favor, confírmalo para completar tu reserva."
}
```

---

### 📱 VERIFICACIÓN TELEFÓNICA ⭐ NUEVO - SPRINT 2

#### 6. Crear Verificación (enviar SMS)
```http
POST /api/public/verificaciones
Content-Type: application/json

{
  "telefono": "+5491112345678",
  "canal": "sms",
  "turnoId": 15
}
```

**Response 200**:
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

**Response 429** (Rate Limit - máximo 3 intentos cada 5 min):
```json
{
  "status": 429,
  "message": "Demasiados intentos. Por favor, espera 5 minutos antes de intentar nuevamente.",
  "timestamp": "2025-11-10T14:05:00"
}
```

#### 7. Confirmar Código de Verificación
```http
POST /api/public/verificaciones/{id}/confirm
Content-Type: application/json

{
  "codigo": "123456"
}
```

**Response 200**:
```json
{
  "id": 1,
  "telefono": "+5491112345678",
  "validado": true,
  "turnoId": 15,
  "message": "Código verificado exitosamente. Tu turno ha sido confirmado."
}
```

**Response 400** (código inválido/expirado):
```json
{
  "status": 400,
  "message": "Código inválido" | "El código ha expirado. Solicita uno nuevo."
}
```

---

### 🔐 AUTENTICACIÓN

#### 8. Registro de Usuario
```http
POST /api/auth/register
Content-Type: application/json

{
  "nombre": "Juan",
  "apellido": "Pérez",
  "email": "juan@example.com",
  "password": "SecurePass123",
  "telefono": "+5491112345678"
}
```

#### 9. Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "juan@example.com",
  "password": "SecurePass123"
}
```

**Response 200**:
```json
{
  "id": 10,
  "email": "juan@example.com",
  "nombre": "Juan",
  "apellido": "Pérez",
  "rol": "EMPRESA" | "EMPLEADO" | "SUPERADMIN"
}
```

---

### 🏢 BACKOFFICE (Requiere autenticación + empresa asociada)

**⚠️ IMPORTANTE**: Todos los endpoints `/api/backoffice/*` están protegidos por `BackofficeAccessFilter`:
- Usuario debe estar autenticado
- Usuario debe tener al menos 1 empresa asociada activa
- Si no cumple: **403 Forbidden** `{ code: "NO_EMPRESA_ASOCIADA", message: "No estás asociado a ninguna empresa" }`

#### 10. Obtener Empresa Activa del Usuario
```http
GET /api/backoffice/empresa
Authorization: Basic base64(email:password)
```

**Response 200**:
```json
{
  "id": 1,
  "nombre": "Peluquería Lola",
  "slug": "peluqueria-lola",
  "descripcion": "...",
  "requiereValidacionTelefono": true,
  "requiereAprobacionTurno": false
}
```

#### 11. Calendario BackOffice ⭐ NUEVO - SPRINT 2
```http
GET /api/backoffice/calendario?desde=2025-11-01T00:00:00&hasta=2025-11-30T23:59:59&empleadoId=5&estados=CONFIRMADO,PENDIENTE
Authorization: Basic base64(email:password)
```

**Response 200** (formato FullCalendar v6):
```json
[
  {
    "id": 15,
    "title": "Juan Pérez - Corte de cabello",
    "start": "2025-11-10T14:00:00",
    "end": "2025-11-10T14:30:00",
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
    "servicioId": 10,
    "requiereValidacion": true,
    "telefonoValidado": true
  }
]
```

**Colores por Estado**:
- 🟢 CONFIRMADO: `#28a745` (verde)
- 🟡 PENDIENTE: `#ffc107` (amarillo)
- 🔴 CANCELADO: `#dc3545` (rojo)
- ⚫ COMPLETADO/REALIZADO: `#6c757d` (gris)

#### 12-16. CRUD Empresas
```http
GET    /api/empresas?visibles=true&activo=true&page=0&size=20
POST   /api/empresas
PUT    /api/empresas/{id}
PATCH  /api/empresas/{id}/activar?activo=true
GET    /api/empresas/{id}
```

#### 17-21. CRUD Empleados (por empresa)
```http
GET    /api/empresas/{empresaId}/empleados?activo=true&page=0&size=20
POST   /api/empresas/{empresaId}/empleados
PUT    /api/empresas/{empresaId}/empleados/{id}
DELETE /api/empresas/{empresaId}/empleados/{id}
GET    /api/empresas/{empresaId}/empleados/{id}
```

#### 22-26. CRUD Servicios (por empresa)
```http
GET    /api/empresas/{empresaId}/servicios?activo=true&page=0&size=20
POST   /api/empresas/{empresaId}/servicios
PUT    /api/empresas/{empresaId}/servicios/{id}
DELETE /api/empresas/{empresaId}/servicios/{id}
GET    /api/empresas/{empresaId}/servicios/{id}
```

#### 27-29. Disponibilidad (por empleado)
```http
GET    /api/empleados/{empleadoId}/disponibilidad
POST   /api/empleados/{empleadoId}/disponibilidad
DELETE /api/empleados/{empleadoId}/disponibilidad?diaSemana=LUNES
```

#### 30-35. Gestión de Turnos
```http
GET  /api/turnos?empresaId=1&empleadoId=5&estado=PENDIENTE&desde=2025-11-01T00:00:00&hasta=2025-11-30T23:59:59&page=0&size=20
GET  /api/turnos/{id}
POST /api/turnos
POST /api/turnos/{id}/aprobar
POST /api/turnos/{id}/cancelar (Body: { "motivo": "Cliente canceló" })
POST /api/turnos/{id}/completar
```

---

### 👑 SUPERADMIN (Solo rol SUPERADMIN)

#### 36-40. Gestión de Usuarios
```http
GET   /api/superadmin/users?page=0&size=20
POST  /api/superadmin/users
PUT   /api/superadmin/users/{id}
PATCH /api/superadmin/users/{id}/activar?activo=true
GET   /api/superadmin/users/{id}
```

#### 41-44. Relaciones Usuario-Empresa
```http
GET    /api/superadmin/relaciones?usuarioId=10&empresaId=1
POST   /api/superadmin/relaciones (Body: { "usuarioId": 10, "empresaId": 1, "rol": "OWNER" })
PATCH  /api/superadmin/relaciones/activar?usuarioId=10&empresaId=1&activo=true
DELETE /api/superadmin/relaciones?usuarioId=10&empresaId=1
```

#### 45-48. Gestión de Categorías
```http
GET   /api/superadmin/categorias?page=0&size=20
POST  /api/superadmin/categorias
PUT   /api/superadmin/categorias/{id}
PATCH /api/superadmin/categorias/{id}/activar?activo=true
```

---

## 🗺️ ROADMAP FRONTEND POR FASES

### 📦 FASE 1: Setup + Público (2-3 semanas)

**Prioridad: ALTA** ✅

#### Setup Inicial
- ✅ Crear proyecto Vite + React + TypeScript
- ✅ Configurar Tailwind CSS + shadcn/ui
- ✅ Configurar ESLint + Prettier + Husky
- ✅ Configurar React Query + Axios
- ✅ Crear estructura de carpetas (features, shared, app)

#### Módulo Público
**Endpoints a consumir**:

1. **Home Landing** (`/`)
   - `GET /api/public/empresas?page=0&size=12`
   - Grid de empresas con paginación
   - Filtros por categoría (futuro: categorías desde backend)

2. **Detalle de Empresa** (`/empresa/:slug`)
   - `GET /api/public/empresas/slug/{slug}`
   - `GET /api/public/empresas/slug/{slug}/empleados`
   - Mostrar banner, servicios, empleados públicos

3. **Reserva Anónima** (`/reserva`)
   - `POST /api/public/turnos`
   - Formulario con React Hook Form + Zod
   - Manejo de `requiresValidation` en response

4. **Validación Telefónica** (`/validar`)
   - `POST /api/public/verificaciones`
   - `POST /api/public/verificaciones/{id}/confirm`
   - Modal/página para ingresar código de 6 dígitos
   - Manejo de HTTP 429 (rate limiting)
   - Timer de expiración (5 minutos)

**React Query Keys**:
```typescript
['empresas-publicas', { categoriaId, page, size }]
['empresa-slug', slug]
['empleados-publicos', slug]
['servicios-publicos', empresaId, { soloActivos, page, size }]
```

---

### 📦 FASE 2: Autenticación (1 semana)

**Prioridad: ALTA** ✅

#### Endpoints a consumir:
- `POST /api/auth/register`
- `POST /api/auth/login`

#### Componentes:
- `AuthProvider` (contexto global)
- `LoginForm` + `RegisterForm`
- `PrivateRoute` guard
- Interceptor Axios para Basic Auth

#### Storage:
- `sessionStorage` o `localStorage` para credenciales (Base64)
- Limpiar en logout

---

### 📦 FASE 3: BackOffice - Calendario + Turnos (2 semanas)

**Prioridad: ALTA** ✅

#### 1. Calendario FullCalendar
**Endpoint**: `GET /api/backoffice/calendario`

**Componentes**:
```typescript
// features/backoffice/calendario/pages/CalendarioPage.tsx
import FullCalendar from '@fullcalendar/react'
import dayGridPlugin from '@fullcalendar/daygrid'
import timeGridPlugin from '@fullcalendar/timegrid'
import interactionPlugin from '@fullcalendar/interaction'

// Consumir eventos del backend
const { data: eventos } = useQuery({
  queryKey: ['calendario', empresaId, { desde, hasta, empleadoId, estados }],
  queryFn: () => getCalendario(empresaId, { desde, hasta, empleadoId, estados })
})

// FullCalendar config
events={eventos}
plugins={[dayGridPlugin, timeGridPlugin, interactionPlugin]}
initialView="dayGridMonth"
headerToolbar={{
  left: 'prev,next today',
  center: 'title',
  right: 'dayGridMonth,timeGridWeek,timeGridDay'
}}
```

**Features**:
- Vista mes/semana/día
- Filtros por empleado y estados
- Click en evento → modal con detalle
- Colores por estado (verde/amarillo/rojo/gris)

#### 2. Gestión de Turnos
**Endpoints**:
- `GET /api/turnos` (listado con filtros)
- `POST /api/turnos/{id}/aprobar`
- `POST /api/turnos/{id}/cancelar`
- `POST /api/turnos/{id}/completar`

**Componentes**:
- `TurnoListPage` (tabla con filtros)
- `TurnoDetailModal` (detalle + acciones)
- Botones de acción según estado

**React Query**:
```typescript
['turnos', { empresaId, empleadoId, estado, desde, hasta, page, size }]
['turno', id]

// Invalidación tras mutaciones
onSuccess: () => {
  queryClient.invalidateQueries(['turnos'])
  queryClient.invalidateQueries(['calendario'])
}
```

---

### 📦 FASE 4: BackOffice - CRUDs (2 semanas)

**Prioridad: MEDIA** ✅

#### 1. Gestión de Empleados
**Endpoints**: `GET/POST/PUT/DELETE /api/empresas/{empresaId}/empleados`

**React Query Keys**:
```typescript
['empleados', empresaId, { activo, page, size }]
['empleado', id]
```

#### 2. Gestión de Servicios
**Endpoints**: `GET/POST/PUT/DELETE /api/empresas/{empresaId}/servicios`

**React Query Keys**:
```typescript
['servicios', empresaId, { activo, page, size }]
['servicio', id]
```

#### 3. Disponibilidad
**Endpoints**: `GET/POST/DELETE /api/empleados/{empleadoId}/disponibilidad`

**React Query Keys**:
```typescript
['disponibilidad', empleadoId]
```

---

### 📦 FASE 5: SuperAdmin (2 semanas)

**Prioridad: MEDIA**

#### 1. Gestión de Empresas Globales
**Endpoints**: `GET/POST/PUT/PATCH /api/empresas`

#### 2. Gestión de Usuarios
**Endpoints**: `GET/POST/PUT/PATCH /api/superadmin/users`

#### 3. Relaciones Usuario-Empresa
**Endpoints**: `GET/POST/PATCH/DELETE /api/superadmin/relaciones`

#### 4. Categorías
**Endpoints**: `GET/POST/PUT/PATCH /api/superadmin/categorias`

---

## 🔑 MATRIZ DE ROLES Y PERMISOS

| Módulo | Público | Cliente | EMPRESA/EMPLEADO | SUPERADMIN |
|--------|---------|---------|------------------|------------|
| Ver empresas públicas | ✅ | ✅ | ✅ | ✅ |
| Crear turno anónimo | ✅ | ✅ | ✅ | ✅ |
| Verificar teléfono | ✅ | ✅ | ✅ | ✅ |
| Ver calendario empresa | ❌ | ❌ | ✅ | ✅ |
| Gestionar turnos empresa | ❌ | ❌ | ✅ | ✅ |
| CRUD empleados/servicios | ❌ | ❌ | ✅ | ✅ |
| Gestionar usuarios globales | ❌ | ❌ | ❌ | ✅ |
| Gestionar relaciones usuario-empresa | ❌ | ❌ | ❌ | ✅ |
| Gestionar categorías | ❌ | ❌ | ❌ | ✅ |

---

## 🚨 GUARDS Y REDIRECCIONES

```typescript
// app/router/guards.tsx

// Guard 1: Requiere autenticación
<PrivateRoute>
  <BackofficeLayout />
</PrivateRoute>

// Guard 2: Requiere empresa asociada
useEffect(() => {
  if (!user.hasEmpresa) {
    navigate('/registro-empresa')
  }
}, [user])

// Guard 3: Solo SuperAdmin
<SuperAdminRoute>
  <SuperAdminLayout />
</SuperAdminRoute>
```

---

## 📊 ESTRATEGIA REACT QUERY

### Cache Keys Recomendadas

```typescript
// Público
['empresas-publicas', { categoriaId, page, size }]
['empresa-slug', slug]
['empleados-publicos', slug]
['servicios-publicos', empresaId, params]

// Verificación
['verificacion', id]

// BackOffice
['calendario', empresaId, { desde, hasta, empleadoId, estados }]
['turnos', { empresaId, empleadoId, estado, desde, hasta, page }]
['turno', id]
['empleados', empresaId, params]
['servicios', empresaId, params]
['disponibilidad', empleadoId]

// SuperAdmin
['sa-empresas', params]
['sa-users', params]
['sa-relaciones', { usuarioId, empresaId }]
['sa-categorias', params]
```

### Invalidaciones tras Mutaciones

```typescript
// Crear turno → invalidar calendario + turnos
onSuccess: () => {
  queryClient.invalidateQueries(['calendario'])
  queryClient.invalidateQueries(['turnos'])
}

// Crear empleado → invalidar empleados
onSuccess: () => {
  queryClient.invalidateQueries(['empleados', empresaId])
}

// Confirmar verificación → invalidar turno
onSuccess: () => {
  queryClient.invalidateQueries(['turno', turnoId])
}
```

---

## 🎨 COMPONENTES REUTILIZABLES

```
shared/components/
  Table.tsx              # Tabla genérica con sort
  Pagination.tsx         # Paginación 0-based
  Loader.tsx            # Spinner global
  ErrorBoundary.tsx     # Manejo de errores
  EmptyState.tsx        # Estado vacío consistente
  Toast.tsx             # Notificaciones
  Modal.tsx             # Modal reutilizable
  FormFields/           # Inputs con RHF + Zod
    Input.tsx
    Select.tsx
    Textarea.tsx
    DatePicker.tsx
```

---

## ✅ CHECKLIST FRONTEND

### Setup Inicial
- [ ] Proyecto Vite + React + TS creado
- [ ] Tailwind CSS configurado
- [ ] shadcn/ui o Chakra UI instalado
- [ ] React Query configurado
- [ ] Axios con interceptors
- [ ] ESLint + Prettier + Husky
- [ ] Router con layouts (Public/BackOffice/SuperAdmin)

### Fase 1: Público
- [ ] Home con listado de empresas
- [ ] Detalle de empresa por slug
- [ ] Reserva anónima funcional
- [ ] Flujo de verificación telefónica completo
- [ ] Manejo de rate limiting (HTTP 429)

### Fase 2: Auth
- [ ] Login + Register
- [ ] AuthProvider + guards
- [ ] Persistencia de sesión

### Fase 3: BackOffice - Calendario
- [ ] FullCalendar integrado
- [ ] Eventos desde API
- [ ] Filtros por empleado/estado
- [ ] Modal detalle turno + acciones

### Fase 4: BackOffice - CRUDs
- [ ] CRUD Empleados
- [ ] CRUD Servicios
- [ ] Gestión Disponibilidad
- [ ] Listado Turnos

### Fase 5: SuperAdmin
- [ ] Gestión Empresas
- [ ] Gestión Usuarios
- [ ] Relaciones Usuario-Empresa
- [ ] Categorías

---

## 🔗 RECURSOS

**Backend**:
- Base URL: `http://localhost:8080`
- Documentación: `/docs/API_ROUTES.md`
- Sprints completados: `/docs/SPRINT1_COMPLETADO.md`, `SPRINT2_COMPLETADO.md`, `SPRINT3_COMPLETADO.md`

**Frontend**:
- FullCalendar Docs: https://fullcalendar.io/docs
- React Query Docs: https://tanstack.com/query/latest
- shadcn/ui: https://ui.shadcn.com/

---

## 📞 CONTACTO

**Tech Lead Backend**: Francisco López  
**Status**: Sprint 1, 2 y 3 completados ✅  
**Próximo Sync**: Coordinar integración Frontend-Backend

---

**Última actualización**: 2025-11-04 19:10:00
