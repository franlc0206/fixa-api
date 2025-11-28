package com.fixa.fixa_api.infrastructure.in.web.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AsignarPlanRequest {
    @NotNull
    private Long planId;

    private BigDecimal precioPactado;
}
