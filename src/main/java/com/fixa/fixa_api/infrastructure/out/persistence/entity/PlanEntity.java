package com.fixa.fixa_api.infrastructure.out.persistence.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Entity
@Table(name = "planes")
@Data
public class PlanEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;

    @Column(name = "max_empleados", nullable = false)
    private int maxEmpleados;

    @Column(name = "max_servicios", nullable = false)
    private int maxServicios;

    @Column(name = "max_turnos_mensuales", nullable = false)
    private int maxTurnosMensuales;

    @Column(name = "soporte_prioritario")
    private boolean soportePrioritario = false;

    @Column(nullable = false)
    private boolean activo = true;
}
