package com.fixa.fixa_api.application.service;

import com.fixa.fixa_api.domain.model.Plan;
import com.fixa.fixa_api.domain.model.Suscripcion;
import com.fixa.fixa_api.domain.repository.PlanRepositoryPort;
import com.fixa.fixa_api.domain.repository.SuscripcionRepositoryPort;
import com.fixa.fixa_api.infrastructure.in.web.error.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class SuperAdminSuscripcionService {

    private final SuscripcionRepositoryPort suscripcionPort;
    private final PlanRepositoryPort planPort;

    public SuperAdminSuscripcionService(SuscripcionRepositoryPort suscripcionPort, PlanRepositoryPort planPort) {
        this.suscripcionPort = suscripcionPort;
        this.planPort = planPort;
    }

    // ABM básico

    public List<Suscripcion> listAll() {
        return suscripcionPort.findAll();
    }

    public Optional<Suscripcion> getById(Long id) {
        return suscripcionPort.findById(id);
    }

    public List<Suscripcion> listByEmpresa(Long empresaId) {
        return suscripcionPort.findByEmpresaId(empresaId);
    }

    public List<Suscripcion> listByPlan(Long planId) {
        // implementación simple: filtrar en memoria
        return suscripcionPort.findAll().stream()
                .filter(s -> s.getPlanId().equals(planId))
                .toList();
    }

    public Suscripcion createManual(Long empresaId, Long planId, BigDecimal precioPactado,
                                    LocalDateTime fechaInicio, LocalDateTime fechaFin,
                                    Boolean activo) {
        if (empresaId == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "empresaId requerido");
        }
        if (planId == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "planId requerido");
        }

        Plan plan = planPort.findById(planId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Plan no encontrado"));

        BigDecimal precioFinal = precioPactado != null ? precioPactado : plan.getPrecio();
        if (precioFinal == null || precioFinal.compareTo(BigDecimal.ZERO) < 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "precioPactado inválido");
        }

        Suscripcion s = new Suscripcion();
        s.setEmpresaId(empresaId);
        s.setPlanId(planId);
        s.setPrecioPactado(precioFinal);
        s.setFechaInicio(fechaInicio != null ? fechaInicio : LocalDateTime.now());
        s.setFechaFin(fechaFin);
        s.setActivo(activo != null ? activo : true);

        return suscripcionPort.save(s);
    }

    public Optional<Suscripcion> update(Long id, Long empresaId, Long planId, BigDecimal precioPactado,
                                        LocalDateTime fechaInicio, LocalDateTime fechaFin,
                                        Boolean activo) {
        return suscripcionPort.findById(id).map(s -> {
            if (empresaId != null) s.setEmpresaId(empresaId);
            if (planId != null) {
                Plan plan = planPort.findById(planId)
                        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Plan no encontrado"));
                s.setPlanId(plan.getId());
            }
            if (precioPactado != null) {
                if (precioPactado.compareTo(BigDecimal.ZERO) < 0) {
                    throw new ApiException(HttpStatus.BAD_REQUEST, "precioPactado inválido");
                }
                s.setPrecioPactado(precioPactado);
            }
            if (fechaInicio != null) s.setFechaInicio(fechaInicio);
            if (fechaFin != null) s.setFechaFin(fechaFin);
            if (activo != null) s.setActivo(activo);
            return suscripcionPort.save(s);
        });
    }

    public boolean activar(Long id, boolean activo) {
        Optional<Suscripcion> opt = suscripcionPort.findById(id);
        if (opt.isEmpty()) return false;
        Suscripcion s = opt.get();
        s.setActivo(activo);
        if (!activo && s.getFechaFin() == null) {
            s.setFechaFin(LocalDateTime.now());
        }
        suscripcionPort.save(s);
        return true;
    }

    // Método existente para asignar/cambiar plan activo de una empresa

    public Suscripcion asignarPlanALaEmpresa(Long empresaId, Long planId, BigDecimal precioPactado) {
        if (empresaId == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "empresaId requerido");
        }
        if (planId == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "planId requerido");
        }

        Plan plan = planPort.findById(planId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Plan no encontrado"));

        if (!plan.isActivo()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "El plan no está activo");
        }

        BigDecimal precioFinal = precioPactado != null ? precioPactado : plan.getPrecio();
        if (precioFinal == null || precioFinal.compareTo(BigDecimal.ZERO) < 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "precioPactado inválido");
        }

        suscripcionPort.findActivaByEmpresaId(empresaId).ifPresent(s -> {
            s.setActivo(false);
            s.setFechaFin(LocalDateTime.now());
            suscripcionPort.save(s);
        });

        Suscripcion nueva = new Suscripcion();
        nueva.setEmpresaId(empresaId);
        nueva.setPlanId(planId);
        nueva.setPrecioPactado(precioFinal);
        nueva.setFechaInicio(LocalDateTime.now());
        nueva.setFechaFin(null);
        nueva.setActivo(true);

        return suscripcionPort.save(nueva);
    }
}
