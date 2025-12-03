package com.fixa.fixa_api.infrastructure.in.web.superadmin;

import com.fixa.fixa_api.application.service.SuperAdminRelacionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/superadmin/relaciones")
public class SuperAdminRelacionesController {

    private final SuperAdminRelacionService service;

    public SuperAdminRelacionesController(SuperAdminRelacionService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<?> list(
            @RequestParam(value = "usuarioId", required = false) Long usuarioId,
            @RequestParam(value = "empresaId", required = false) Long empresaId,
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "size", required = false, defaultValue = "20") int size) {
        if (usuarioId != null) {
            List<SuperAdminRelacionService.RelacionDTO> items = service.listByUsuario(usuarioId);
            return ResponseEntity.ok(items);
        } else if (empresaId != null) {
            List<SuperAdminRelacionService.RelacionDTO> items = service.listByEmpresa(empresaId);
            return ResponseEntity.ok(items);
        } else {
            return ResponseEntity.ok(service.listAllPaged(page, size));
        }
    }

    @PostMapping
    public ResponseEntity<SuperAdminRelacionService.RelacionDTO> add(
            @RequestBody SuperAdminRelacionService.RelacionDTO req) {
        return ResponseEntity.ok(service.add(req));
    }

    @DeleteMapping
    public ResponseEntity<Void> remove(@RequestParam("usuarioId") Long usuarioId,
            @RequestParam("empresaId") Long empresaId) {
        service.remove(usuarioId, empresaId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/activar")
    public ResponseEntity<Void> activar(@RequestParam("usuarioId") Long usuarioId,
            @RequestParam("empresaId") Long empresaId,
            @RequestParam("activo") boolean activo) {
        service.activar(usuarioId, empresaId, activo);
        return ResponseEntity.noContent().build();
    }
}
