package com.fixa.fixa_api.infrastructure.out.persistence.mapper;

import com.fixa.fixa_api.domain.model.Empresa;
import com.fixa.fixa_api.infrastructure.out.persistence.entity.CategoriaEntity;
import com.fixa.fixa_api.infrastructure.out.persistence.entity.EmpresaEntity;
import com.fixa.fixa_api.infrastructure.out.persistence.entity.UsuarioEntity;

public class EmpresaMapper {

    public static Empresa toDomain(EmpresaEntity e) {
        if (e == null)
            return null;
        Empresa d = new Empresa();
        d.setId(e.getId());
        d.setUsuarioAdminId(e.getUsuarioAdmin() != null ? e.getUsuarioAdmin().getId() : null);
        d.setNombre(e.getNombre());
        d.setSlug(e.getSlug());
        d.setDescripcion(e.getDescripcion());
        d.setDireccion(e.getDireccion());
        d.setTelefono(e.getTelefono());
        d.setEmail(e.getEmail());
        d.setBannerUrl(e.getBannerUrl());
        d.setLogoUrl(e.getLogoUrl());
        d.setCategoriaId(e.getCategoria() != null ? e.getCategoria().getId() : null);
        d.setPermiteReservasSinUsuario(e.isPermiteReservasSinUsuario());
        d.setRequiereValidacionTelefono(e.isRequiereValidacionTelefono());
        d.setRequiereAprobacionTurno(e.isRequiereAprobacionTurno());
        d.setMensajeValidacionPersonalizado(e.getMensajeValidacionPersonalizado());
        d.setVisibilidadPublica(e.isVisibilidadPublica());
        d.setLatitud(e.getLatitud());
        d.setLongitud(e.getLongitud());
        d.setActivo(e.isActivo());
        if (e.getPlanActual() != null) {
            d.setPlanActualId(e.getPlanActual().getId());
        }
        return d;
    }

    public static void copyToEntity(Empresa d, EmpresaEntity e, CategoriaEntity categoria, UsuarioEntity admin,
            com.fixa.fixa_api.infrastructure.out.persistence.entity.PlanEntity plan) {
        e.setNombre(d.getNombre());
        e.setSlug(d.getSlug());
        e.setDescripcion(d.getDescripcion());
        e.setDireccion(d.getDireccion());
        e.setTelefono(d.getTelefono());
        e.setEmail(d.getEmail());
        e.setBannerUrl(d.getBannerUrl());
        e.setLogoUrl(d.getLogoUrl());
        e.setPermiteReservasSinUsuario(d.isPermiteReservasSinUsuario());
        e.setRequiereValidacionTelefono(d.isRequiereValidacionTelefono());
        e.setRequiereAprobacionTurno(d.isRequiereAprobacionTurno());
        e.setMensajeValidacionPersonalizado(d.getMensajeValidacionPersonalizado());
        e.setVisibilidadPublica(d.isVisibilidadPublica());
        e.setLatitud(d.getLatitud());
        e.setLongitud(d.getLongitud());
        e.setActivo(d.isActivo());
        e.setCategoria(categoria);
        e.setUsuarioAdmin(admin);
        e.setPlanActual(plan);
    }
}
