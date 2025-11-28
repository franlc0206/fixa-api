package com.fixa.fixa_api.infrastructure.in.web.superadmin;

import com.fixa.fixa_api.application.service.SuperAdminSuscripcionService;
import com.fixa.fixa_api.domain.model.Suscripcion;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/superadmin/suscripciones")
public class SuperAdminSuscripcionAbmController {

    private final SuperAdminSuscripcionService service;

    public SuperAdminSuscripcionAbmController(SuperAdminSuscripcionService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<Suscripcion>> list(
            @RequestParam(required = false) Long empresaId,
            @RequestParam(required = false) Long planId) {
        if (empresaId != null) {
            return ResponseEntity.ok(service.listByEmpresa(empresaId));
        }
        if (planId != null) {
            return ResponseEntity.ok(service.listByPlan(planId));
        }
        return ResponseEntity.ok(service.listAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Suscripcion> getById(@PathVariable Long id) {
        return service.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    public static class CreateSuscripcionRequest {
        public Long empresaId;
        public Long planId;
        public BigDecimal precioPactado;
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        public LocalDateTime fechaInicio;
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        public LocalDateTime fechaFin;
        public Boolean activo;
    }

    @PostMapping
    public ResponseEntity<Suscripcion> create(@RequestBody CreateSuscripcionRequest req) {
        Suscripcion s = service.createManual(req.empresaId, req.planId, req.precioPactado,
                req.fechaInicio, req.fechaFin, req.activo);
        return ResponseEntity.ok(s);
    }

    public static class UpdateSuscripcionRequest {
        public Long empresaId;
        public Long planId;
        public BigDecimal precioPactado;
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        public LocalDateTime fechaInicio;
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        public LocalDateTime fechaFin;
        public Boolean activo;
    }

    @PutMapping("/{id}")
    public ResponseEntity<Suscripcion> update(@PathVariable Long id, @RequestBody UpdateSuscripcionRequest req) {
        return service.update(id, req.empresaId, req.planId, req.precioPactado,
                        req.fechaInicio, req.fechaFin, req.activo)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/activar")
    public ResponseEntity<Void> activar(@PathVariable Long id, @RequestParam("activo") boolean activo) {
        boolean ok = service.activar(id, activo);
        return ok ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
