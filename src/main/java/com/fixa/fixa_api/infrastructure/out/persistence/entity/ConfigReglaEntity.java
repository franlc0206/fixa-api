package com.fixa.fixa_api.infrastructure.out.persistence.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "config_regla")
@Data
public class ConfigReglaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "fk_empresa")
    private EmpresaEntity empresa;

    @Column(length = 100)
    private String clave;

    @Column(length = 255)
    private String valor;

    @Column(length = 20)
    private String tipo; // bool, int, string, decimal

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    private boolean activo;
}
