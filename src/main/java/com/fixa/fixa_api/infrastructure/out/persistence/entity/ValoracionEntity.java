package com.fixa.fixa_api.infrastructure.out.persistence.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "valoracion", 
    uniqueConstraints = @UniqueConstraint(columnNames = {"fk_turno"}),
    indexes = {
        @Index(name = "idx_valoracion_empresa", columnList = "fk_empresa"),
        @Index(name = "idx_valoracion_usuario", columnList = "fk_usuario")
    }
)
@Data
public class ValoracionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "fk_empresa", nullable = false)
    private EmpresaEntity empresa;

    @ManyToOne(optional = false)
    @JoinColumn(name = "fk_usuario", nullable = false)
    private UsuarioEntity usuario;

    @OneToOne(optional = false)
    @JoinColumn(name = "fk_turno", nullable = false, unique = true)
    private TurnoEntity turno;

    @Column(name = "puntuacion", nullable = false)
    private Integer puntuacion; // 0-5

    @Column(name = "resena", columnDefinition = "TEXT")
    private String resena;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "activo", nullable = false)
    private boolean activo = true;

    @PrePersist
    protected void onCreate() {
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
    }
}
