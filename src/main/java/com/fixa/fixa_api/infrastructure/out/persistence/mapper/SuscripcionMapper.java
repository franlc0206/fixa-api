package com.fixa.fixa_api.infrastructure.out.persistence.mapper;

import com.fixa.fixa_api.domain.model.Suscripcion;
import com.fixa.fixa_api.infrastructure.out.persistence.entity.SuscripcionEntity;

public class SuscripcionMapper {

    public static Suscripcion toDomain(SuscripcionEntity entity) {
        if (entity == null)
            return null;
        Suscripcion suscripcion = new Suscripcion();
        suscripcion.setId(entity.getId());
        suscripcion.setEmpresaId(entity.getEmpresaId());
        suscripcion.setPlanId(entity.getPlanId());
        suscripcion.setPrecioPactado(entity.getPrecioPactado());
        suscripcion.setFechaInicio(entity.getFechaInicio());
        suscripcion.setFechaFin(entity.getFechaFin());
        suscripcion.setActivo(entity.isActivo());
        return suscripcion;
    }

    public static SuscripcionEntity toEntity(Suscripcion domain) {
        if (domain == null)
            return null;
        SuscripcionEntity entity = new SuscripcionEntity();
        entity.setId(domain.getId());
        entity.setEmpresaId(domain.getEmpresaId());
        entity.setPlanId(domain.getPlanId());
        entity.setPrecioPactado(domain.getPrecioPactado());
        entity.setFechaInicio(domain.getFechaInicio());
        entity.setFechaFin(domain.getFechaFin());
        entity.setActivo(domain.isActivo());
        return entity;
    }
}
