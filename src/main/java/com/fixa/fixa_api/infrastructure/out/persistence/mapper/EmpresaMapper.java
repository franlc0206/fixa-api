package com.fixa.fixa_api.infrastructure.out.persistence.mapper;

import com.fixa.fixa_api.domain.model.Empresa;
import com.fixa.fixa_api.infrastructure.out.persistence.entity.CategoriaEntity;
import com.fixa.fixa_api.infrastructure.out.persistence.entity.EmpresaEntity;

public class EmpresaMapper {

    public static Empresa toDomain(EmpresaEntity e) {
        if (e == null) return null;
        Empresa d = new Empresa();
        d.setId(e.getId());
        d.setUsuarioAdminId(e.getUsuarioAdmin() != null ? e.getUsuarioAdmin().getId() : null);
        d.setNombre(e.getNombre());
        d.setDescripcion(e.getDescripcion());
        d.setDireccion(e.getDireccion());
        d.setTelefono(e.getTelefono());
        d.setEmail(e.getEmail());
        d.setCategoriaId(e.getCategoria() != null ? e.getCategoria().getId() : null);
        d.setPermiteReservasSinUsuario(e.isPermiteReservasSinUsuario());
        d.setRequiereValidacionTelefono(e.isRequiereValidacionTelefono());
        d.setRequiereAprobacionTurno(e.isRequiereAprobacionTurno());
        d.setMensajeValidacionPersonalizado(e.getMensajeValidacionPersonalizado());
        d.setVisibilidadPublica(e.isVisibilidadPublica());
        d.setActivo(e.isActivo());
        return d;
    }

    public static void copyToEntity(Empresa d, EmpresaEntity e, CategoriaEntity categoria) {
        e.setNombre(d.getNombre());
        e.setDescripcion(d.getDescripcion());
        e.setDireccion(d.getDireccion());
        e.setTelefono(d.getTelefono());
        e.setEmail(d.getEmail());
        e.setPermiteReservasSinUsuario(d.isPermiteReservasSinUsuario());
        e.setRequiereValidacionTelefono(d.isRequiereValidacionTelefono());
        e.setRequiereAprobacionTurno(d.isRequiereAprobacionTurno());
        e.setMensajeValidacionPersonalizado(d.getMensajeValidacionPersonalizado());
        e.setVisibilidadPublica(d.isVisibilidadPublica());
        e.setActivo(d.isActivo());
        e.setCategoria(categoria);
    }
}
