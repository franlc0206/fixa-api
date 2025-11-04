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
        System.out.println(" [CURRENT USER SERVICE] Obteniendo usuario actual...");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth == null) {
            System.out.println(" [CURRENT USER SERVICE] Authentication es NULL");
            return Optional.empty();
        }
        
        System.out.println(" [CURRENT USER SERVICE] Authentication encontrado: " + auth.getClass().getSimpleName());
        System.out.println(" [CURRENT USER SERVICE] Principal: " + (auth.getPrincipal() != null ? auth.getPrincipal().getClass().getSimpleName() : "NULL"));
        System.out.println(" [CURRENT USER SERVICE] Name: " + auth.getName());
        System.out.println(" [CURRENT USER SERVICE] Authenticated: " + auth.isAuthenticated());
        
        Optional<Usuario> userOpt = getCurrentUser();
        
        if (userOpt.isPresent()) {
            System.out.println(" [CURRENT USER SERVICE] Usuario encontrado - ID: " + userOpt.get().getId() + ", Email: " + userOpt.get().getEmail());
            return userOpt.map(Usuario::getId);
        } else {
            System.out.println(" [CURRENT USER SERVICE] Usuario NO encontrado en BD");
            return Optional.empty();
        }
    }

    public String getCurrentUserEmailOrThrow() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            throw new IllegalStateException("No hay usuario autenticado en el contexto");
        }
        return auth.getName();
    }
}
