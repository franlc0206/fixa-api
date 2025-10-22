package com.fixa.fixa_api.infrastructure.out.persistence.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "auditoria")
@Data
public class AuditoriaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "fk_usuario")
    private UsuarioEntity usuario; // opcional (p.ej. eventos de sistema)

    @Column(length = 100)
    private String entidad;

    @Column(length = 20)
    private String operacion; // CREATE, UPDATE, DELETE, LOGIN

    private LocalDateTime fecha;

    @Column(columnDefinition = "TEXT")
    private String detalle; // JSON como texto
}
