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
import java.util.Optional;

@Service
public class SuscripcionService {

    private final SuscripcionRepositoryPort suscripcionPort;
    private final PlanRepositoryPort planPort;

    public SuscripcionService(SuscripcionRepositoryPort suscripcionPort, PlanRepositoryPort planPort) {
        this.suscripcionPort = suscripcionPort;
        this.planPort = planPort;
    }

    public Optional<Suscripcion> obtenerSuscripcionActiva(Long empresaId) {
        // Buscar suscripción activa (activo = true y fechaFin > now o null)
        // Como el repositorio no tiene un método específico complejo, filtramos en
        // memoria o asumimos que
        // la lógica de negocio garantiza una única suscripción activa.
        // Por ahora, buscamos la última suscripción activa.
        // TODO: Implementar método específico en repositorio si es necesario optimizar
        return suscripcionPort.findByEmpresaId(empresaId).stream()
                .filter(s -> s.isActivo())
                .filter(s -> s.getFechaFin() == null || s.getFechaFin().isAfter(LocalDateTime.now()))
                .findFirst();
    }

    public Plan obtenerPlanActual(Long empresaId) {
        return obtenerSuscripcionActiva(empresaId)
                .flatMap(s -> planPort.findById(s.getPlanId()))
                .orElseThrow(
                        () -> new ApiException(HttpStatus.FORBIDDEN, "La empresa no tiene una suscripción activa"));
    }

    public void validarSuscripcionActiva(Long empresaId) {
        obtenerSuscripcionActiva(empresaId)
                .orElseThrow(() -> new ApiException(HttpStatus.FORBIDDEN, "SUBSCRIPTION_EXPIRED",
                        "Su suscripción ha expirado. Contacte al administrador."));
    }

    public Suscripcion asignarPlan(Long empresaId, Long planId, BigDecimal precioPactado) {
        Plan plan = planPort.findById(planId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Plan no encontrado"));

        // Desactivar suscripciones anteriores
        suscripcionPort.findByEmpresaId(empresaId).stream()
                .filter(Suscripcion::isActivo)
                .forEach(s -> {
                    s.setActivo(false);
                    s.setFechaFin(LocalDateTime.now());
                    suscripcionPort.save(s);
                });

        Suscripcion nueva = new Suscripcion();
        nueva.setEmpresaId(empresaId);
        nueva.setPlanId(planId);
        nueva.setPrecioPactado(precioPactado != null ? precioPactado : plan.getPrecio());
        nueva.setFechaInicio(LocalDateTime.now());
        nueva.setActivo(true);

        return suscripcionPort.save(nueva);
    }
}
