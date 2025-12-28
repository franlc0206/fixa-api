package com.fixa.fixa_api.infrastructure.in.web.superadmin;

import com.fixa.fixa_api.application.service.SuperAdminPlanService;
import com.fixa.fixa_api.domain.model.Plan;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/superadmin/planes")
public class SuperAdminPlanController {

    private final SuperAdminPlanService service;

    public SuperAdminPlanController(SuperAdminPlanService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<Plan>> list() {
        return ResponseEntity.ok(service.list());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Plan> getById(@PathVariable Long id) {
        return service.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    public static class CreatePlanRequest {
        @NotBlank
        public String nombre;
        @NotNull
        @Min(0)
        public BigDecimal precio;
        @Min(1)
        public int maxEmpleados;
        @Min(1)
        public int maxServicios;
        @Min(1)
        public int maxTurnosMensuales;
        public boolean soportePrioritario;
        public boolean activo = true;
        public String mercadopagoPlanId;
    }

    @PostMapping
    public ResponseEntity<Plan> create(@Valid @RequestBody CreatePlanRequest req) {
        Plan saved = service.create(req.nombre, req.precio, req.maxEmpleados, req.maxServicios,
                req.maxTurnosMensuales, req.soportePrioritario, req.activo, req.mercadopagoPlanId);
        return ResponseEntity.ok(saved);
    }

    public static class UpdatePlanRequest {
        public String nombre;
        public BigDecimal precio;
        public Integer maxEmpleados;
        public Integer maxServicios;
        public Integer maxTurnosMensuales;
        public Boolean soportePrioritario;
        public Boolean activo;
        public String mercadopagoPlanId;
    }

    @PutMapping("/{id}")
    public ResponseEntity<Plan> update(@PathVariable Long id, @RequestBody UpdatePlanRequest req) {
        return service.update(id, req.nombre, req.precio, req.maxEmpleados, req.maxServicios,
                req.maxTurnosMensuales, req.soportePrioritario, req.activo, req.mercadopagoPlanId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/activar")
    public ResponseEntity<Void> activar(@PathVariable Long id, @RequestParam("activo") boolean activo) {
        boolean ok = service.activar(id, activo);
        return ok ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
