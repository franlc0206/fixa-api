package com.fixa.fixa_api.application.usecase;

import com.fixa.fixa_api.domain.model.Turno;

public interface AprobarTurnoUseCase {
    Turno aprobar(Long turnoId);
}
