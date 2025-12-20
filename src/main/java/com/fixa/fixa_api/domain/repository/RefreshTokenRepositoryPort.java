package com.fixa.fixa_api.domain.repository;

import com.fixa.fixa_api.domain.model.RefreshToken;
import java.util.Optional;

public interface RefreshTokenRepositoryPort {
    RefreshToken save(RefreshToken refreshToken);

    Optional<RefreshToken> findByToken(String token);

    void deleteByUsuarioId(Long usuarioId);
}
