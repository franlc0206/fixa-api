package com.fixa.fixa_api.infrastructure.out.persistence.mapper;

import com.fixa.fixa_api.domain.model.UsuarioOnboardingProgreso;
import com.fixa.fixa_api.infrastructure.out.persistence.entity.UsuarioEntity;
import com.fixa.fixa_api.infrastructure.out.persistence.entity.UsuarioOnboardingProgresoEntity;
import org.springframework.stereotype.Component;

@Component
public class OnboardingMapper {

    public UsuarioOnboardingProgreso toDomain(UsuarioOnboardingProgresoEntity entity) {
        if (entity == null)
            return null;
        return new UsuarioOnboardingProgreso(
                entity.getId(),
                entity.getUsuario() != null ? entity.getUsuario().getId() : null,
                entity.getFeatureKey(),
                entity.getCompletado(),
                entity.getPasoActual(),
                entity.getFechaCompletado());
    }

    public UsuarioOnboardingProgresoEntity toEntity(UsuarioOnboardingProgreso domain, UsuarioEntity usuarioEntity) {
        if (domain == null)
            return null;
        UsuarioOnboardingProgresoEntity entity = new UsuarioOnboardingProgresoEntity();
        if (domain.getId() != null) {
            entity.setId(domain.getId());
        }
        entity.setUsuario(usuarioEntity);
        entity.setFeatureKey(domain.getFeatureKey());
        entity.setCompletado(domain.getCompletado());
        entity.setPasoActual(domain.getPasoActual());
        entity.setFechaCompletado(domain.getFechaCompletado());
        return entity;
    }
}
