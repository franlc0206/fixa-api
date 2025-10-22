package com.fixa.fixa_api.infrastructure.out.persistence.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "notificacion")
@Data
public class NotificacionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "fk_turno")
    private TurnoEntity turno; // opcional

    @ManyToOne
    @JoinColumn(name = "fk_usuario")
    private UsuarioEntity usuario; // opcional

    @Column(length = 20)
    private String canal; // email, whatsapp, sms

    @Column(columnDefinition = "TEXT")
    private String mensaje;

    @Column(name = "fecha_envio")
    private LocalDateTime fechaEnvio;

    @Column(length = 20)
    private String estado; // pendiente, enviado, error
}
