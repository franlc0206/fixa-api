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

    private Empresa mapToDomain(Long id, EmpresaRequest req) {
        Empresa d = new Empresa();
        d.setId(id);
        d.setNombre(req.getNombre());
        d.setDescripcion(req.getDescripcion());
        d.setDireccion(req.getDireccion());
        d.setTelefono(req.getTelefono());
        d.setEmail(req.getEmail());
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
