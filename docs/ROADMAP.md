# Turnero Web – Roadmap y Action Plan

Este documento guía la ejecución por fases del backend (Spring Boot) acorde al RFC. Incluye tareas, entregables, criterios de aceptación y dependencias.

## Principios

- Clean Architecture: `domain/`, `application/`, `infrastructure/`.
- Local (dev): Hibernate gestiona el esquema (`spring.jpa.hibernate.ddl-auto=update`).
- Más adelante: migraciones versionadas con Flyway para entornos estables.
- Pull requests pequeños, con tests y documentación breve (README o Swagger).

---

## Fase 0 – Preparación del entorno

Objetivo: Tener el proyecto inicial listo para desarrollo.

- [DONE] Crear proyecto Spring Boot con:
  - `spring-boot-starter-web`
  - `spring-boot-starter-data-jpa`
  - `spring-boot-starter-validation`
  - `spring-boot-starter-security`
- [DONE] Configurar base de datos MySQL (dev/local).
- [DONE] Configurar `hibernate.ddl-auto=update` para desarrollo.
- [PENDING] Configurar perfiles (`application-dev.yml`, `application-prod.yml`).
- [DONE] Documentación inicial en `README.md`.

Criterios de aceptación
- [DONE] Compila: `mvnw.cmd -DskipTests package`.
- [DONE] App levanta y crea tablas automáticamente vía Hibernate.

---

## Fase 1 – Base de datos y entidades (Domain Layer)

Objetivo: Modelar la lógica principal del negocio en entidades y relaciones.

Tablas (alias RFC ↔ implementación):
- [DONE] Usuario (`usuario`).
- [DONE] Empresa (`empresa`).
- [DONE] Categoria (`categoria`) para empresa/servicio.
- [DONE] Empleado (`empleado`).
- [DONE] Servicio (`servicio`).
- [DONE] HorarioEmpleado = Disponibilidad (`disponibilidad`).
- [DONE] Reserva = Turno (`turno`) + `TurnoEstado {PENDIENTE, CONFIRMADO, CANCELADO, COMPLETADO}`.
- [DONE] ConfigRegla (`config_regla`).
- [DONE] Notificacion (`notificacion`).
- [DONE] Auditoria (`auditoria`).
- [DONE] BloqueoHorario (`bloqueo_horario`).
- [DONE] VerificacionTelefono (`verificacion_telefono`).

Relaciones (resumen):
- Empresa → Empleados → Disponibilidad.
- Empresa → Servicios.
- Servicio → (opcional) Categoria; Empresa → (opcional) Categoria.
- Turno ↔ Cliente (Usuario) ↔ Empleado ↔ Servicio ↔ Empresa.

Repositorios Spring Data
- [DONE] `Usuario`, `Empresa`, `Empleado`, `Servicio`, `Disponibilidad`, `Turno`, `VerificacionTelefono`.
- [DONE] `Categoria`, `BloqueoHorario`, `ConfigRegla`, `Notificacion`, `Auditoria`.

Consultas clave (mínimo):
- [DONE] Disponibilidad por empleado y día (`findByEmpleado_IdAndDiaSemana`).
- [DONE] Turnos por empleado y rango de fecha/hora (`findByEmpleado_IdAndFechaHoraInicioBetween`).

Entregables
- [DONE] Entities + Repos implementados.
- [PENDING] Mappers domain ↔ entity para nuevas entidades.
- [PENDING] Pruebas de integración mínimas para CRUD y queries clave.

Criterios de aceptación
- Crear/leer entidades sin errores.
- Queries devuelven datos esperados con dataset mínimo.

---

## Fase 2 – Núcleo de Turnos

Objetivo: flujo de creación y aprobación de turnos con validaciones base.

Tareas
- `CrearTurnoUseCase` (domain/application):
  - Validar duración por servicio.
  - Validar solapamientos para un `empleado`.
  - Calcular `fecha_hora_fin`.
  - Setear `estado` inicial: `PENDIENTE` o `CONFIRMADO` según `ConfigRegla`/empresa.
- `AprobarTurnoUseCase`:
  - Validar transición de estado.
  - Confirmar y persistir.
- Endpoints REST (infrastructure → in/web):
  - POST `/api/turnos` (crear)
  - POST `/api/turnos/{id}/aprobar` (aprobar)
- DTOs request/response y validaciones `@Valid`.

Estado
- [DONE] Implementación de `CrearTurnoUseCase` y `AprobarTurnoUseCase`.
- [DONE] Endpoints: `POST /api/turnos`, `POST /api/turnos/{id}/aprobar`.
- [DONE] DTO `TurnoCreateRequest` con `@Valid`.
- [DONE] Manejo de errores global `GlobalExceptionHandler` con `ApiException`.
- [DONE] Migración a Hexagonal: `TurnoRepositoryPort` + `TurnoRepositoryAdapter` + `TurnoMapper`. `TurnoCommandService` usa puertos.
- [PENDING] Endpoints y reglas de transición para `CANCELADO` y `COMPLETADO`.
- [PENDING] Reglas adicionales vía `ConfigRegla` (máx turnos/día/semana, etc.).
- [PENDING] Tests de integración (repos, endpoints, validaciones).

Criterios de aceptación
- No se permiten solapamientos.
- Transiciones de estado correctas.
- Respuestas REST con códigos HTTP apropiados y mensajes de error claros.

---

## Fase 3 – Backoffice Empresa

Objetivo: CRUDs de servicios, empleados y disponibilidad.

Tareas
- Endpoints REST:
  - `POST/GET/PUT/DELETE /api/empresas/{id}/servicios`
  - `POST/GET/PUT/DELETE /api/empresas/{id}/empleados`
  - `POST/GET/PUT/DELETE /api/empleados/{id}/disponibilidad`
- Reglas de dominio:
  - `servicio.duracion_minutos` > 0.
  - `disponibilidad`: hora_inicio < hora_fin, enum día válido.
  - `servicio.requiere_espacio_libre` aplicado al planificador (si corresponde en F2/F4).
- DTOs y validaciones `@Valid` (request/response) y paginación básica.

Estado
- [DONE] Refactor hexagonal de Empresa, Empleado, Servicio y Disponibilidad:
  - Controllers usan Services (application) y modelos de dominio.
  - Services usan Puertos de dominio.
  - Adapters JPA implementan puertos con mappers.
- [PENDING] Tests de integración básicos para CRUDs y validaciones.

Criterios de aceptación
- CRUDs con validaciones y errores amigables.
- Paginación básica y filtros por activo/empresa.

---

## Fase 4 – Público: Catálogo + Reserva anónima

Objetivo: publicar listado de empresas y servicios y permitir reserva sin login.

Tareas
- Endpoints públicos:
  - `GET /api/public/empresas` (solo visibles)
  - `GET /api/public/empresas/{id}/servicios`
- Reserva anónima:
  - `POST /api/public/turnos` con datos de cliente anónimo.
  - Si la empresa requiere validación telefónica: marcar `requiere_validacion=true` y estado acorde.

Criterios de aceptación
- Flujo anónimo crea turnos con los flags correctos.
- Catálogo público solo muestra empresas visibles.

---

## Fase 5 – Seguridad Fase 1 (login clásico)

Objetivo: proteger backoffice y asignar roles básicos.

Tareas
- `AuthService` con `register()` / `login()`.
- `BCryptPasswordEncoder` para passwords.
- Filtros/Reglas por rol: `superadmin`, `empresa`, `empleado`, `cliente`.
- Proteger endpoints de backoffice y de turnos internos.

Criterios de aceptación
- Login con email/password.
- Acceso según rol a endpoints.

---

## Fase 6 – Scheduler y Mensajería (Infrastructure → scheduler / messaging)

Scheduler diario/semanal:
- Recordatorios de turnos
- Notificaciones de aprobación
- Penalización de no-shows

Mensajería:
- Integración Twilio para SMS
- WhatsApp Cloud API
- Email (SMTP / SendGrid / AWS SES)

---

## Fase 7 – Auditoría y métricas (Infrastructure + Application)

Registrar todos los eventos relevantes (Auditoria, LogEventos)

Métricas:
- Turnos por empresa / empleado
- Turnos pendientes / confirmados / cancelados
- Clientes activos
- Tiempos de respuesta para confirmación

---

## Fase 8 – Testing y QA

Unit tests:
- Repositorios, use cases, validaciones

Integration tests:
- Endpoints REST
- Scheduler y mensajería

QA manual:
- Pruebas de flujo completo: registro → reserva → confirmación → recordatorio

---

## Fase 9 – Producción y despliegue

Configuración de profiles (dev/prod)
- CI/CD: build Maven, pruebas unitarias, despliegue automático
- Seguridad de base de datos y credenciales (Vault / env vars)

Monitorización:
- Logs centralizados (ELK / CloudWatch)
- Métricas de uso y alertas

---

## Checklists operativos

- DTOs y Validaciones
  - Requests/Responses definidos.
  - `@Valid` + mensajes claros.
- Endpoints REST
  - Código HTTP correcto.
  - Errores con body estandarizado.
- Persistencia
  - Relaciones y cascadas revisadas.
  - Índices en claves de búsqueda.
- Seguridad
  - Rutas públicas y protegidas claras.
  - Roles probados.
- DX/Docs
  - README actualizado.
  - Ejemplos curl o Swagger.

---

## Cómo ejecutar local

- MySQL en localhost con DB `turnero`.
- `application.yml` (dev): `ddl-auto=update`, Flyway deshabilitado.
- Compilar: `mvnw.cmd -DskipTests package`
- Ejecutar: `mvnw.cmd spring-boot:run`
- Health: `GET http://localhost:8080/health`

---

## Transición a Flyway (más adelante)

1) Congelar el esquema estable generado por Hibernate.
2) Exportar DDL y crear `V1__init.sql` completo (todas las tablas, índices, FKs).
3) Habilitar `spring.flyway.enabled=true` y poner `ddl-auto=none`.
4) Validar en entornos limpios y luego promover.

---

## Checklist de avance (repo actual)

- [DONE] Fase 0: base técnica, MySQL, Hibernate update, build OK.
- [DONE] Fase 1: entidades y repos (incluye `categoria`, `bloqueo_horario`, `config_regla`, `notificacion`, `auditoria`).
- [PENDING] Fase 1: mappers domain ↔ entity y tests de integración.
- [PENDING] Fase 0: perfiles `application-*.yml` (dev/prod).
- [NEXT] Fase 2: casos de uso `CrearTurnoUseCase` y `AprobarTurnoUseCase`, endpoints `/api/turnos`.
