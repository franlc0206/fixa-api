# Frontend Roadmap (React + Vite)

Guía de roadmap, arquitectura y buenas prácticas para implementar el Frontend de Fixa (público y backoffice) con React + Vite.

## Objetivos

- Público: catálogo de empresas y servicios, reserva anónima de turnos.
- Backoffice: administración de empresa, empleados, servicios, disponibilidades y turnos.
- Codebase moderna, predecible, con tests, y CI-ready.

## Stack técnico (propuesto)

- Build: Vite + React 18 + TypeScript
- Router: React Router v6.22+
- Datos: React Query (TanStack Query) para fetching/caching; Zustand o Redux Toolkit para UI/shared state simple.
- UI: Tailwind CSS o Chakra UI (a definir). Icons: Lucide o Heroicons.
- Formularios/validación: React Hook Form + Zod/Yup
- HTTP: Axios (con interceptors) o fetch + ky
- Env: Vite env vars (`import.meta.env`)
- Tests: Vitest + React Testing Library + MSW (mocks de API)
- Lint/format: ESLint (Airbnb + React + TS), Prettier, Husky + lint-staged
- i18n: react-i18next (opcional MVP)

## Estructura del proyecto (scaffolding)

```
src/
  app/
    router/
      index.tsx             # rutas públicas y privadas
    layout/
      PublicLayout.tsx
      BackofficeLayout.tsx
    providers/
      QueryProvider.tsx
      ThemeProvider.tsx
      AuthProvider.tsx      # contexto de usuario autenticado
      TenantProvider.tsx    # contexto multi-tenant: empresa activa + empresas del usuario
  features/
    auth/
      api.ts                # llamadas /api/auth
      hooks.ts              # useLogin, useRegister
      components/
        LoginForm.tsx
        RegisterForm.tsx
    public/
      empresas/
        api.ts              # GET /api/public/empresas
        pages/
          EmpresasPublicList.tsx
      servicios/
        api.ts              # GET /api/public/empresas/:id/servicios
        pages/
          ServiciosPublicList.tsx
      turnos/
        api.ts              # POST /api/public/turnos
        pages/
          ReservaAnonimaPage.tsx
    backoffice/
      empresas/
        api.ts              # /api/empresas CRUD
        pages/
          EmpresaListPage.tsx
          EmpresaEditPage.tsx
      empleados/
        api.ts              # /api/empresas/:id/empleados CRUD
        pages/
          EmpleadoListPage.tsx
          EmpleadoEditPage.tsx
      servicios/
        api.ts              # /api/empresas/:id/servicios CRUD
        pages/
          ServicioListPage.tsx
          ServicioEditPage.tsx
      disponibilidad/
        api.ts              # /api/empleados/:id/disponibilidad
        pages/
          DisponibilidadListPage.tsx
      turnos/
        api.ts              # /api/turnos listar/obtener/crear/aprobar/cancelar/completar
        pages/
          TurnoListPage.tsx
          TurnoDetailPage.tsx (opcional)
  shared/
    api/
      http.ts               # axios instance con interceptors (auth, errores)
    me/
      api.ts                # GET /api/me/empresas (empresas del usuario)
    components/
      Table.tsx
      Pagination.tsx
      FormControls.tsx
      Loader.tsx
      ErrorBoundary.tsx
    hooks/
      usePagination.ts
      useToast.ts
    utils/
      formatters.ts
      constants.ts
    types/
      index.d.ts
index.css
main.tsx
public/
vite.config.ts
tsconfig.json
package.json
```

## Convenciones

- Feature-first: cada agregado en `features/<feature>` con `api.ts`, `pages`, `components`, `hooks`.
- Requests vía `shared/api/http.ts` (Axios con baseURL, interceptors de auth y manejo de errores estándar).
- React Query para caching: keys por recurso (`['empresas', params]`, `['servicios', empresaId]`).
- Formularios con RHF + Zod schemas; mensajes de error amigables.
- Paginación/filtrado por query params (sin estado global innecesario).
- Componentes puros y pequeños; páginas orquestan.

## Seguridad/Autenticación (Fase 1)

- Login básico (HTTP Basic para MVP):
    - Guardar credenciales en memoria (Context) + setear `Authorization` en Axios.
    - Proteger rutas backoffice con `PrivateRoute` (verificar `AuthContext.isAuthenticated`).
- Roles UI:
    - Renderizado condicional por `rol` para acciones/botones (ej.: `EMPLEADO` no puede editar empresa).
- Futuro: JWT + Refresh tokens (Fase 2 de seguridad front).

## Integración con API actual

- Base URL: `http://localhost:8080`
- Headers: `Content-Type: application/json` y, en backoffice, `Authorization: Basic <base64(email:password)>`.
- Paginación: `page` (0-based) y `size`.

### Público (sin auth)

- `GET /api/public/empresas?categoriaId=&page=&size=` → `Empresa[]`
- `GET /api/public/empresas/{empresaId}/servicios?soloActivos=&page=&size=` → `Servicio[]`
- `POST /api/public/turnos` → crea `Turno`

### Auth (MVP)

- `POST /api/auth/register` → `Usuario`
- `POST /api/auth/login` → `{ id, email, rol }` → usar Basic Auth para backoffice

### Backoffice (requiere auth básica)

- Empresas: `GET/POST/PUT/PATCH /api/empresas`
- Empleados: `GET/POST/PUT/DELETE /api/empresas/{id}/empleados`
- Servicios: `GET/POST/PUT/DELETE /api/empresas/{id}/servicios`
- Disponibilidad: `GET/POST/DELETE /api/empleados/{id}/disponibilidad`
- Turnos internos: `POST /api/turnos`, `POST /api/turnos/{id}/aprobar|cancelar|completar`

## Estado actual

- **Fase A – Setup y base común**
    - Crear proyecto Vite + React + TS: [DONE]
    - ESLint + Prettier + Husky + lint-staged: [DONE]
    - Tailwind/tema base: [DONE]
    - Axios + interceptors; React Query Provider; Router + layouts: [DONE]
    - Componentes base (Table, Pagination, Loader, ErrorBoundary): [DONE]
- **Fase B – Público (Catálogo + Reserva)**
    - Páginas y APIs de catálogo/reserva con validaciones: [DONE]
- **Fase C – Auth (MVP)**
    - Auth context básico: [DONE]
    - Login/Registro + PrivateRoute + persistencia: [DONE]
- **Fase D – Backoffice CRUDs**
    - Empresas: [DONE]
    - Empleados: [DONE]
    - Servicios: [DONE]
    - Disponibilidad: [DONE]
    - Turnos: [DONE]
- **Fase E – UX/Calidad**
    - Estados vacíos, skeletons, toasts globales.
    - Accesibilidad.
    - i18n (si aplica) y tema.
    - Performance: Lighthouse y code splitting por rutas.
    - Testing por feature (MSW, Vitest, React Testing Library).

## Alineación con RFC – Frontend Turnero Web (v1.0)

- **Roles del RFC** (Cliente, Empresa/Admin, Empleado, SuperAdmin)
    - Cliente ↔ Módulo público (ya implementado: catálogo, servicios, reserva)
    - Empresa/Empleado ↔ Backoffice empresa (parcial: empresas, empleados, servicios, disponibilidad; pendiente: turnos con calendario, configuración)
    - SuperAdmin ↔ Backoffice global (pendiente)

- **Casos de uso mapeados**
    - UC-01 Explorar empresas/servicios → `/`, `/empresas`, `/empresas/:id/servicios` [DONE]
    - UC-02 Reserva anónima → `/reserva` [DONE]; calendario público [PENDIENTE]
    - UC-03 Validación de teléfono → `/validar` [PENDIENTE] (requiere API de verificación)
    - UC-04 Mis turnos (cliente) → `/mis-turnos` [PENDIENTE]
    - UC-05 Registro/Login (OAuth2) → MVP con Basic [DONE]; OAuth2/JWT [PENDIENTE]
    - UC-06 Alta de empresa → `/superadmin/empresas/new` [DONE]; registro dedicado `/empresa/registro` [PENDIENTE]
    - UC-07 Empleados + Horarios → `/backoffice/empleados/:empresaId`, `/backoffice/disponibilidad/:empleadoId` [DONE]
    - UC-08 Servicios → `/backoffice/servicios/:empresaId` [DONE]
    - UC-09 Configuración reglas → `/backoffice/empresa|configuracion` [PENDIENTE]
    - UC-10 Gestión de turnos (calendario/acciones) → `/backoffice/turnos` [DONE]
    - UC-11 Dashboard empresa → `/backoffice/dashboard` [PENDIENTE]
    - UC-12 SuperAdmin (global) → `/superadmin/*` [PARCIAL] (Empresas DONE; categorías/usuarios/auditoría PENDIENTE)

- **Gaps/pendientes según RFC**
    - Autenticación: OAuth 2.0 + JWT/refresh (reemplazar MVP Basic Auth) [PENDIENTE]
    - Validación telefónica: UI `/validar` + integración proveedor (SMS/WhatsApp) [PENDIENTE]
    - Módulo cliente: EmpresaDetalle/ServicioDetalle con calendario público de turnos [PENDIENTE]
    - Módulo cliente: Mis turnos (ver/cancelar/reprogramar) [PENDIENTE]
    - Backoffice: Turnos (listado/calendario y acciones aprobar/cancelar/completar) [PENDIENTE]

- Público
    - `/` → Home
    - `/empresas` → Listado de empresas (filtros por `categoriaId`, paginación `page`,`size`)
    - `/empresas/:empresaId/servicios` → Servicios de la empresa (filtro `soloActivos`, paginación)
    - `/reserva` → Reserva anónima
    - `/login` y `/register`
    - `/validar` → Validación de teléfono (envío y confirmación de código)
    - `/mis-turnos` → Turnos del cliente (listar/cancelar/reprogramar)
- Backoffice (privadas)
    - `/backoffice/dashboard`
    - `/backoffice/turnos` → Listado/acciones
    - `/backoffice/turnos/new` → Crear turno
    - `/backoffice/turnos/calendario` → Calendario semanal
    - `/backoffice/servicios/:empresaId` → Listado por empresa
    - `/backoffice/servicios/:empresaId/new` y `/backoffice/servicios/:empresaId/:id`
    - `/backoffice/empleados/:empresaId` → Listado por empresa
    - `/backoffice/empleados/:empresaId/new` y `/backoffice/empleados/:empresaId/:id`
    - `/backoffice/disponibilidad/:empleadoId` → Gestión de disponibilidad
    - `/backoffice/empresas` → Gestión básica (listado/edición)
    - `/backoffice/empresas/new` y `/backoffice/empresas/:id` (uso interno empresa). Alta global se hace en SuperAdmin.
    - `/backoffice/configuracion` → Configuración de empresa
- SuperAdmin (privadas)
    - `/superadmin/empresas` → CRUD global de empresas (alta incluida)
    - `/superadmin/empresas/new` y `/superadmin/empresas/:id`
    - (próximo) `/superadmin/categorias`, `/superadmin/usuarios`, `/superadmin/auditoria`
    - Archivos: `features/public/empresas/api.ts`, `pages/EmpresasPublicList.tsx`, `hooks/useEmpresas.ts`
    - Éxito: render tabla/lista + paginación; Error: toast estándar; Vacío: estado vacío
- Público/Servicios por empresa
    - Endpoint: `GET /api/public/empresas/{empresaId}/servicios?soloActivos=&page=&size=`
    - Key: `['servicios-publicos', empresaId, { soloActivos, page, size }]`
- Reserva anónima
    - Endpoint: `POST /api/public/turnos`
    - Archivos: `features/public/turnos/api.ts`, `pages/ReservaAnonimaPage.tsx`, `components/ReservaForm.tsx`
    - Form: RHF + Zod con `TurnoCreateRequest` y feedback de éxito/error
- Auth (MVP)
    - Login: `POST /api/auth/login` → setear Basic Auth con `setBasicAuth`
    - Register: `POST /api/auth/register` (opcional)
    - Archivos: `features/auth/api.ts`, `hooks.ts (useLogin/useRegister)`, `components/LoginForm.tsx`
    - Guard: `PrivateRoute` usando `AuthProvider`
- Backoffice/Empresas
    - Endpoints: `GET/POST/PUT/PATCH /api/empresas`
    - Keys: `['empresas-admin', params]`, `['empresa', id]`
    - Archivos: `features/backoffice/empresas/api.ts`, `pages/EmpresaListPage.tsx`, `pages/EmpresaEditPage.tsx`
- Backoffice/Servicios
    - Endpoints: `GET/POST/PUT/DELETE /api/empresas/{empresaId}/servicios`
    - Keys: `['servicios', empresaId, params]`, `['servicio', id]`
    - Archivos: `features/backoffice/servicios/api.ts`, `pages/ServicioListPage.tsx`, `pages/ServicioEditPage.tsx`
- Backoffice/Empleados
    - Endpoints: `GET/POST/PUT/DELETE /api/empresas/{empresaId}/empleados`
    - Keys: `['empleados', empresaId, params]`, `['empleado', id]`
    - Archivos: `features/backoffice/empleados/api.ts`, `pages/EmpleadoListPage.tsx`, `pages/EmpleadoEditPage.tsx`
- Backoffice/Disponibilidad
    - Endpoints: `GET/POST/DELETE /api/empleados/{empleadoId}/disponibilidad`
    - Keys: `['disponibilidad', empleadoId]`
    - Archivos: `features/backoffice/disponibilidad/api.ts`, `pages/DisponibilidadListPage.tsx`
- Backoffice/Turnos
    - Endpoints: `POST /api/turnos`, `POST /api/turnos/{id}/aprobar|cancelar|completar`
    - Keys: `['turnos', params]`, `['turno', id]`
    - Archivos: `features/backoffice/turnos/api.ts`, `pages/TurnoListPage.tsx`

## Testing por feature

- MSW handlers por recurso (empresas, servicios, empleados, disponibilidad, turnos)
- Unit: hooks (Query + RHF + utils)
- Integration: páginas con navegación, estados (loading/error/vacío), y flujos felices/errores
- Cobertura básica con `@vitest/coverage-v8`

## Checklist Fase A (pendientes)

- Instalar dependencias (`npm install`) y levantar `npm run dev`
- Configurar Husky + lint-staged (`prepare`, hooks `pre-commit`)
- Agregar `.env.example`
- Confirmar naming y paths finales de `features/*` al iniciar Fase B

## Roadmap por fases

### Fase A – Setup y base común
- Crear proyecto Vite + React + TS.
- ESLint + Prettier + Husky + lint-staged.
- Tailwind/Chakra + tema base.
- Axios + interceptors; React Query Provider; Router + layouts.
- Componentes base: Table, Pagination, Loader, ErrorBoundary.

### Fase B – Público (Catálogo + Reserva)
- Listado de empresas visibles con filtros por categoría y paginación.
- Servicios de empresa (solo activos) con paginación.
- Reserva anónima (formulario + validaciones + feedback).
- Tests con MSW.

### Fase C – Auth (MVP)
- Login y (opcional) registro básico.
- Guard de rutas privadas usando contexto de auth.
- Persistencia temporal de sesión (memory/sessionStorage).

### Fase D – Backoffice CRUDs
- Empresas: listado con filtros/paginación, crear/editar, activar.
- Empleados: listado y CRUD; filtro activo.
- Servicios: listado y CRUD; filtro activo.
- Disponibilidad: listado y altas/bajas simples.
- Turnos: listado y acciones (aprobar/cancelar/completar).
- Tests de integración con MSW.

### Fase E – UX/Calidad
- Estados vacíos, skeletons, toasts globales.
- Accesibilidad.
- i18n (si aplica) y tema.
- Performance: Lighthouse y code splitting por rutas.

## Buenas prácticas

- Mantener controllers del backend como fuente de verdad de contratos; definir tipos en `shared/types` o generar desde OpenAPI.
- Evitar estado global indiscriminado; preferir Query string + React Query + estado local.
- Normalizar errores en Axios (interceptor) y mostrar toasts legibles.
- Formularios: controlar touched/dirty, feedback en tiempo real con Zod.
- Reutilizar componentes de tabla/paginación/filtros.

## Scripts (package.json sugerido)

```json
{
  "scripts": {
    "dev": "vite",
    "build": "tsc -b && vite build",
    "preview": "vite preview",
    "lint": "eslint src --ext .ts,.tsx",
    "test": "vitest"
  }
}
```

## CI/CD (resumen)

- Ejecutar `lint` y `test` en cada PR.
- Build preview (Vercel/Netlify) o GitHub Pages para QA.

## Entregables por fase

- Fase A: repo inicial con scaffolding, lint/format, providers e infra de datos.
- Fase B: catálogo y reserva anónima funcionales.
- Fase C: login básico y protección de backoffice.
- Fase D: CRUDs completos en backoffice con paginación/filtros.
- Fase E: pulido UX, accesibilidad y performance.
