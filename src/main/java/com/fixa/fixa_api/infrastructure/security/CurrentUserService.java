package com.fixa.fixa_api.infrastructure.security;

import com.fixa.fixa_api.domain.model.Usuario;
import com.fixa.fixa_api.domain.repository.UsuarioRepositoryPort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class CurrentUserService {

    private final UsuarioRepositoryPort usuarioPort;

    public CurrentUserService(UsuarioRepositoryPort usuarioPort) {
        this.usuarioPort = usuarioPort;
    }

    public Optional<Usuario> getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) return Optional.empty();
        return usuarioPort.findByEmail(auth.getName());
    }

    public Optional<Long> getCurrentUserId() {
        return getCurrentUser().map(Usuario::getId);
    }

    public String getCurrentUserEmailOrThrow() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            throw new IllegalStateException("No hay usuario autenticado en el contexto");
        }
        return auth.getName();
    }
}
