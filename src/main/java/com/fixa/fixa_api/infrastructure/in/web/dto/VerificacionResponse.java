package com.fixa.fixa_api.infrastructure.in.web.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para respuesta de verificación telefónica.
 * Siguiendo arquitectura hexagonal, este DTO pertenece a la capa de infraestructura (web).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerificacionResponse {
    private Long id;
    private String telefono;
    private String canal;
    private LocalDateTime fechaEnvio;
    private LocalDateTime fechaExpiracion;
    private boolean validado;
    private Long turnoId;
    private String message;
}
