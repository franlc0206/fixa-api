package com.fixa.fixa_api.domain.model;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class Plan {
    private Long id;
    private String nombre;
    private BigDecimal precio;
    private int maxEmpleados;
    private int maxServicios;
    private int maxTurnosMensuales;
    private boolean soportePrioritario;
    private boolean activo;
    private String mercadopagoPlanId;
}
