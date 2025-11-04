package com.fixa.fixa_api.infrastructure.out.persistence.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "turno")
@Data
public class TurnoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    @Column(name = "version")
    private Long version; // Optimistic locking: JPA incrementa automáticamente en cada actualización

    @ManyToOne(optional = false)
    @JoinColumn(name = "fk_servicio")
    private ServicioEntity servicio;

    @ManyToOne(optional = false)
    @JoinColumn(name = "fk_empleado")
    private EmpleadoEntity empleado;

    @ManyToOne(optional = false)
    @JoinColumn(name = "fk_empresa")
    private EmpresaEntity empresa;

    @ManyToOne
    @JoinColumn(name = "fk_cliente")
    private UsuarioEntity cliente; // nullable para anónimo

    @Column(name = "cliente_nombre", length = 100)
    private String clienteNombre;

    @Column(name = "cliente_apellido", length = 100)
    private String clienteApellido;

    @Column(name = "cliente_telefono", length = 30)
    private String clienteTelefono;

    @Column(name = "cliente_dni", length = 20)
    private String clienteDni;

    @Column(name = "cliente_email", length = 150)
    private String clienteEmail;

    @Column(name = "telefono_validado")
    private boolean telefonoValidado;

    @Column(name = "fecha_hora_inicio")
    private LocalDateTime fechaHoraInicio;

    @Column(name = "fecha_hora_fin")
    private LocalDateTime fechaHoraFin;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private TurnoEstado estado; // PENDIENTE, CONFIRMADO, CANCELADO, COMPLETADO

    @Column(name = "requiere_validacion")
    private boolean requiereValidacion;

    @Column(columnDefinition = "TEXT")
    private String observaciones;
}
