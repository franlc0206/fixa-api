package com.fixa.fixa_api.infrastructure.in.web;

import com.fixa.fixa_api.infrastructure.in.web.dto.EmpleadoRequest;
import com.fixa.fixa_api.application.service.EmpleadoService;
import com.fixa.fixa_api.domain.model.Empleado;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class EmpleadoController {

    private final EmpleadoService empleadoService;

    public EmpleadoController(EmpleadoService empleadoService) {
        this.empleadoService = empleadoService;
    }

    @GetMapping("/api/empresas/{empresaId}/empleados")
    public ResponseEntity<List<Empleado>> listar(@PathVariable Long empresaId,
                                                 @RequestParam(value = "activo", required = false) Boolean activo,
                                                 @RequestParam(value = "visibles", required = false) Boolean visibles,
                                                 @RequestParam(value = "page", required = false) Integer page,
                                                 @RequestParam(value = "size", required = false) Integer size) {
        if (page != null && size != null) {
            return ResponseEntity.ok(empleadoService.listarPorEmpresaPaginado(empresaId, activo, visibles, page, size));
        }
        if (activo == null && visibles == null) {
            return ResponseEntity.ok(empleadoService.listarPorEmpresa(empresaId));
        }
        return ResponseEntity.ok(empleadoService.listarPorEmpresa(empresaId, activo, visibles));
    }

    @PostMapping("/api/empresas/{empresaId}/empleados")
    public ResponseEntity<Empleado> crear(@PathVariable Long empresaId, @Valid @RequestBody EmpleadoRequest req) {
        Empleado d = new Empleado();
        d.setEmpresaId(empresaId);
        d.setNombre(req.getNombre());
        d.setApellido(req.getApellido());
        d.setRol(req.getRol());
        d.setTrabajaPublicamente(req.isTrabajaPublicamente());
        d.setActivo(req.isActivo());
        return ResponseEntity.ok(empleadoService.guardar(d));
    }

    @GetMapping("/api/empleados/{id}")
    public ResponseEntity<Empleado> obtener(@PathVariable Long id) {
        return empleadoService.obtener(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/api/empleados/{id}")
    public ResponseEntity<Empleado> actualizar(@PathVariable Long id, @Valid @RequestBody EmpleadoRequest req) {
        return empleadoService.obtener(id).map(existing -> {
            Empleado d = new Empleado();
            d.setId(id);
            d.setEmpresaId(req.getEmpresaId());
            d.setNombre(req.getNombre());
            d.setApellido(req.getApellido());
            d.setRol(req.getRol());
            d.setTrabajaPublicamente(req.isTrabajaPublicamente());
            d.setActivo(req.isActivo());
            return ResponseEntity.ok(empleadoService.guardar(d));
        }).orElse(ResponseEntity.notFound().build());
    }

    // Nuevos endpoints anidados por empresa (consistencia REST + guards FE)
    @GetMapping("/api/empresas/{empresaId}/empleados/{id}")
    public ResponseEntity<Empleado> obtenerPorEmpresa(@PathVariable Long empresaId, @PathVariable Long id) {
        return empleadoService.obtener(id)
                .filter(e -> e.getEmpresaId() != null && e.getEmpresaId().equals(empresaId))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/api/empresas/{empresaId}/empleados/{id}")
    public ResponseEntity<Empleado> actualizarPorEmpresa(@PathVariable Long empresaId,
                                                         @PathVariable Long id,
                                                         @Valid @RequestBody EmpleadoRequest req) {
        return empleadoService.obtener(id)
                .filter(e -> e.getEmpresaId() != null && e.getEmpresaId().equals(empresaId))
                .map(existing -> {
                    Empleado d = new Empleado();
                    d.setId(id);
                    d.setEmpresaId(empresaId); // se toma del path para evitar tampering
                    d.setNombre(req.getNombre());
                    d.setApellido(req.getApellido());
                    d.setRol(req.getRol());
                    d.setTrabajaPublicamente(req.isTrabajaPublicamente());
                    d.setActivo(req.isActivo());
                    return ResponseEntity.ok(empleadoService.guardar(d));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/api/empleados/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (!empleadoService.eliminar(id)) return ResponseEntity.notFound().build();
        return ResponseEntity.noContent().build();
    }
}
