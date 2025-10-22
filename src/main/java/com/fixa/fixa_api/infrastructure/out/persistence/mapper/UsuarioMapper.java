package com.fixa.fixa_api.infrastructure.out.persistence.mapper;

import com.fixa.fixa_api.domain.model.Usuario;
import com.fixa.fixa_api.infrastructure.out.persistence.entity.UsuarioEntity;

public final class UsuarioMapper {
    private UsuarioMapper() {}

    public static Usuario toDomain(UsuarioEntity e) {
        if (e == null) return null;
        Usuario u = new Usuario();
        u.setId(e.getId());
        u.setNombre(e.getNombre());
        u.setApellido(e.getApellido());
        u.setEmail(e.getEmail());
        u.setTelefono(e.getTelefono());
        u.setRol(e.getRol());
        u.setActivo(e.isActivo());
        return u;
    }

    public static UsuarioEntity toEntity(Usuario u) {
        if (u == null) return null;
        UsuarioEntity e = new UsuarioEntity();
        e.setId(u.getId());
        e.setNombre(u.getNombre());
        e.setApellido(u.getApellido());
        e.setEmail(u.getEmail());
        e.setTelefono(u.getTelefono());
        e.setRol(u.getRol());
        e.setActivo(u.isActivo());
        // passwordHash intencionalmente omitido en dominio en esta etapa del scaffolding
        return e;
    }
}
