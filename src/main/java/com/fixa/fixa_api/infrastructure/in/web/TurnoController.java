package com.fixa.fixa_api.infrastructure.in.web;

import com.fixa.fixa_api.application.usecase.AprobarTurnoUseCase;
import com.fixa.fixa_api.application.usecase.CrearTurnoUseCase;
import com.fixa.fixa_api.application.usecase.CancelarTurnoUseCase;
import com.fixa.fixa_api.application.usecase.CompletarTurnoUseCase;
import com.fixa.fixa_api.domain.model.Turno;
import com.fixa.fixa_api.infrastructure.in.web.dto.TurnoCreateRequest;
import com.fixa.fixa_api.application.service.TurnoQueryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/turnos")
public class TurnoController {

    private final CrearTurnoUseCase crearTurnoUseCase;
    private final AprobarTurnoUseCase aprobarTurnoUseCase;
    private final CancelarTurnoUseCase cancelarTurnoUseCase;
    private final CompletarTurnoUseCase completarTurnoUseCase;
    private final TurnoQueryService turnoQueryService;

    public TurnoController(CrearTurnoUseCase crearTurnoUseCase, AprobarTurnoUseCase aprobarTurnoUseCase,
                           CancelarTurnoUseCase cancelarTurnoUseCase, CompletarTurnoUseCase completarTurnoUseCase,
                           TurnoQueryService turnoQueryService) {
        this.crearTurnoUseCase = crearTurnoUseCase;
        this.aprobarTurnoUseCase = aprobarTurnoUseCase;
        this.cancelarTurnoUseCase = cancelarTurnoUseCase;
        this.completarTurnoUseCase = completarTurnoUseCase;
        this.turnoQueryService = turnoQueryService;
    }

    @GetMapping
    public ResponseEntity<java.util.List<Turno>> listar(
            @RequestParam(value = "empresaId", required = false) Long empresaId,
            @RequestParam(value = "empleadoId", required = false) Long empleadoId,
            @RequestParam(value = "estado", required = false) String estado,
            @RequestParam(value = "desde", required = false) java.time.LocalDateTime desde,
            @RequestParam(value = "hasta", required = false) java.time.LocalDateTime hasta,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size
    ) {
        var result = turnoQueryService.listar(empresaId, empleadoId, estado, desde, hasta, page, size);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Turno> obtener(@PathVariable("id") Long id) {
        return turnoQueryService.obtener(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Turno> crear(@Valid @RequestBody TurnoCreateRequest req) {
        Turno t = new Turno();
        t.setServicioId(req.getServicioId());
        t.setEmpleadoId(req.getEmpleadoId());
        t.setEmpresaId(req.getEmpresaId());
        t.setClienteId(req.getClienteId());
        t.setClienteNombre(req.getClienteNombre());
        t.setClienteApellido(req.getClienteApellido());
        t.setClienteTelefono(req.getClienteTelefono());
        t.setClienteDni(req.getClienteDni());
        t.setClienteEmail(req.getClienteEmail());
        t.setFechaHoraInicio(req.getFechaHoraInicio());
        t.setObservaciones(req.getObservaciones());
        Turno creado = crearTurnoUseCase.ejecutar(t);
        return ResponseEntity.ok(creado);
    }

    @PostMapping("/{id}/aprobar")
    public ResponseEntity<Turno> aprobar(@PathVariable("id") Long id) {
        Turno aprobado = aprobarTurnoUseCase.aprobar(id);
        return ResponseEntity.ok(aprobado);
    }

    public static class CancelarRequest {
        public String motivo;
        public String getMotivo() { return motivo; }
        public void setMotivo(String motivo) { this.motivo = motivo; }
    }

    @PostMapping("/{id}/cancelar")
    public ResponseEntity<Turno> cancelar(@PathVariable("id") Long id, @RequestBody(required = false) CancelarRequest body) {
        String motivo = body != null ? body.getMotivo() : null;
        Turno t = cancelarTurnoUseCase.cancelar(id, motivo);
        return ResponseEntity.ok(t);
    }

    @PostMapping("/{id}/completar")
    public ResponseEntity<Turno> completar(@PathVariable("id") Long id) {
        Turno t = completarTurnoUseCase.completar(id);
        return ResponseEntity.ok(t);
    }
}

