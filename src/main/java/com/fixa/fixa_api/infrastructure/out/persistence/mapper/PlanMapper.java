package com.fixa.fixa_api.infrastructure.out.persistence.mapper;

import com.fixa.fixa_api.domain.model.Plan;
import com.fixa.fixa_api.infrastructure.out.persistence.entity.PlanEntity;

public class PlanMapper {

    public static Plan toDomain(PlanEntity entity) {
        if (entity == null)
            return null;
        Plan plan = new Plan();
        plan.setId(entity.getId());
        plan.setNombre(entity.getNombre());
        plan.setPrecio(entity.getPrecio());
        plan.setMaxEmpleados(entity.getMaxEmpleados());
        plan.setMaxServicios(entity.getMaxServicios());
        plan.setMaxTurnosMensuales(entity.getMaxTurnosMensuales());
        plan.setSoportePrioritario(entity.isSoportePrioritario());
        plan.setActivo(entity.isActivo());
        plan.setMercadopagoPlanId(entity.getMercadopagoPlanId());
        return plan;
    }

    public static PlanEntity toEntity(Plan domain) {
        if (domain == null)
            return null;
        PlanEntity entity = new PlanEntity();
        entity.setId(domain.getId());
        entity.setNombre(domain.getNombre());
        entity.setPrecio(domain.getPrecio());
        entity.setMaxEmpleados(domain.getMaxEmpleados());
        entity.setMaxServicios(domain.getMaxServicios());
        entity.setMaxTurnosMensuales(domain.getMaxTurnosMensuales());
        entity.setSoportePrioritario(domain.isSoportePrioritario());
        entity.setActivo(domain.isActivo());
        entity.setMercadopagoPlanId(domain.getMercadopagoPlanId());
        return entity;
    }
}
