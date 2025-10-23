package com.fixa.fixa_api.infrastructure.in.web;

import com.fixa.fixa_api.application.usecase.AprobarTurnoUseCase;
import com.fixa.fixa_api.application.usecase.CrearTurnoUseCase;
import com.fixa.fixa_api.application.usecase.CancelarTurnoUseCase;
import com.fixa.fixa_api.application.usecase.CompletarTurnoUseCase;
import com.fixa.fixa_api.domain.model.Turno;
import com.fixa.fixa_api.infrastructure.in.web.dto.TurnoCreateRequest;
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

    public TurnoController(CrearTurnoUseCase crearTurnoUseCase, AprobarTurnoUseCase aprobarTurnoUseCase,
                           CancelarTurnoUseCase cancelarTurnoUseCase, CompletarTurnoUseCase completarTurnoUseCase) {
        this.crearTurnoUseCase = crearTurnoUseCase;
        this.aprobarTurnoUseCase = aprobarTurnoUseCase;
        this.cancelarTurnoUseCase = cancelarTurnoUseCase;
        this.completarTurnoUseCase = completarTurnoUseCase;
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

