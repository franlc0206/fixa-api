package com.fixa.fixa_api.domain.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Turno {
    private Long id;
    private Long servicioId;
    private Long empleadoId;
    private Long empresaId;
    private Long clienteId; // nullable si anónimo
    private String clienteNombre;
    private String clienteApellido;
    private String clienteTelefono;
    private String clienteDni;
    private String clienteEmail;
    private boolean telefonoValidado;
    private LocalDateTime fechaHoraInicio;
    private LocalDateTime fechaHoraFin;
    private String estado; // pendiente_aprobacion, confirmado, cancelado, completado
    private boolean requiereValidacion;
    private String observaciones;
}
