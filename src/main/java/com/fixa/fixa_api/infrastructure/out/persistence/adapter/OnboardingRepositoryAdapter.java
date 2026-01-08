package com.fixa.fixa_api.infrastructure.out.persistence.adapter;

import com.fixa.fixa_api.domain.model.UsuarioOnboardingProgreso;
import com.fixa.fixa_api.domain.repository.OnboardingRepositoryPort;
import com.fixa.fixa_api.infrastructure.out.persistence.entity.UsuarioEntity;
import com.fixa.fixa_api.infrastructure.out.persistence.entity.UsuarioOnboardingProgresoEntity;
import com.fixa.fixa_api.infrastructure.out.persistence.mapper.OnboardingMapper;
import com.fixa.fixa_api.infrastructure.out.persistence.repository.OnboardingJpaRepository;
import com.fixa.fixa_api.infrastructure.out.persistence.repository.UsuarioJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class OnboardingRepositoryAdapter implements OnboardingRepositoryPort {

    private final OnboardingJpaRepository onboardingJpaRepository;
    private final UsuarioJpaRepository usuarioJpaRepository;
    private final OnboardingMapper onboardingMapper;

    public OnboardingRepositoryAdapter(OnboardingJpaRepository onboardingJpaRepository,
            UsuarioJpaRepository usuarioJpaRepository,
            OnboardingMapper onboardingMapper) {
        this.onboardingJpaRepository = onboardingJpaRepository;
        this.usuarioJpaRepository = usuarioJpaRepository;
        this.onboardingMapper = onboardingMapper;
    }

    @Override
    public UsuarioOnboardingProgreso save(UsuarioOnboardingProgreso progreso) {
        UsuarioEntity usuarioEntity = usuarioJpaRepository.findById(progreso.getUsuarioId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con id: " + progreso.getUsuarioId()));

        UsuarioOnboardingProgresoEntity entity = onboardingMapper.toEntity(progreso, usuarioEntity);
        UsuarioOnboardingProgresoEntity saved = onboardingJpaRepository.save(entity);
        return onboardingMapper.toDomain(saved);
    }

    @Override
    public List<UsuarioOnboardingProgreso> findByUsuarioId(Long usuarioId) {
        return onboardingJpaRepository.findByUsuarioId(usuarioId).stream()
                .map(onboardingMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<UsuarioOnboardingProgreso> findByUsuarioIdAndFeatureKey(Long usuarioId, String featureKey) {
        return onboardingJpaRepository.findByUsuarioIdAndFeatureKey(usuarioId, featureKey)
                .map(onboardingMapper::toDomain);
    }

}
