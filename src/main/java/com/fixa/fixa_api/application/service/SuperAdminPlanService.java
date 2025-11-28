package com.fixa.fixa_api.application.service;

import com.fixa.fixa_api.domain.model.Plan;
import com.fixa.fixa_api.domain.repository.PlanRepositoryPort;
import com.fixa.fixa_api.infrastructure.in.web.error.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class SuperAdminPlanService {

    private final PlanRepositoryPort planPort;

    public SuperAdminPlanService(PlanRepositoryPort planPort) {
        this.planPort = planPort;
    }

    public List<Plan> list() {
        return planPort.findAll();
    }

    public Optional<Plan> getById(Long id) {
        return planPort.findById(id);
    }

    public Plan create(String nombre, BigDecimal precio, int maxEmpleados, int maxServicios,
                       int maxTurnosMensuales, boolean soportePrioritario, boolean activo) {
        validate(nombre, precio, maxEmpleados, maxServicios, maxTurnosMensuales);
        Plan p = new Plan();
        p.setNombre(nombre);
        p.setPrecio(precio);
        p.setMaxEmpleados(maxEmpleados);
        p.setMaxServicios(maxServicios);
        p.setMaxTurnosMensuales(maxTurnosMensuales);
        p.setSoportePrioritario(soportePrioritario);
        p.setActivo(activo);
        return planPort.save(p);
    }

    public Optional<Plan> update(Long id, String nombre, BigDecimal precio, Integer maxEmpleados,
                                 Integer maxServicios, Integer maxTurnosMensuales,
                                 Boolean soportePrioritario, Boolean activo) {
        return planPort.findById(id).map(p -> {
            if (nombre != null) p.setNombre(nombre);
            if (precio != null) p.setPrecio(precio);
            if (maxEmpleados != null) p.setMaxEmpleados(maxEmpleados);
            if (maxServicios != null) p.setMaxServicios(maxServicios);
            if (maxTurnosMensuales != null) p.setMaxTurnosMensuales(maxTurnosMensuales);
            if (soportePrioritario != null) p.setSoportePrioritario(soportePrioritario);
            if (activo != null) p.setActivo(activo);
            validate(p.getNombre(), p.getPrecio(), p.getMaxEmpleados(), p.getMaxServicios(), p.getMaxTurnosMensuales());
            return planPort.save(p);
        });
    }

    public boolean activar(Long id, boolean activo) {
        Optional<Plan> opt = planPort.findById(id);
        if (opt.isEmpty()) return false;
        Plan p = opt.get();
        p.setActivo(activo);
        planPort.save(p);
        return true;
    }

    private void validate(String nombre, BigDecimal precio, int maxEmpleados, int maxServicios, int maxTurnosMensuales) {
        if (nombre == null || nombre.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "nombre requerido");
        }
        if (precio == null || precio.compareTo(BigDecimal.ZERO) < 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "precio debe ser >= 0");
        }
        if (maxEmpleados <= 0 || maxServicios <= 0 || maxTurnosMensuales <= 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "los limites deben ser > 0");
        }
    }
}
