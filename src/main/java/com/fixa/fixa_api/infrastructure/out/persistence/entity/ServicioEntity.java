package com.fixa.fixa_api.infrastructure.out.persistence.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Entity
@Table(name = "servicio")
@Data
public class ServicioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "fk_empresa")
    private EmpresaEntity empresa;

    @ManyToOne
    @JoinColumn(name = "fk_categoria")
    private CategoriaEntity categoria;

    @Column(length = 150)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "duracion_minutos")
    private Integer duracionMinutos;

    @Column(name = "requiere_espacio_libre")
    private boolean requiereEspacioLibre;

    @Column(name = "patron_bloques", columnDefinition = "TEXT")
    private String patronBloques; // JSON para configuración avanzada

    private BigDecimal costo;

    @Column(name = "requiere_sena")
    private boolean requiereSena;

    private boolean activo;

    @Column(name = "foto_url", length = 500)
    private String fotoUrl;
}
