# Frontend Roadmap (React + Vite)

Guía de roadmap, arquitectura y buenas prácticas para implementar el Frontend de Fixa (público y backoffice) con React + Vite.

## Objetivos

- Público: catálogo de empresas y servicios, reserva anónima de turnos.
- Backoffice: administración de empresa, empleados, servicios, disponibilidades y turnos.
- Codebase moderna, predecible, con tests, y CI-ready.

## Stack técnico

- Build: Vite + React 18 + TypeScript
- Router: React Router v6.22+
- Datos: React Query (TanStack Query) para fetching/caching; Zustand o Redux Toolkit para UI/shared state simple.
- UI: Tailwind CSS o Chakra UI (elegir 1). Icons: Lucide o Heroicons.
- Formularios/validación: React Hook Form + Zod/Yup
- HTTP: Axios (con interceptors) o fetch + ky
- Env: Vite env vars (`import.meta.env`)
- Tests: Vitest + React Testing Library + MSW (mocks de API)
- Lint/format: ESLint (Airbnb + React + TS), Prettier, Husky + lint-staged
- i18n: react-i18next (opcional MVP)

## Estructura del proyecto (scaffolding)

```
frontend/
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
          api.ts              # /api/turnos crear/aprobar/cancelar/completar
          pages/
            TurnoListPage.tsx
    shared/
      api/
        http.ts               # axios instance con interceptors (auth, errores)
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

- Catálogo público:
  - `GET /api/public/empresas?categoriaId=&page=&size=`
  - `GET /api/public/empresas/{empresaId}/servicios?soloActivos=&page=&size=`
- Reserva anónima:
  - `POST /api/public/turnos` con payload `TurnoCreateRequest`.
- Backoffice (requiere auth básica):
  - Empresas: `GET/POST/PUT/PATCH /api/empresas`
  - Empleados: `GET/POST/PUT/DELETE /api/empresas/{id}/empleados`
  - Servicios: `GET/POST/PUT/DELETE /api/empresas/{id}/servicios`
  - Disponibilidad: `GET/POST/DELETE /api/empleados/{id}/disponibilidad`
  - Turnos internos: `POST /api/turnos`, `/api/turnos/{id}/aprobar|cancelar|completar`

## Roadmap por fases (Frontend)

### Fase A – Setup y base común
- Crear proyecto Vite + React + TS.
- ESLint + Prettier + Husky + lint-staged.
- Tailwind/Chakra + tema base (dark/light opcional).
- Axios + interceptors; React Query Provider; Router + layouts.
- Componentes base: Table, Pagination, Loader, ErrorBoundary.

### Fase B – Público (Catálogo + Reserva)
- Página listado de empresas visibles con filtros por categoría y paginación.
- Página servicios de empresa (solo activos) con paginación.
- Página de reserva anónima (formulario con validaciones + feedback de éxito/error).
- Tests: render de páginas, flujos felices y errores con MSW.

### Fase C – Auth (MVP)
- Página Login y (opcional) registro básico.
- Guard de rutas privadas (BackofficeLayout) usando contexto de auth.
- Persistencia temporal de sesión (memory o sessionStorage).

### Fase D – Backoffice CRUDs
- Empresas: listado con filtros/paginación, crear/editar, activar.
- Empleados: listado y CRUD; filtro activo.
- Servicios: listado y CRUD; filtro activo.
- Disponibilidad: listado y altas/bajas simples.
- Turnos: listado y acciones (aprobar/cancelar/completar); validaciones.
- Tests de integración de cada flujo con MSW.

### Fase E – UX/Calidad
- Estados vacíos, skeletons, toasts globales.
- Accesibilidad (aria-labels, focus management).
- i18n (si aplica), tema.
- Métricas de performance (Lighthouse), code splitting por rutas.

## Buenas prácticas

- Mantener controllers del backend como fuente de verdad de contratos; generar tipos TS desde OpenAPI (si se incorpora Swagger) o definir tipos compartidos en `shared/types`.
- Evitar estado global indiscriminado; preferir Query string + React Query + estado local.
- Normalizar errores en Axios (interceptor) y mostrar toasts legibles.
- Formularios: controlar touched/dirty, feedback en tiempo real con Zod.
- Reutilizar componentes de tabla/paginación/filtros.

## Scripts (package.json)

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

## Testing

- Configurar Vitest + RTL; MSW para simular endpoints.
- Unit: hooks y componentes puros.
- Integration: páginas con navegación, API mocks y aserciones de vista.

## CI/CD (resumen)

- Ejecutar `lint` y `test` en cada PR.
- Build preview (Vercel/Netlify) o GitHub Pages para QA.

## Entregables por fase

- Fase A: repo inicial con scaffolding, lint/format, providers e infra de datos.
- Fase B: catálogo y reserva anónima 100% funcionales (desktop-first; responsive básico).
- Fase C: login básico y protección de backoffice.
- Fase D: CRUDs completos en backoffice con paginación/filtros e interacción con reglas.
- Fase E: pulido UX, accesibilidad y performance.
