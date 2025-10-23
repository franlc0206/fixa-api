package com.fixa.fixa_api.infrastructure.in.web;

import com.fixa.fixa_api.application.usecase.CrearTurnoUseCase;
import com.fixa.fixa_api.domain.model.Turno;
import com.fixa.fixa_api.infrastructure.in.web.dto.TurnoCreateRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public/turnos")
public class PublicTurnoController {

    private final CrearTurnoUseCase crearTurnoUseCase;

    public PublicTurnoController(CrearTurnoUseCase crearTurnoUseCase) {
        this.crearTurnoUseCase = crearTurnoUseCase;
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
}
