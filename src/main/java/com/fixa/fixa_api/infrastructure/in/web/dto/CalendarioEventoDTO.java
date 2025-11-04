package com.fixa.fixa_api.infrastructure.in.web.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para eventos de calendario compatible con FullCalendar.
 * Siguiendo arquitectura hexagonal, este DTO pertenece a la capa de infraestructura (web).
 * 
 * Formato compatible con FullCalendar v6:
 * https://fullcalendar.io/docs/event-object
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CalendarioEventoDTO {
    
    // Campos requeridos por FullCalendar
    private Long id;                    // ID del turno
    private String title;               // Título del evento (ej: "Juan Pérez - Corte")
    private String start;               // Fecha/hora inicio (ISO 8601: "2025-11-10T14:00:00")
    private String end;                 // Fecha/hora fin (ISO 8601: "2025-11-10T15:00:00")
    
    // Campos opcionales de FullCalendar
    private String backgroundColor;     // Color de fondo según estado
    private String borderColor;         // Color de borde
    private String textColor;           // Color del texto
    private boolean allDay = false;     // Si es evento de todo el día
    
    // Campos personalizados (extendedProps en FullCalendar)
    private String estado;              // CONFIRMADO, PENDIENTE, CANCELADO, etc.
    private String clienteNombre;       // Nombre completo del cliente
    private String clienteTelefono;     // Teléfono del cliente
    private String servicioNombre;      // Nombre del servicio
    private String empleadoNombre;      // Nombre del empleado
    private Long empleadoId;            // ID del empleado
    private Long servicioId;            // ID del servicio
    private String observaciones;       // Observaciones del turno
    private boolean requiereValidacion; // Si requiere validación telefónica
    private boolean telefonoValidado;   // Si el teléfono fue validado
}
