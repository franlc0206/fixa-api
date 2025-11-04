package com.fixa.fixa_api.infrastructure.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * DTO para confirmar un código de verificación.
 * Siguiendo arquitectura hexagonal, este DTO pertenece a la capa de infraestructura (web)
 * y será mapeado al modelo de dominio en el controller.
 */
@Data
public class VerificacionConfirmRequest {
    
    @NotBlank(message = "El código es requerido")
    @Pattern(regexp = "^\\d{6}$", message = "El código debe ser de 6 dígitos")
    private String codigo;
}
