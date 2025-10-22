package com.fixa.fixa_api.infrastructure.out.persistence.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "bloqueo_horario")
@Data
public class BloqueoHorarioEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "fk_empresa")
    private EmpresaEntity empresa;

    @ManyToOne
    @JoinColumn(name = "fk_empleado")
    private EmpleadoEntity empleado; // opcional

    @Column(name = "fecha_inicio")
    private LocalDateTime fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDateTime fechaFin;

    @Column(length = 255)
    private String motivo;
}
