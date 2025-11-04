package com.fixa.fixa_api.infrastructure.in.web.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TurnoPublicoResponse {
    private Long turnoId;
    private String estado;
    private boolean requiresValidation;
    private Long verificationId;
    private String message;
}
