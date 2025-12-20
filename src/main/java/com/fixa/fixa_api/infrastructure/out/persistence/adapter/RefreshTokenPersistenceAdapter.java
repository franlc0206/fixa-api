package com.fixa.fixa_api.infrastructure.out.persistence.adapter;

import com.fixa.fixa_api.domain.model.RefreshToken;
import com.fixa.fixa_api.domain.repository.RefreshTokenRepositoryPort;
import com.fixa.fixa_api.infrastructure.out.persistence.entity.RefreshTokenEntity;
import com.fixa.fixa_api.infrastructure.out.persistence.repository.RefreshTokenJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
public class RefreshTokenPersistenceAdapter implements RefreshTokenRepositoryPort {

    private final RefreshTokenJpaRepository repository;

    public RefreshTokenPersistenceAdapter(RefreshTokenJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public RefreshToken save(RefreshToken refreshToken) {
        RefreshTokenEntity entity = new RefreshTokenEntity();
        entity.setId(refreshToken.getId());
        entity.setUsuarioId(refreshToken.getUsuarioId());
        entity.setToken(refreshToken.getToken());
        entity.setExpiryDate(refreshToken.getExpiryDate());

        RefreshTokenEntity saved = repository.save(entity);
        refreshToken.setId(saved.getId());
        return refreshToken;
    }

    @Override
    public Optional<RefreshToken> findByToken(String token) {
        return repository.findByToken(token)
                .map(entity -> RefreshToken.builder()
                        .id(entity.getId())
                        .usuarioId(entity.getUsuarioId())
                        .token(entity.getToken())
                        .expiryDate(entity.getExpiryDate())
                        .build());
    }

    @Override
    @Transactional
    public void deleteByUsuarioId(Long usuarioId) {
        repository.deleteByUsuarioId(usuarioId);
    }
}
