package com.fixa.fixa_api.infrastructure.out.persistence.mapper;

import com.fixa.fixa_api.domain.model.Empleado;
import com.fixa.fixa_api.infrastructure.out.persistence.entity.EmpleadoEntity;
import com.fixa.fixa_api.infrastructure.out.persistence.entity.EmpresaEntity;
import com.fixa.fixa_api.infrastructure.out.persistence.entity.UsuarioEntity;

public class EmpleadoMapper {
    public static Empleado toDomain(EmpleadoEntity e) {
        if (e == null) return null;
        Empleado d = new Empleado();
        d.setId(e.getId());
        d.setEmpresaId(e.getEmpresa() != null ? e.getEmpresa().getId() : null);
        d.setUsuarioId(e.getUsuario() != null ? e.getUsuario().getId() : null);
        d.setNombre(e.getNombre());
        d.setApellido(e.getApellido());
        d.setRol(e.getRol());
        d.setFotoUrl(e.getFotoUrl());
        d.setTrabajaPublicamente(e.isTrabajaPublicamente());
        d.setActivo(e.isActivo());
        return d;
    }

    public static void copyToEntity(Empleado d, EmpleadoEntity e, EmpresaEntity empresa, UsuarioEntity usuario) {
        e.setEmpresa(empresa);
        e.setUsuario(usuario);
        e.setNombre(d.getNombre());
        e.setApellido(d.getApellido());
        e.setRol(d.getRol());
        e.setFotoUrl(d.getFotoUrl());
        e.setTrabajaPublicamente(d.isTrabajaPublicamente());
        e.setActivo(d.isActivo());
    }
}
