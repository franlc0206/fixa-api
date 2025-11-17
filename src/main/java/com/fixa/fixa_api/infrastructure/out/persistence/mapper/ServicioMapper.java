package com.fixa.fixa_api.infrastructure.out.persistence.mapper;

import com.fixa.fixa_api.domain.model.Servicio;
import com.fixa.fixa_api.infrastructure.out.persistence.entity.CategoriaEntity;
import com.fixa.fixa_api.infrastructure.out.persistence.entity.EmpresaEntity;
import com.fixa.fixa_api.infrastructure.out.persistence.entity.ServicioEntity;

public class ServicioMapper {
    public static Servicio toDomain(ServicioEntity e) {
        if (e == null) return null;
        Servicio d = new Servicio();
        d.setId(e.getId());
        d.setEmpresaId(e.getEmpresa() != null ? e.getEmpresa().getId() : null);
        d.setNombre(e.getNombre());
        d.setDescripcion(e.getDescripcion());
        d.setDuracionMinutos(e.getDuracionMinutos());
        d.setRequiereEspacioLibre(e.isRequiereEspacioLibre());
        d.setPatronBloques(e.getPatronBloques());
        d.setCosto(e.getCosto());
        d.setRequiereSena(e.isRequiereSena());
        d.setActivo(e.isActivo());
        d.setCategoriaId(e.getCategoria() != null ? e.getCategoria().getId() : null);
        d.setFotoUrl(e.getFotoUrl());
        return d;
    }

    public static void copyToEntity(Servicio d, ServicioEntity e, EmpresaEntity empresa, CategoriaEntity categoria) {
        e.setEmpresa(empresa);
        e.setNombre(d.getNombre());
        e.setDescripcion(d.getDescripcion());
        e.setDuracionMinutos(d.getDuracionMinutos());
        e.setRequiereEspacioLibre(d.isRequiereEspacioLibre());
        e.setPatronBloques(d.getPatronBloques());
        e.setCosto(d.getCosto());
        e.setRequiereSena(d.isRequiereSena());
        e.setActivo(d.isActivo());
        e.setCategoria(categoria);
        e.setFotoUrl(d.getFotoUrl());
    }
}
