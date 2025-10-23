package com.fixa.fixa_api.infrastructure.out.persistence.adapter;

import com.fixa.fixa_api.domain.model.Usuario;
import com.fixa.fixa_api.domain.repository.UsuarioRepositoryPort;
import com.fixa.fixa_api.infrastructure.out.persistence.entity.UsuarioEntity;
import com.fixa.fixa_api.infrastructure.out.persistence.mapper.UsuarioMapper;
import com.fixa.fixa_api.infrastructure.out.persistence.repository.UsuarioJpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UsuarioRepositoryAdapter implements UsuarioRepositoryPort {

    private final UsuarioJpaRepository usuarioRepo;

    public UsuarioRepositoryAdapter(UsuarioJpaRepository usuarioRepo) {
        this.usuarioRepo = usuarioRepo;
    }

    @Override
    public Optional<Usuario> findByEmail(String email) {
        return usuarioRepo.findByEmail(email).map(UsuarioMapper::toDomain);
    }

    @Override
    public Usuario save(Usuario usuario) {
        UsuarioEntity e = UsuarioMapper.toEntity(usuario);
        UsuarioEntity saved = usuarioRepo.save(e);
        return UsuarioMapper.toDomain(saved);
    }

    @Override
    public Usuario saveWithPasswordHash(Usuario usuario, String passwordHash) {
        UsuarioEntity e = UsuarioMapper.toEntity(usuario);
        e.setPasswordHash(passwordHash);
        UsuarioEntity saved = usuarioRepo.save(e);
        return UsuarioMapper.toDomain(saved);
    }
}
