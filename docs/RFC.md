# 🧾 RFC – Sistema de Turnos Online (Turnero Web)

Versión: 2.0  
Autor: Francisco López  
Fecha: Octubre 2025

---

## 1. Objetivo

Desarrollar una plataforma web moderna y flexible para la gestión de turnos en línea, que permita a empresas de servicios (peluquerías, barberías, centros de estética, gimnasios, consultorios, etc.) ofrecer reservas a clientes de forma simple, segura y configurable.
El sistema permitirá:
- Alta de empresas y configuración de sus servicios, empleados y reglas.
- Reservas configurables con aprobación, validación o confirmación automática.
- Control granular de horarios, disponibilidad, bloqueos y notificaciones.
- Escalabilidad para múltiples empresas (modelo multi-tenant).

---

## 2. Alcance

### Funcionalidades principales
- Registro de empresas y categorías.
- Alta de empleados, servicios y disponibilidad.
- Configuración de reglas de negocio por empresa (validaciones, reservas pendientes, etc.).
- Reservas anónimas o con usuario registrado.
- Validación telefónica opcional (SMS/WhatsApp).
- Notificaciones automáticas (confirmaciones, recordatorios, cancelaciones).
- Panel para empresa y panel de SuperAdmin.

### No incluido en esta versión (posible v3.0)
- Pagos en línea (MercadoPago, Stripe).
- Integración con Google Calendar.
- Aplicación móvil nativa.

---

## 3. Tipos de usuarios

- SuperAdmin: Control total del sistema. Permisos: alta/baja empresas, auditoría, gestión global.
- Empresa (Admin empresa): Dueño o responsable. Permisos: alta empleados, servicios, horarios, reglas.
- Empleado: Prestador del servicio. Permisos: visualiza y gestiona sus turnos.
- Cliente registrado: Usuario con cuenta. Permisos: reservar, cancelar, modificar turnos.
- Cliente anónimo: Usuario sin cuenta. Permisos: reserva con validación telefónica.

---

## 4. Modelo de datos (vFinal)

Nota de implementación por fases:
- Fase 1 (MVP dev con Hibernate): usuario, empresa, empleado, servicio, disponibilidad, turno, verificacion_telefono.
- Próximas fases: categoria, bloqueo_horario, config_regla, notificacion, auditoria.

### 🧑‍💻 usuario
- id BIGINT PK
- nombre VARCHAR(100)
- apellido VARCHAR(100)
- email VARCHAR(150) UNIQUE
- telefono VARCHAR(30)
- password_hash VARCHAR(255)
- rol ENUM('superadmin','empresa','empleado','cliente')
- activo BOOLEAN

### 🏢 empresa
- id BIGINT PK
- fk_usuario_admin BIGINT FK → usuario.id
- fk_categoria BIGINT FK → categoria.id
- nombre VARCHAR(150)
- descripcion TEXT
- direccion VARCHAR(255)
- telefono VARCHAR(30)
- email VARCHAR(150)
- permite_reservas_sin_usuario BOOLEAN
- requiere_validacion_telefono BOOLEAN
- requiere_aprobacion_turno BOOLEAN
- mensaje_validacion_personalizado TEXT
- visibilidad_publica BOOLEAN
- activo BOOLEAN

### 🏷️ categoria
- id BIGINT PK
- tipo ENUM('empresa','servicio')
- nombre VARCHAR(100) UNIQUE
- descripcion TEXT
- activo BOOLEAN

### 🧍‍♂️ empleado
- id BIGINT PK
- fk_empresa BIGINT FK → empresa.id
- fk_usuario BIGINT NULL FK → usuario.id
- nombre VARCHAR(100)
- apellido VARCHAR(100)
- rol VARCHAR(100)
- activo BOOLEAN

### 💇‍♂️ servicio
- id BIGINT PK
- fk_empresa BIGINT FK → empresa.id
- fk_categoria BIGINT NULL FK → categoria.id
- nombre VARCHAR(150)
- descripcion TEXT
- duracion_minutos INT
- requiere_espacio_libre BOOLEAN
- costo DECIMAL(10,2)
- requiere_seña BOOLEAN
- activo BOOLEAN

### 🕓 disponibilidad
- id BIGINT PK
- fk_empleado BIGINT FK → empleado.id
- dia_semana ENUM('lunes','martes','miércoles','jueves','viernes','sábado','domingo')
- hora_inicio TIME
- hora_fin TIME

### 🚫 bloqueo_horario
- id BIGINT PK
- fk_empresa BIGINT FK → empresa.id
- fk_empleado BIGINT NULL FK → empleado.id
- fecha_inicio DATETIME
- fecha_fin DATETIME
- motivo VARCHAR(255)

### 📅 turno
- id BIGINT PK
- fk_servicio BIGINT FK → servicio.id
- fk_empleado BIGINT FK → empleado.id
- fk_empresa BIGINT FK → empresa.id
- fk_cliente BIGINT NULL FK → usuario.id
- cliente_nombre VARCHAR(100)
- cliente_apellido VARCHAR(100)
- cliente_telefono VARCHAR(30)
- cliente_email VARCHAR(150) NULL
- cliente_dni VARCHAR(20) NULL
- telefono_validado BOOLEAN
- fecha_hora_inicio DATETIME
- fecha_hora_fin DATETIME
- estado ENUM('pendiente','confirmado','cancelado','completado')
- requiere_validacion BOOLEAN
- observaciones TEXT

### 📱 verificacion_telefono
- id BIGINT PK
- telefono VARCHAR(30)
- codigo VARCHAR(10)
- fecha_envio DATETIME
- fecha_expiracion DATETIME
- validado BOOLEAN
- canal ENUM('sms','whatsapp')
- fk_turno BIGINT NULL FK → turno.id

### ⚙️ config_regla
- id BIGINT PK
- fk_empresa BIGINT FK → empresa.id
- clave VARCHAR(100)
- valor VARCHAR(255)
- tipo ENUM('bool','int','string','decimal')
- descripcion TEXT
- activo BOOLEAN

### 💬 notificacion
- id BIGINT PK
- fk_turno BIGINT NULL FK → turno.id
- fk_usuario BIGINT NULL FK → usuario.id
- canal ENUM('email','whatsapp','sms')
- mensaje TEXT
- fecha_envio DATETIME
- estado ENUM('pendiente','enviado','error')

### 📜 auditoria
- id BIGINT PK
- fk_usuario BIGINT REFERENCES usuario(id)
- entidad VARCHAR(100)
- operacion ENUM('CREATE','UPDATE','DELETE','LOGIN')
- fecha DATETIME
- detalle JSON

---

## 5. Flujo funcional (ejemplo: Peluquería)

- Alta de empresa: El usuario administrador crea su peluquería → se le asigna la categoría “Peluquería”.
- Alta de empleados: Agrega empleados (ej. Manuel) y define sus horarios.
- Alta de servicios: Carga servicios (“Corte de pelo”, “Coloración”) con duración (30 min o 1 h).
- Configuración de reglas: Define si los turnos deben aprobarse, si hay validación telefónica, etc.
- Disponibilidad: Manuel trabaja de lunes a viernes, de 9:00 a 18:00, con turnos de 30 min.
- Publicación: La empresa habilita visibilidad pública.
- Reserva del cliente: Un cliente selecciona un horario libre y reserva. Si la empresa usa “pendiente de aprobación”: el turno queda en estado pendiente. Si usa “validación telefónica”: se envía un código vía SMS/WhatsApp.
- Confirmación: Al validarse o aprobarse, el turno pasa a confirmado.
- Notificación: Se envía confirmación o recordatorio automático.

---

## 6. Arquitectura técnica

- Frontend público: React / Next.js
- Backoffice empresa: React (Panel)
- Backend: Java Spring Boot
- Base de datos: MySQL
- Mensajería: Twilio / WhatsApp Cloud API
- Infraestructura: AWS / Railway / DonWeb / Render
- Arquitectura: Hexagonal (Domain / Application / Infrastructure)

Estructura de paquetes:
```
src/main/java/com/fixa/turnero/
  domain/
    model/
    repository/
  application/
    usecase/
  infrastructure/
    in/
      web/
      messaging/
    out/
      persistence/
      sms/
      email/
    config/
```

---

## 7. Seguridad – Roadmap (OAuth2.0 + MFA)

- Fase 1 (MVP)
  - Login clásico con email/password (BCrypt)
  - Roles: superadmin, empresa, empleado, cliente
  - Endpoints públicos: /auth, /health
- Fase 2
  - OAuth2 (Google, Facebook)
  - Alta automática del usuario cliente tras login social
- Fase 3
  - MFA por SMS/WhatsApp
  - Auditoría de logins
  - Rate limit y recaptcha
  - Revocación de tokens

---

## 8. Consideraciones técnicas

- Flyway para migraciones controladas
- Validaciones: no solapamiento de turnos, horarios válidos
- Paginación y DTOs limpios (@Valid)
- Configuración de reglas editable desde el panel
- Notificaciones asíncronas (event-driven)

---

## 9. KPIs / Métricas de éxito (MVP)

- Alta completa de empresa con empleados y servicios.
- Reserva anónima funcional con aprobación manual.
- Validación telefónica básica operativa.
- Login clásico funcionando.

---

## 10. Glosario

- Turno pendiente: Reserva que requiere aprobación manual
- Reserva anónima: Reserva sin cuenta de usuario
- Validación telefónica: Confirmación por código enviado
- Regla de negocio: Configuración dinámica que define comportamiento del sistema
- Bloqueo horario: Período en que no se pueden tomar turnos

---

## 11. Referencias

- ROADMAP: `docs/ROADMAP.md`
- Guía de scaffolding: `README.md`
- Configuración: `src/main/resources/application.yml`
