# Guía de Desarrollo

Esta guía resume buenas prácticas, reglas y el scaffolding del repo para contribuir de forma consistente.

## Principios

- **Arquitectura Hexagonal**
  - Domain (modelos + puertos) no depende de frameworks.
  - Application (use cases / servicios) solo conoce puertos del dominio.
  - Infrastructure implementa adapters in/out y mapea DTOs/Entities.
- **Single Responsibility**: cada clase con una responsabilidad clara.
- **DTOs vs Dominio**: los controllers exponen DTOs; los services trabajan con modelos de dominio.
- **Errores consistentes**: usar `ApiException` y `GlobalExceptionHandler`.
- **Validaciones**: `@Valid` en DTOs, y reglas de negocio en services/use cases.

## Estructura del código (scaffolding)

```
src/main/java/com/fixa/fixa_api/
  domain/
    model/             // POJOs de dominio
    repository/        // Puertos (interfaces)
  application/
    usecase/           // Interfaces de casos de uso
    service/           // Servicios de aplicación (implementan casos de uso)
  infrastructure/
    in/
      web/             // Controllers + DTOs + error handling
    out/
      persistence/
        entity/        // Entidades JPA
        repository/    // Spring Data JPA repos
        mapper/        // Mappers dominio <-> entidad
        adapter/       // Adapters que implementan puertos con JPA
    config/            // Config técnica (Security, etc.)
```

## Flujo para agregar una feature

1. **Dominio**: definir/ajustar modelo(s) en `domain/model` y puerto(s) en `domain/repository`.
2. **Aplicación**: crear use case(s) en `application/usecase` y su service en `application/service`.
3. **Infra Out**: crear adapter + mapper + usar repos JPA para implementar el puerto.
4. **Infra In**: exponer controller/DTOs que mapean a modelos de dominio y llaman al service.
5. **Errores/validación**: usar `ApiException`, `GlobalExceptionHandler` y `@Valid`.
6. **Tests**: agregar tests de integración (Spring Boot Test) que cubran el flujo completo.

## Reglas y buenas prácticas

- **No acoples controllers a JPA**: siempre pasar por services y puertos.
- **Mappers dedicados** por agregado para dominio ↔ entidad.
- **Transacciones**: delimitar con `@Transactional` en services de aplicación.
- **Nombres claros**: `*RepositoryPort`, `*RepositoryAdapter`, `*Service`, `*Controller`.
- **Commits**: mensajes tipo Conventional Commits (feat, fix, refactor, docs, chore).
- **Validaciones de negocio** en services (no en controllers).
- **Paginación/Filtros**: en MVP se permite en memoria; idealmente mover al puerto/repositorio con queries.

## Casos de uso de Turnos (resumen)

- Crear: valida duración, solapamiento, anticipación (mín/max), límites día/semana; calcula fin; fija estado inicial; persiste.
- Aprobar: PENDIENTE → CONFIRMADO.
- Cancelar: PENDIENTE/CONFIRMADO → CANCELADO (opcional motivo).
- Completar: CONFIRMADO → COMPLETADO.
- Cliente autenticado puede consultar su historial mediante `GET /api/me/turnos` (opcional `estado`, `page`, `size`).

## Estándares de errores

- Excepciones de negocio: `ApiException(HttpStatus, message)`.
- Respuestas de error: `ApiError` con `status`, `message`, `path`, `timestamp`, `details`.

## Testing (recomendado)

- **Spring Boot Test** + MockMvc/WebTestClient.
- Tests de integración deben cubrir:
  - Turnos: crear OK, solape 409, `max_turnos_por_dia` 409, `max_turnos_por_semana` 409, min/max anticipación 409, transiciones.
  - Backoffice: CRUD de Empresa/Empleado/Servicio/Disponibilidad, filtros y paginación.
- Si es posible, usar `@Sql` o `TestEntityManager` para preparar datos.

## Seguridad

- `SecurityConfig` básico para MVP. Endurecer en fases siguientes.

### Modelo de Roles y permisos

- **Rol global (`Usuario.rol`)**
  - Valores esperados: `SUPERADMIN`, `CLIENTE` (históricamente `EMPRESA`, `EMPLEADO`, pero se recomienda no usarlos para nuevas features).
  - Se mapea a authorities Spring:
    - `SUPERADMIN` → `ROLE_SUPERADMIN`.
    - Cualquier otro → `ROLE_CLIENTE`.
  - Uso principal:
    - `SUPERADMIN` puede acceder a `/api/superadmin/**` y crear/editar empresas globalmente.
    - `CLIENTE` (incluye usuarios de backoffice) se combina con el rol por empresa.

- **Rol por empresa (`UsuarioEmpresa.rolEmpresa`)**
  - Representa el rol del usuario dentro de una empresa concreta (multi-tenant).
  - Valores recomendados: `OWNER`, `MANAGER`, `STAFF`.
    - `OWNER`: dueño/administrador principal de la empresa.
    - `MANAGER`: gestiona agenda, empleados y servicios, pero no necesariamente operaciones globales.
    - `STAFF`: empleado operativo (acceso limitado a agenda/turnos y datos básicos).
  - Autorización típica en servicios de backoffice:
    - Se valida primero pertenencia: `usuarioEmpresaPort.existsByUsuarioAndEmpresa(usuarioId, empresaId)`.
    - A futuro se puede refinar según `rolEmpresa` (ej: solo `OWNER`/`MANAGER` pueden crear empleados/servicios).

- **Patrones de autorización sugeridos (guía)**
  - Endpoints `superadmin`:
    - Siempre requerir `hasRole("SUPERADMIN")` y operar a nivel global.
  - Endpoints de backoffice por empresa (`/api/empresas/{empresaId}/...`):
    - Validar autenticación (JWT) + pertenencia a la empresa vía `UsuarioEmpresa`.
    - Si se necesita granularidad adicional:
      - **Lectura** de agenda/turnos/empleados: `OWNER`/`MANAGER`/`STAFF`.
      - **Alta/edición** de empleados/servicios/disponibilidad: `OWNER`/`MANAGER`.
      - **Configuración crítica** (ej: borrar empresa, cambiar flags sensibles): solo `OWNER` o `SUPERADMIN`.

- **Buenas prácticas**
  - No exponer cambios de `Usuario.rol` en endpoints públicos.
  - Gestionar `UsuarioEmpresa.rolEmpresa` solo desde flujos de backoffice/superadmin.
  - Documentar siempre en los nuevos endpoints:
    - Qué rol global y qué rol por empresa se requiere para usarlos.

## Commits y PRs

- **Conventional Commits**.
- PRs pequeños y enfocados; incluir descripción, lista de cambios y pasos de prueba.

## Scripts y comandos

- Compilar: `mvnw.cmd -DskipTests package` (Windows) / `./mvnw -DskipTests package`
- Ejecutar: `mvnw.cmd spring-boot:run` / `./mvnw spring-boot:run`

