package com.fixa.fixa_api.infrastructure.out.persistence.repository;

import com.fixa.fixa_api.infrastructure.out.persistence.entity.UsuarioOnboardingProgresoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OnboardingJpaRepository extends JpaRepository<UsuarioOnboardingProgresoEntity, Long> {
    List<UsuarioOnboardingProgresoEntity> findByUsuarioId(Long usuarioId);

    Optional<UsuarioOnboardingProgresoEntity> findByUsuarioIdAndFeatureKey(Long usuarioId, String featureKey);

}
