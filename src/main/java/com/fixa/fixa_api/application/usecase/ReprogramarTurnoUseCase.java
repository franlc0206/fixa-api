package com.fixa.fixa_api.application.usecase;

import com.fixa.fixa_api.domain.model.Turno;
import java.time.LocalDateTime;

public interface ReprogramarTurnoUseCase {
    Turno reprogramar(Long turnoId, LocalDateTime nuevaFechaInicio, Long usuarioIdSolicitante);
}
