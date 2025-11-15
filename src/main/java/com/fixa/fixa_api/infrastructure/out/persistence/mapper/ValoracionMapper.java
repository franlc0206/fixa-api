package com.fixa.fixa_api.infrastructure.out.persistence.mapper;

import com.fixa.fixa_api.domain.model.Valoracion;
import com.fixa.fixa_api.infrastructure.out.persistence.entity.ValoracionEntity;
import com.fixa.fixa_api.infrastructure.out.persistence.entity.EmpresaEntity;
import com.fixa.fixa_api.infrastructure.out.persistence.entity.UsuarioEntity;
import com.fixa.fixa_api.infrastructure.out.persistence.entity.TurnoEntity;

public class ValoracionMapper {

    public static Valoracion toDomain(ValoracionEntity e) {
        if (e == null) return null;
        Valoracion d = new Valoracion();
        d.setId(e.getId());
        d.setEmpresaId(e.getEmpresa() != null ? e.getEmpresa().getId() : null);
        d.setUsuarioId(e.getUsuario() != null ? e.getUsuario().getId() : null);
        d.setTurnoId(e.getTurno() != null ? e.getTurno().getId() : null);
        d.setPuntuacion(e.getPuntuacion());
        d.setResena(e.getResena());
        d.setFechaCreacion(e.getFechaCreacion());
        d.setActivo(e.isActivo());
        return d;
    }

    public static void copyToEntity(
            Valoracion d,
            ValoracionEntity e,
            EmpresaEntity empresa,
            UsuarioEntity usuario,
            TurnoEntity turno
    ) {
        e.setEmpresa(empresa);
        e.setUsuario(usuario);
        e.setTurno(turno);
        e.setPuntuacion(d.getPuntuacion());
        e.setResena(d.getResena());
        e.setFechaCreacion(d.getFechaCreacion());
        e.setActivo(d.isActivo());
    }
}
