package com.fixa.fixa_api.infrastructure.in.web.dto;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * DTO para crear una verificación telefónica.
 * Siguiendo arquitectura hexagonal, este DTO pertenece a la capa de
 * infraestructura (web)
 * y será mapeado al modelo de dominio en el controller.
 */
@Data
public class VerificacionCreateRequest {

    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Formato de teléfono inválido. Use formato internacional (ej: +5491112345678)")
    private String telefono;

    private String email;

    private String canal = "sms"; // Default: sms (opciones: sms, whatsapp)

    private Long turnoId; // Opcional: ID del turno a asociar
}
