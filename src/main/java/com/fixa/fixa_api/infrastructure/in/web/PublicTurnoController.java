package com.fixa.fixa_api.infrastructure.in.web;

import com.fixa.fixa_api.application.service.TurnoQueryService;
import com.fixa.fixa_api.application.usecase.CrearTurnoUseCase;
import com.fixa.fixa_api.application.usecase.CrearVerificacionUseCase;
import com.fixa.fixa_api.domain.model.Turno;
import com.fixa.fixa_api.domain.model.VerificacionTelefono;
import com.fixa.fixa_api.domain.repository.UsuarioRepositoryPort;
import com.fixa.fixa_api.infrastructure.in.web.dto.TurnoCreateRequest;
import com.fixa.fixa_api.infrastructure.in.web.dto.TurnoPublicoResponse;
import com.fixa.fixa_api.infrastructure.security.CurrentUserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/public/turnos")
public class PublicTurnoController {

    private final CrearTurnoUseCase crearTurnoUseCase;
    private final CrearVerificacionUseCase crearVerificacionUseCase;
    private final TurnoQueryService turnoQueryService;
    private final CurrentUserService currentUserService;
    private final UsuarioRepositoryPort usuarioRepositoryPort;

    public PublicTurnoController(
            CrearTurnoUseCase crearTurnoUseCase,
            CrearVerificacionUseCase crearVerificacionUseCase,
            TurnoQueryService turnoQueryService,
            CurrentUserService currentUserService,
            UsuarioRepositoryPort usuarioRepositoryPort) {
        this.crearTurnoUseCase = crearTurnoUseCase;
        this.crearVerificacionUseCase = crearVerificacionUseCase;
        this.turnoQueryService = turnoQueryService;
        this.currentUserService = currentUserService;
        this.usuarioRepositoryPort = usuarioRepositoryPort;
    }

    /**
     * Obtener turnos públicos para ver disponibilidad
     * Solo devuelve turnos CONFIRMADOS y PENDIENTES para mostrar horarios ocupados
     */
    @GetMapping
    public ResponseEntity<List<Turno>> listarTurnosPublicos(
            @RequestParam(required = false) Long empleadoId,
            @RequestParam(required = false) String fecha) { // formato: yyyy-MM-dd

        LocalDateTime desde = null;
        LocalDateTime hasta = null;

        if (fecha != null) {
            LocalDate fechaLocal = LocalDate.parse(fecha);
            desde = fechaLocal.atStartOfDay();
            hasta = fechaLocal.plusDays(1).atStartOfDay();
        }

        // Listar turnos sin filtro de estado (null)
        List<Turno> turnos = turnoQueryService.listar(null, empleadoId, null, desde, hasta, null, null);

        // Filtrar solo CONFIRMADO y PENDIENTE para mostrar horarios ocupados
        List<Turno> turnosOcupados = turnos.stream()
                .filter(t -> "CONFIRMADO".equalsIgnoreCase(t.getEstado()) ||
                        "PENDIENTE".equalsIgnoreCase(t.getEstado()))
                .toList();

        return ResponseEntity.ok(turnosOcupados);
    }

    @PostMapping
    public ResponseEntity<TurnoPublicoResponse> crear(@Valid @RequestBody TurnoCreateRequest req) {
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

        currentUserService.getCurrentUserId().ifPresent(usuarioId -> {
            t.setClienteId(usuarioId);
        });

        if (t.getClienteId() == null) {
            if (req.getClienteEmail() != null && !req.getClienteEmail().isBlank()) {
                usuarioRepositoryPort.findByEmail(req.getClienteEmail().trim().toLowerCase())
                        .ifPresent(usuario -> t.setClienteId(usuario.getId()));
            }
        }

        Turno creado = crearTurnoUseCase.ejecutar(t);

        // Construir response mejorado
        TurnoPublicoResponse response = new TurnoPublicoResponse();
        response.setTurnoId(creado.getId());
        response.setEstado(creado.getEstado());
        response.setRequiresValidation(creado.isRequiereValidacion());

        // Si requiere validación, crear verificación y enviar código (Email o SMS)
        if (creado.isRequiereValidacion()
                && (creado.getClienteTelefono() != null || creado.getClienteEmail() != null)) {
            try {
                String canal = (creado.getClienteEmail() != null && !creado.getClienteEmail().isBlank()) ? "email"
                        : "sms";
                String destino = canal.equals("email") ? creado.getClienteEmail() : creado.getClienteTelefono();

                VerificacionTelefono verificacion = crearVerificacionUseCase.ejecutar(
                        creado.getClienteTelefono(),
                        creado.getClienteEmail(),
                        canal,
                        creado.getId());
                response.setVerificationId(verificacion.getId());
                response.setMessage("Turno creado. Hemos enviado un código de verificación a " +
                        destino + ". Por favor, confírmalo para completar tu reserva.");
            } catch (Exception e) {
                // Si falla el envío del SMS, el turno ya está creado pero sin verificación
                response.setVerificationId(null);
                response.setMessage("Turno creado pero hubo un error al enviar el código de verificación. " +
                        "Por favor, contacta con la empresa.");
            }
        } else {
            // No requiere validación telefónica
            response.setVerificationId(null);
            String message;
            if ("PENDIENTE".equalsIgnoreCase(creado.getEstado())) {
                message = "Turno creado y pendiente de aprobación por la empresa.";
            } else if ("CONFIRMADO".equalsIgnoreCase(creado.getEstado())) {
                message = "Turno confirmado exitosamente.";
            } else {
                message = "Turno creado con estado: " + creado.getEstado();
            }
            response.setMessage(message);
        }

        return ResponseEntity.ok(response);
    }
}
