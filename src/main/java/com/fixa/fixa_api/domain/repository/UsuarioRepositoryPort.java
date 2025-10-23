package com.fixa.fixa_api.domain.repository;

import com.fixa.fixa_api.domain.model.Usuario;
import java.util.Optional;

public interface UsuarioRepositoryPort {
    Optional<Usuario> findByEmail(String email);
    Usuario save(Usuario usuario);
    Usuario saveWithPasswordHash(Usuario usuario, String passwordHash);
}
