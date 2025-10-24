package com.fixa.fixa_api.infrastructure.out.persistence.mapper;

import com.fixa.fixa_api.domain.model.UsuarioEmpresa;
import com.fixa.fixa_api.infrastructure.out.persistence.entity.EmpresaEntity;
import com.fixa.fixa_api.infrastructure.out.persistence.entity.UsuarioEmpresaEntity;
import com.fixa.fixa_api.infrastructure.out.persistence.entity.UsuarioEntity;

public final class UsuarioEmpresaMapper {
    private UsuarioEmpresaMapper() {}

    public static UsuarioEmpresa toDomain(UsuarioEmpresaEntity e) {
        if (e == null) return null;
        UsuarioEmpresa d = new UsuarioEmpresa();
        d.setId(e.getId());
        d.setUsuarioId(e.getUsuario() != null ? e.getUsuario().getId() : null);
        d.setEmpresaId(e.getEmpresa() != null ? e.getEmpresa().getId() : null);
        d.setRolEmpresa(e.getRolEmpresa());
        d.setActivo(e.isActivo());
        return d;
    }

    public static UsuarioEmpresaEntity toEntity(UsuarioEmpresa d, UsuarioEntity usuario, EmpresaEntity empresa) {
        UsuarioEmpresaEntity e = new UsuarioEmpresaEntity();
        e.setId(d.getId());
        e.setUsuario(usuario);
        e.setEmpresa(empresa);
        e.setRolEmpresa(d.getRolEmpresa());
        e.setActivo(d.isActivo());
        return e;
    }
}
