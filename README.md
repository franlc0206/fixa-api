# Turnero API – Scaffolding (Clean/Hexagonal)

Este proyecto usa una estructura por capas: domain, application e infrastructure. Los adapters (in/out) viven dentro de `infrastructure`.

## Capas

- domain/
  - domain/model/: modelos de dominio puros (sin anotaciones de frameworks)
  - domain/repository/: puertos (interfaces) que define el dominio para persistencia u otras IO
- application/
  - application/usecase/: casos de uso (orquestación de reglas) que dependen de puertos del dominio
- infrastructure/
  - infrastructure/in/web/: adapters de entrada (REST Controllers)
  - infrastructure/in/messaging/: adapters de entrada (mensajería, webhooks)
  - infrastructure/in/scheduler/: jobs programados
  - infrastructure/out/persistence/: adapters de salida (JPA/Hibernate, entidades, repos)
    - entity/: entidades JPA
    - repository/: repositorios Spring Data JPA
    - mapper/: mappers entre dominio y persistencia
  - infrastructure/out/sms/ | out/email/: otros adapters externos
  - infrastructure/config/: configuración técnica (Security, Swagger, Beans)

## Rutas clave actuales

- `src/main/java/com/fixa/fixa_api/domain/model/` → Usuario, Empresa, Empleado, Servicio, Turno
- `src/main/java/com/fixa/fixa_api/domain/repository/` → UsuarioRepositoryPort, EmpresaRepositoryPort, TurnoRepositoryPort
- `src/main/java/com/fixa/fixa_api/application/usecase/` → CrearTurnoUseCase, AprobarTurnoUseCase
- `src/main/java/com/fixa/fixa_api/infrastructure/in/web/` → HealthController (endpoint `/health`)
- `src/main/java/com/fixa/fixa_api/infrastructure/out/persistence/` → UsuarioEntity, UsuarioJpaRepository, UsuarioMapper
- `src/main/java/com/fixa/fixa_api/infrastructure/config/` → SecurityConfig

## Seguridad (MVP Fase 1)

- Spring Security configurado en `SecurityConfig`.
- Por ahora: `"/health"` es público y el resto `permitAll()` para no bloquear el MVP.
- `PasswordEncoder`: `BCryptPasswordEncoder` (ya definido como Bean).

## Base de datos

- MySQL configurado en `src/main/resources/application.yml`:
  - URL: `jdbc:mysql://localhost:3306/turnero?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC`
  - Usuario/Password: `root`/`root` (cambiar según tu entorno)
  - Dialecto: `org.hibernate.dialect.MySQL8Dialect`
- Migraciones con Flyway en `src/main/resources/db/migration/`:
  - `V1__init.sql`: crea tabla `usuario` (parcial, según RFC). Agregar tablas restantes en `V2__...`.

## Flujo recomendado para nuevas features

1. Definir/ajustar modelos de dominio en `domain/model` y puertos en `domain/repository`.
2. Implementar casos de uso en `application/usecase` (y servicios de aplicación si hace falta).
3. Implementar adapters en `infrastructure`:
   - Entrada: controllers REST en `infrastructure/in/web` (mapean DTOs ⇄ dominio).
   - Salida: persistencia en `infrastructure/out/persistence` (JPA entities/repos + mappers).
4. Agregar migraciones Flyway (V2, V3, ...) para cambios de esquema.

## Comandos útiles

- Compilar: `./mvnw -DskipTests package` (Linux/Mac) o `mvnw.cmd -DskipTests package` (Windows)
- Levantar app: `./mvnw spring-boot:run` o `mvnw.cmd spring-boot:run`

## Pendientes inmediatos (según RFC)

- Agregar entidades y repos para: Empresa, Empleado, Servicio, Turno, Disponibilidad, VerificacionTelefono.
- Implementar casos de uso: CrearTurno, AprobarTurno, y endpoints correspondientes.
- Endurecer seguridad: proteger endpoints, Auth (Fase 1), y luego OAuth2 (Fase 2).
