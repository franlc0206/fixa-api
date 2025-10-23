# Fixa API – Backend (Clean/Hexagonal)

API para gestión de turnos (empresas, empleados, servicios, disponibilidades y turnos) basada en arquitectura hexagonal.

## Arquitectura

- domain/
  - `domain/model/`: modelos de dominio puros
  - `domain/repository/`: puertos del dominio (interfaces)
- application/
  - `application/usecase/`: contratos de casos de uso
  - `application/service/`: orquestación/servicios de aplicación (usan puertos del dominio)
- infrastructure/
  - `in/web/`: adapters de entrada (REST Controllers, DTOs)
  - `out/persistence/`: adapters de salida (JPA entities/repos + mappers + adapters)
  - `infrastructure/config/`: beans técnicos (security, etc.)

## Funcionalidad clave (estado actual)

- Backoffice (hexagonal): Empresa, Empleado, Servicio, Disponibilidad
  - CRUDs vía controllers → services → ports → adapters JPA
  - Filtros: `activo`, `categoriaId`, `visibles` (empresas)
  - Paginación básica: `page`, `size` (empresas, empleados, servicios)
- Turnos (núcleo):
  - Casos de uso: crear, aprobar, cancelar, completar
  - Reglas: sin solape, calcula fin, estado inicial por empresa, `max_turnos_por_dia`, `max_turnos_por_semana`, `min_anticipacion_minutos`, `max_anticipacion_dias`
  - Endpoints: `POST /api/turnos`, `/api/turnos/{id}/aprobar`, `/api/turnos/{id}/cancelar`, `/api/turnos/{id}/completar`
  - Errores: `ApiException` + `GlobalExceptionHandler`

## Ejecutar

- Requisitos: JDK 21, MySQL/MariaDB local
- Config por defecto en `src/main/resources/application.yml` (ajustar credenciales)
- Compilar: `mvnw.cmd -DskipTests package` (Windows) / `./mvnw -DskipTests package`
- Run: `mvnw.cmd spring-boot:run` / `./mvnw spring-boot:run`

## Endpoints de ejemplo

- Empresas: `GET /api/empresas?visibles=&activo=&categoriaId=&page=&size=`
- Empleados: `GET /api/empresas/{empresaId}/empleados?activo=&page=&size=`
- Servicios: `GET /api/empresas/{empresaId}/servicios?activo=&page=&size=`
- Turnos: `POST /api/turnos`, `POST /api/turnos/{id}/aprobar|cancelar|completar`

## Desarrollo

- Guía completa: ver `docs/DEVELOPMENT.md` (reglas, buenas prácticas, scaffolding, commits, testing, ports/adapters, mappers, DTOs y errores).
- Roadmap y estado: ver `docs/ROADMAP.md`.

## Seguridad (MVP)

- `SecurityConfig` con configuración básica; endurecer en fases siguientes.

