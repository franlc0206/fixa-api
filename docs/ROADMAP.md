# Turnero Web – Roadmap y Action Plan

Este documento guía la ejecución por fases del backend (Spring Boot) acorde al RFC. Incluye tareas, entregables, criterios de aceptación y dependencias.

## Principios

- Clean Architecture: `domain/`, `application/`, `infrastructure/`.
- Local (dev): Hibernate gestiona el esquema (`spring.jpa.hibernate.ddl-auto=update`).
- Más adelante: migraciones versionadas con Flyway para entornos estables.
- Pull requests pequeños, con tests y documentación breve (README o Swagger).

---

## Fase 0 – Scaffolding y Base Técnica (completada)

- Estructura por capas creada.
- Seguridad base (Spring Security) con endpoint `/health` público.
- MySQL local configurado en `application.yml`.
- Documentación inicial en `README.md`.

Criterios de aceptación
- Compila: `mvnw.cmd -DskipTests package`.
- App levanta y crea tablas automáticamente vía Hibernate.

---

## Fase 1 – Modelo y Persistencia (dev, con Hibernate)

Objetivo: tener el modelo mínimo funcional con tablas creadas automáticamente en MySQL.

Tareas
- Domain models: `Usuario`, `Empresa`, `Empleado`, `Servicio`, `Disponibilidad`, `Turno`, `VerificacionTelefono`.
- Entities JPA espejadas y relaciones con `@ManyToOne`, `@OneToMany` donde corresponda.
- Repositorios Spring Data: `Usuario`, `Empresa`, `Empleado`, `Servicio`, `Disponibilidad`, `Turno`, `VerificacionTelefono`.
- Mappers (domain ↔ persistence) por entidad.
- Consultas clave:
  - Disponibilidad por empleado y día.
  - Turnos por empleado y rango de fecha/hora.

Entregables
- Entities + Repos + Mappers implementados.
- Pruebas de integración mínimas para CRUD y queries clave.

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
  - Setear `estado` inicial: `pendiente_aprobacion` o `confirmado` según configuración de empresa.
- `AprobarTurnoUseCase`:
  - Validar transición de estado.
  - Confirmar y persistir.
- Endpoints REST (infrastructure/in/web):
  - POST `/api/turnos` (crear)
  - POST `/api/turnos/{id}/aprobar` (aprobar)
- DTOs request/response y validaciones `@Valid`.

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

## Fase 6 – Validación telefónica y Notificaciones

Objetivo: validar por SMS/WhatsApp y notificar confirmaciones.

Tareas
- Entidad `verificacion_telefono`.
- Endpoints para enviar/validar código.
- Adapter de mensajería (stub Twilio/WhatsApp Cloud API).

Criterios de aceptación
- Códigos con expiración.
- Estados de validación reflejados en `turno`.

---

## Fase 7 – SuperAdmin

Objetivo: administración global.

Tareas
- Endpoints para gestionar usuarios, empresas (alta/baja), visibilidad y auditorías mínimas.

Criterios de aceptación
- Acceso restringido al rol `superadmin`.

---

## Fase 8 – Hardening, Observabilidad, Calidad

Objetivo: robustez y calidad.

Tareas
- Manejo centralizado de errores (ControllerAdvice).
- Logs estructurados.
- Rate limiting básico.
- Tests: unidad (dominio), integración (repos), e2e mínimos.

Criterios de aceptación
- Cobertura mínima acordada.
- Alertas/logs útiles en errores.

---

## Fase 9 – Deploy y Entornos

Objetivo: llevar a prod/staging.

Tareas
- Perfiles `dev`, `staging`, `prod` con `application-*.yml`.
- Volver a habilitar Flyway (migraciones V1..Vn completas) para esquema estable.
- Deploy en Railway/Render. Variables de entorno documentadas.

Criterios de aceptación
- Arranque limpio con migraciones.
- Documentación de despliegue actualizada.

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
