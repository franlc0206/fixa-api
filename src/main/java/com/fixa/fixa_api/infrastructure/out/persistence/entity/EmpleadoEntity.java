package com.fixa.fixa_api.infrastructure.out.persistence.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "empleado")
@Data
public class EmpleadoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "fk_empresa")
    private EmpresaEntity empresa;

    @ManyToOne
    @JoinColumn(name = "fk_usuario")
    private UsuarioEntity usuario; // opcional

    @Column(length = 100)
    private String nombre;

    @Column(length = 100)
    private String apellido;

    @Column(length = 100)
    private String rol; // peluquero, colorista, etc.

    @Column(name = "foto_url", length = 500)
    private String fotoUrl;

    @Column(name = "trabaja_publicamente")
    private boolean trabajaPublicamente = true;

    @Column
    private boolean activo;
}
