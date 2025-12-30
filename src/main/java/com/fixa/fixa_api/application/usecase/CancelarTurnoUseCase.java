package com.fixa.fixa_api.application.usecase;

import com.fixa.fixa_api.domain.model.Turno;

public interface CancelarTurnoUseCase {
    Turno cancelar(Long turnoId, String motivo);

    Turno cancelar(Long turnoId, String motivo, Long usuarioSolicitanteId);
}
