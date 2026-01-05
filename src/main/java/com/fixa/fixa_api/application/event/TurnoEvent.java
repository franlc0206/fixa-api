package com.fixa.fixa_api.application.event;

import com.fixa.fixa_api.domain.model.Turno;
import lombok.Getter;

@Getter
public class TurnoEvent {
    private final Turno turno;
    private final String tipo; // CREACION, REPROGRAMACION, APROBACION, CANCELACION
    private final String motivo; // Opcional para cancelación

    public TurnoEvent(Turno turno, String tipo) {
        this(turno, tipo, null);
    }

    public TurnoEvent(Turno turno, String tipo, String motivo) {
        this.turno = turno;
        this.tipo = tipo;
        this.motivo = motivo;
    }
}
