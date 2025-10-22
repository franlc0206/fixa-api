# 🧾 RFC – Sistema de Turnos Online (Turnero Web)

Versión: 1.0  
Autor: Francisco López  
Fecha: Octubre 2025

---

## 1. Objetivo

Desarrollar una plataforma web de gestión de turnos flexible, moderna y escalable, que permita:

- A los usuarios finales (clientes) consultar servicios, disponibilidad y reservar turnos de manera rápida, incluso sin necesidad de registrarse.
- A las empresas (peluquerías, barberías, centros de estética, etc.) gestionar sus turnos, empleados, servicios, configuraciones, reservas y validaciones.
- A los administradores del sistema (SuperAdmin) administrar las empresas registradas, moderar contenidos y tener control sobre la operación global del sistema.

El sistema permitirá reservas configurables, validaciones opcionales por teléfono, gestión multiusuario y un entorno de administración completo.

---

## 2. Alcance

### Funcionalidades principales
- Registro de empresas, empleados y servicios.
- Configuración flexible de turnos (duración, horarios, validaciones, señas).
- Reservas públicas (sin login) o privadas (con login).
- Validación opcional por SMS o WhatsApp.
- Panel de administración (backoffice) para empresas.
- Panel de superadministración (SuperAdmin) global.
- Notificaciones (correo o WhatsApp) para confirmaciones o recordatorios.

### No incluido en esta versión (posible v2.0)
- Pasarela de pago integrada (MercadoPago, Stripe).
- Aplicación móvil nativa.
- Integración automática con Google Calendar.

---

## 3. Tipos de usuarios

| Rol | Descripción | Permisos principales |
|---|---|---|
| SuperAdmin | Control total del sistema | Alta/baja empresas, usuarios, auditoría global |
| Empresa (Admin empresa) | Dueño o responsable de una empresa registrada | Crear servicios, configurar horarios, gestionar empleados y turnos |
| Empleado | Persona que ofrece servicios dentro de una empresa | Consultar y gestionar turnos propios |
| Cliente registrado | Usuario con cuenta propia | Reservar, cancelar, modificar turnos |
| Cliente anónimo | Usuario sin cuenta | Reservar turnos rápidos con validación telefónica |

---

## 4. Modelo de datos (v3.0)

### Tabla: `usuario`
- id BIGINT PK
- nombre VARCHAR(100)
- apellido VARCHAR(100)
- email VARCHAR(150) UNIQUE
- telefono VARCHAR(30)
- password_hash VARCHAR(255)
- rol ENUM('superadmin','empresa','empleado','cliente')
- activo BOOLEAN

### Tabla: `empresa`
- id BIGINT PK
- fk_usuario_admin BIGINT FK → usuario.id
- nombre VARCHAR(150)
- descripcion TEXT
- direccion VARCHAR(255)
- telefono VARCHAR(30)
- categoria VARCHAR(100)
- permite_reservas_sin_usuario BOOLEAN
- requiere_validacion_telefono BOOLEAN
- mensaje_validacion_personalizado TEXT
- visibilidad_publica BOOLEAN

### Tabla: `empleado`
- id BIGINT PK
- fk_empresa BIGINT FK → empresa.id
- fk_usuario BIGINT FK → usuario.id (opcional)
- nombre VARCHAR(100)
- apellido VARCHAR(100)
- rol VARCHAR(100)
- activo BOOLEAN

### Tabla: `servicio`
- id BIGINT PK
- fk_empresa BIGINT FK → empresa.id
- nombre VARCHAR(150)
- descripcion TEXT
- duracion_minutos INT
- requiere_espacio_libre BOOLEAN
- costo DECIMAL(10,2)
- requiere_seña BOOLEAN
- activo BOOLEAN

### Tabla: `disponibilidad`
- id BIGINT PK
- fk_empleado BIGINT FK → empleado.id
- dia_semana ENUM('lunes','martes','miércoles','jueves','viernes','sábado','domingo')
- hora_inicio TIME
- hora_fin TIME

### Tabla: `turno`
- id BIGINT PK
- fk_servicio BIGINT FK → servicio.id
- fk_empleado BIGINT FK → empleado.id
- fk_empresa BIGINT FK → empresa.id
- fk_cliente BIGINT NULL FK → usuario.id
- cliente_nombre VARCHAR(100)
- cliente_apellido VARCHAR(100)
- cliente_telefono VARCHAR(30)
- cliente_dni VARCHAR(20) NULL
- cliente_email VARCHAR(150) NULL
- telefono_validado BOOLEAN
- fecha_hora_inicio DATETIME
- fecha_hora_fin DATETIME
- estado ENUM('pendiente_aprobacion','confirmado','cancelado','completado')
- requiere_validacion BOOLEAN
- observaciones TEXT

### Tabla: `verificacion_telefono`
- id BIGINT PK
- telefono VARCHAR(30)
- codigo VARCHAR(10)
- fecha_envio DATETIME
- fecha_expiracion DATETIME
- validado BOOLEAN
- canal ENUM('sms','whatsapp')
- fk_turno BIGINT NULL FK → turno.id

### Tabla: `seña` (opcional futuro)
- id BIGINT PK
- fk_turno BIGINT FK → turno.id
- monto DECIMAL(10,2)
- estado ENUM('pendiente','pagada','devuelta')
- metodo ENUM('efectivo','mp','transferencia')

---

## 5. Casos de uso (resumen)

- UC-01: Registro de empresa
- UC-02: Alta de empleado
- UC-03: Publicación de servicios
- UC-04: Configuración de disponibilidad
- UC-05: Visualización pública
- UC-06: Solicitud de turno (registrado)
- UC-07: Solicitud de turno (anónimo)
- UC-08: Validación de número (SMS/WhatsApp)
- UC-09: Aprobación de turno
- UC-10: Gestión de turnos (cancelar/reprogramar)
- UC-11: Panel del SuperAdmin

---

## 6. Arquitectura técnica sugerida

- Frontend público: React
- Backoffice empresa + SuperAdmin: React
- Backend: Java Spring Boot
- Base de datos: MySQL (local dev) / (prod a definir)
- Mensajería/validación: Twilio / WhatsApp Cloud API
- Hosting: Render / Railway / AWS / DonWeb / Clever Cloud
- Escalabilidad: Arquitectura modular, API REST, futura GraphQL

Estructura de código (Clean/Hexagonal):
```
src/main/java/com/fixa/fixa_api/
  domain/
    model/
    repository/
  application/
    usecase/
  infrastructure/
    in/
      web/
      messaging/
      scheduler/
    out/
      persistence/
        entity/
        repository/
        mapper/
      sms/
      email/
    config/
```

---

## 7. Roadmap de Seguridad – Registro y Login con OAuth2.0

### Fase 1: MVP – Seguridad base y registro manual
- Dependencias: `spring-boot-starter-security`, `spring-boot-starter-validation`, `spring-boot-starter-data-jpa`.
- Entidades: `UsuarioEntity` (id, email, passwordHash, rol, activo).
- Repos: `UsuarioRepository` (`findByEmail`).
- Servicios: `AuthService` (`register`, `login`).
- Seguridad: endpoints públicos/protegidos.
- Passwords: `BCryptPasswordEncoder`.
- Tokens: UUID/DB o JWT opcional.

### Fase 2: Autenticación federada (OAuth2.0 Social Login)
- Dependencia: `spring-boot-starter-oauth2-client`.
- Configuración `application.yml` (Google, etc.).
- Flujo: React → Provider → callback → alta/merge usuario.
- `OAuth2SuccessHandler`: asignar rol base `CLIENTE`.

### Fase 3: Endurecimiento y administración de seguridad
- Módulo `security` independiente.
- MFA por SMS/WhatsApp.
- Revocación de tokens y auditoría.
- Rate limit, CAPTCHA, recuperación de contraseña.

---

## 8. Consideraciones de implementación

- Entorno local: Hibernate gestiona esquema (`ddl-auto=update`), Flyway deshabilitado.
- Transición a Flyway: una vez estable el esquema, exportar DDL y mover a migraciones versionadas.
- Validaciones de dominio: evitar solapamientos de turnos, respetar duraciones y espacios.
- Diseño de endpoints: DTOs con `@Valid`, errores claros, paginación donde aplique.
- Seguridad progresiva: arrancar abierto (solo `/health` público), luego proteger backoffice y casos de uso críticos.

---

## 9. Métricas de éxito/MVP

- Crear y aprobar un turno con reglas básicas.
- CRUD de servicios/empleados/disponibilidades funcionando.
- Reserva anónima (sin validación real, solo flag) disponible.
- Login clásico operativo (Fase 1 de seguridad).

---

## 10. Glosario

- Reserva anónima: turno solicitado sin cuenta de usuario.
- Validación telefónica: verificación por código (SMS/WhatsApp) para confirmar reserva.
- Backoffice: panel interno de administración de empresa.

---

## 11. Referencias

- ROADMAP: `docs/ROADMAP.md`
- Guía de scaffolding: `README.md`
- Configuración: `src/main/resources/application.yml`
