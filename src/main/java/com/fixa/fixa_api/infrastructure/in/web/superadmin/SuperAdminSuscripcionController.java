package com.fixa.fixa_api.infrastructure.in.web.superadmin;

import com.fixa.fixa_api.application.service.SuperAdminSuscripcionService;
import com.fixa.fixa_api.domain.model.Suscripcion;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/superadmin/empresas")
public class SuperAdminSuscripcionController {

    private final SuperAdminSuscripcionService service;

    public SuperAdminSuscripcionController(SuperAdminSuscripcionService service) {
        this.service = service;
    }

    public static class AsignarPlanRequest {
        @NotNull
        public Long planId;
        public BigDecimal precioPactado; // opcional, si null se usa el precio del plan
    }

    @PostMapping("/{empresaId}/plan")
    public ResponseEntity<Suscripcion> asignarPlan(@PathVariable Long empresaId,
                                                   @RequestBody AsignarPlanRequest req) {
        Suscripcion suscripcion = service.asignarPlanALaEmpresa(empresaId, req.planId, req.precioPactado);
        return ResponseEntity.ok(suscripcion);
    }
}
