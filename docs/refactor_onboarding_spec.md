# Especificación de Refactor: Sistema de Cursos y Onboarding

**Versión:** 1.0 (Borrador)
**Fecha:** 08/01/2026
**Objetivo:** Refactorizar el módulo de Onboarding para convertirlo en un sistema genérico de Cursos/Guías, escalable para futuros tutoriales (ej: WhatsApp, Novedades) y alineado con la arquitectura hexagonal.

---

## 1. Cambios en Base de Datos (Modelado)

### Nueva Tabla: `usuario_curso_progreso`
**Propósito:** Reemplazar la tabla rígida `usuario_onboarding_progreso` por una estructura capaz de soportar múltiples flujos.

```sql
CREATE TABLE usuario_curso_progreso (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    fk_usuario BIGINT NOT NULL,
    
    -- Identificador del Curso (Ej: 'ONBOARDING_INICIAL', 'TUTORIAL_WHATSAPP')
    curso_key VARCHAR(50) NOT NULL,
    
    -- Identificador del Paso dentro del curso (Ej: 'INITIAL_SETUP', 'VALIDAR_CELULAR')
    step_key VARCHAR(50) NOT NULL,
    
    -- Estado del paso
    completado BOOLEAN NOT NULL DEFAULT FALSE,
    fecha_completado TIMESTAMP,
    
    CONSTRAINT fk_curso_usuario FOREIGN KEY (fk_usuario) REFERENCES usuario(id),
    -- Regla de Negocio: Un usuario no puede tener el mismo paso registrado dos veces para el mismo curso
    UNIQUE (fk_usuario, curso_key, step_key)
);
```

### Migración
*   Si existen datos en la tabla anterior, migrarlos asignando `curso_key = 'ONBOARDING_INICIAL'`.

---

## 2. Arquitectura y Código (Backend)

### A. Capa de Infraestructura (Limpieza y Unificación)
Actualmente existen `NotificationServiceAdapter` (existente) y `NotificationClient` (nuevo/duplicado).

1.  **Eliminar `NotificationClient`**: Esta clase viola la arquitectura al ser inyectada en servicios y duplica funcionalidad.
2.  **Actualizar `NotificationServiceAdapter`**:
    *   Centralizar aquí el envío de correos.
    *   **Soporte de Templates:** Si la API de notificaciones requiere un ID (ej: "welcome_onboarding") en lugar de HTML, actualizar este adaptador para soportarlo. Mantener el contrato del puerto.
    *   **Configuración:** Usar una única fuente de propiedades (ej: `notifications.api.url`).

### B. Capa de Aplicación (Servicio de Dominio)
1.  **Renombrar `OnboardingService` a `CourseProgressService`**.
2.  **Inyección Correcta:** El servicio debe inyectar `NotificationServicePort`, nunca una clase de infraestructura concreta.
3.  **Lógica General:**
    *   El servicio gestiona el progreso de *cualquier* curso basado en `cursoKey`.
    *   **Lógica de "Smart Detection"**: Al consultar el estado, verificar condiciones de negocio (ej: "¿Tiene empleados?") para marcar pasos como completados automáticamente.
    *   **Side Effects:** Al completar un paso crítico (ej: el último paso del onboarding), disparar eventos o notificaciones usando el Puerto.

---

## 3. Contrato de API (Frontend <-> Backend)

El Frontend debe ser agnóstico del contenido del curso y limitarse a renderizar lo que el Backend indique.

### 1. Obtener Estado de Cursos
`GET /api/me/courses` (o `/api/me/courses/{courseKey}`)

**Respuesta JSON Sugerida:**
```json
{
  "courses": [
    {
      "key": "ONBOARDING_INICIAL",
      "status": "IN_PROGRESS", // O "COMPLETED"
      "completedSteps": ["INITIAL_SETUP", "TOUR_EMPLEADOS"],
      "suggestedStep": "TOUR_SERVICIOS", // El siguiente paso lógico
      "totalSteps": 4
    }
  ]
}
```

### 2. Completar un Paso
`POST /api/me/courses/{courseKey}/steps/{stepKey}/complete`

*   **Acción:** El frontend llama a este endpoint cuando el usuario finaliza satisfactoriamente una guía o tarea.
*   **Backend:** Registra el avance y verifica si el curso se completó.

---

## 4. Definición del Curso "Onboarding Inicial"
Configuración inicial para el refactor.

*   **Key:** `ONBOARDING_INICIAL`
*   **Pasos:**
    1.  `INITIAL_SETUP` (Registro y datos base)
    2.  `TOUR_EMPLEADOS` (Primer empleado cargado)
    3.  `TOUR_SERVICIOS` (Primer servicio cargado)
    4.  `TOUR_DISPONIBILIDAD` (Configuración horaria)

---
**Nota Final:** El objetivo es que mañana, si queremos agregar un "Tutorial de WhatsApp", solo tengamos que definir un nuevo `curso_key` y sus pasos, sin tocar la estructura de la base de datos ni crear nuevas tablas.
