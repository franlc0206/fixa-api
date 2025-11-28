package com.fixa.fixa_api.infrastructure.in.web.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class SuscripcionResponse {
    private Long id;
    private Long empresaId;
    private Long planId;
    private BigDecimal precioPactado;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private boolean activo;
}
