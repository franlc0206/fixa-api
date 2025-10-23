package com.fixa.fixa_api.infrastructure.in.web;

import com.fixa.fixa_api.infrastructure.in.web.dto.EmpresaRequest;
import com.fixa.fixa_api.application.service.EmpresaService;
import com.fixa.fixa_api.domain.model.Empresa;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/empresas")
public class EmpresaController {

    private final EmpresaService empresaService;

    public EmpresaController(EmpresaService empresaService) {
        this.empresaService = empresaService;
    }

    @GetMapping
    public ResponseEntity<List<Empresa>> listar(@RequestParam(value = "visibles", required = false) Boolean visibles) {
        List<Empresa> result = empresaService.listar(visibles);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Empresa> obtener(@PathVariable Long id) {
        return empresaService.obtener(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Empresa> crear(@Valid @RequestBody EmpresaRequest req) {
        Empresa saved = empresaService.guardar(armarDesdeRequest(null, req));
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Empresa> actualizar(@PathVariable Long id, @Valid @RequestBody EmpresaRequest req) {
        return empresaService.obtener(id)
                .map(existing -> ResponseEntity.ok(empresaService.guardar(armarDesdeRequest(id, req))))
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/activar")
    public ResponseEntity<Void> activar(@PathVariable Long id, @RequestParam("activo") boolean activo) {
        boolean ok = empresaService.activar(id, activo);
        if (!ok) {
            return ResponseEntity.status(404).build();
        }
        return ResponseEntity.noContent().build();
    }

    private Empresa armarDesdeRequest(Long id, EmpresaRequest req) {
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

