package com.fixa.fixa_api.infrastructure.out.persistence.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "empresa")
@Data
public class EmpresaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "fk_usuario_admin")
    private UsuarioEntity usuarioAdmin; // nullable=false en futuro si se requiere

    @Column(length = 150)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(length = 255)
    private String direccion;

    @Column(length = 30)
    private String telefono;

    @Column(length = 150)
    private String email;

    @ManyToOne
    @JoinColumn(name = "fk_categoria")
    private CategoriaEntity categoria;

    @Column(name = "permite_reservas_sin_usuario")
    private boolean permiteReservasSinUsuario;

    @Column(name = "requiere_validacion_telefono")
    private boolean requiereValidacionTelefono;

    @Column(name = "requiere_aprobacion_turno")
    private boolean requiereAprobacionTurno;

    @Column(name = "mensaje_validacion_personalizado", columnDefinition = "TEXT")
    private String mensajeValidacionPersonalizado;

    @Column(name = "visibilidad_publica")
    private boolean visibilidadPublica;

    @Column(name = "activo")
    private boolean activo;
}
