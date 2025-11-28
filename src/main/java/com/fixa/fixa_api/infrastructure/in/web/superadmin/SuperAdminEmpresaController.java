package com.fixa.fixa_api.infrastructure.in.web.superadmin;

import com.fixa.fixa_api.application.service.EmpresaService;
import com.fixa.fixa_api.domain.model.Empresa;
import com.fixa.fixa_api.infrastructure.in.web.dto.EmpresaRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/superadmin/empresas")
public class SuperAdminEmpresaController {

    private final EmpresaService empresaService;

    public SuperAdminEmpresaController(EmpresaService empresaService) {
        this.empresaService = empresaService;
    }

    @GetMapping
    public ResponseEntity<java.util.List<Empresa>> listar() {
        return ResponseEntity.ok(empresaService.listar(null));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Empresa> obtener(@PathVariable Long id) {
        return empresaService.obtener(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Empresa> crear(@Valid @RequestBody EmpresaRequest req) {
        Empresa saved = empresaService.guardar(mapToDomain(null, req));
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Empresa> actualizar(@PathVariable Long id, @Valid @RequestBody EmpresaRequest req) {
        return empresaService.obtener(id)
                .map(existing -> ResponseEntity.ok(empresaService.guardar(mapToDomain(id, req))))
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/activar")
    public ResponseEntity<Void> activar(@PathVariable Long id, @RequestParam("activo") boolean activo) {
        boolean ok = empresaService.activar(id, activo);
        return ok ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    @PostMapping("/{id}/plan")
    public ResponseEntity<com.fixa.fixa_api.infrastructure.in.web.dto.SuscripcionResponse> asignarPlan(
            @PathVariable Long id,
            @Valid @RequestBody com.fixa.fixa_api.infrastructure.in.web.dto.AsignarPlanRequest req) {

        // Delegamos al servicio de suscripción a través de una inyección directa o vía
        // EmpresaService
        // Como EmpresaService ya tiene SuscripcionService, podríamos exponerlo allí,
        // pero para mantener separación de responsabilidades, inyectaremos
        // SuscripcionService aquí también.
        // O mejor, agregamos el método en EmpresaService para encapsular.

        // Por simplicidad y dado que no modifiqué el constructor para inyectar
        // SuscripcionService aquí,
        // voy a usar un método nuevo en EmpresaService que delegue.

        return empresaService.asignarPlan(id, req.getPlanId(), req.getPrecioPactado())
                .map(s -> {
                    com.fixa.fixa_api.infrastructure.in.web.dto.SuscripcionResponse resp = new com.fixa.fixa_api.infrastructure.in.web.dto.SuscripcionResponse();
                    resp.setId(s.getId());
                    resp.setEmpresaId(s.getEmpresaId());
                    resp.setPlanId(s.getPlanId());
                    resp.setPrecioPactado(s.getPrecioPactado());
                    resp.setFechaInicio(s.getFechaInicio());
                    resp.setFechaFin(s.getFechaFin());
                    resp.setActivo(s.isActivo());
                    return ResponseEntity.ok(resp);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    private Empresa mapToDomain(Long id, EmpresaRequest req) {
        Empresa d = new Empresa();
        d.setId(id);
        d.setNombre(req.getNombre());
        d.setDescripcion(req.getDescripcion());
        d.setDireccion(req.getDireccion());
        d.setTelefono(req.getTelefono());
        d.setEmail(req.getEmail());
        d.setBannerUrl(req.getBannerUrl());
        d.setLogoUrl(req.getLogoUrl());
        d.setPermiteReservasSinUsuario(req.isPermiteReservasSinUsuario());
        d.setRequiereValidacionTelefono(req.isRequiereValidacionTelefono());
        d.setRequiereAprobacionTurno(req.isRequiereAprobacionTurno());
        d.setMensajeValidacionPersonalizado(req.getMensajeValidacionPersonalizado());
        d.setVisibilidadPublica(req.isVisibilidadPublica());
        d.setActivo(req.isActivo());
        d.setCategoriaId(req.getCategoriaId());
        return d;
    }
}
