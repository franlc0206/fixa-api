package com.fixa.fixa_api.infrastructure.out.persistence.mapper;

import com.fixa.fixa_api.domain.model.Categoria;
import com.fixa.fixa_api.infrastructure.out.persistence.entity.CategoriaEntity;

public final class CategoriaMapper {
    private CategoriaMapper() {}

    public static Categoria toDomain(CategoriaEntity e) {
        if (e == null) return null;
        Categoria d = new Categoria();
        d.setId(e.getId());
        d.setTipo(e.getTipo());
        d.setNombre(e.getNombre());
        d.setDescripcion(e.getDescripcion());
        d.setActivo(e.isActivo());
        return d;
    }

    public static CategoriaEntity toEntity(Categoria d) {
        CategoriaEntity e = new CategoriaEntity();
        e.setId(d.getId());
        e.setTipo(d.getTipo());
        e.setNombre(d.getNombre());
        e.setDescripcion(d.getDescripcion());
        e.setActivo(d.isActivo());
        return e;
    }
}
