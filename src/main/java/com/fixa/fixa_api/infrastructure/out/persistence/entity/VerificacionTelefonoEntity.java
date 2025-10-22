package com.fixa.fixa_api.infrastructure.out.persistence.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "verificacion_telefono")
@Data
public class VerificacionTelefonoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 30)
    private String telefono;

    @Column(length = 10)
    private String codigo;

    @Column(name = "fecha_envio")
    private LocalDateTime fechaEnvio;

    @Column(name = "fecha_expiracion")
    private LocalDateTime fechaExpiracion;

    private boolean validado;

    @Column(length = 20)
    private String canal; // sms, whatsapp

    @ManyToOne
    @JoinColumn(name = "fk_turno")
    private TurnoEntity turno;
}
