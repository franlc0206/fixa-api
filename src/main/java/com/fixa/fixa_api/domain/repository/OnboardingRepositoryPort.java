package com.fixa.fixa_api.domain.repository;

import com.fixa.fixa_api.domain.model.UsuarioOnboardingProgreso;
import java.util.List;
import java.util.Optional;

public interface OnboardingRepositoryPort {
    UsuarioOnboardingProgreso save(UsuarioOnboardingProgreso progreso);

    List<UsuarioOnboardingProgreso> findByUsuarioId(Long usuarioId);

    Optional<UsuarioOnboardingProgreso> findByUsuarioIdAndFeatureKey(Long usuarioId, String featureKey);

}
