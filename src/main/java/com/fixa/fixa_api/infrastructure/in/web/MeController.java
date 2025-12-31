package com.fixa.fixa_api.infrastructure.in.web;

import com.fixa.fixa_api.application.service.UsuarioEmpresaQueryService;
import com.fixa.fixa_api.application.service.TurnoQueryService;
import com.fixa.fixa_api.application.service.ValoracionService;
import com.fixa.fixa_api.application.service.AuthService;
import com.fixa.fixa_api.application.usecase.ReprogramarTurnoUseCase;
import com.fixa.fixa_api.application.usecase.CancelarTurnoUseCase;
import com.fixa.fixa_api.domain.model.Empresa;
import com.fixa.fixa_api.domain.model.Turno;
import com.fixa.fixa_api.infrastructure.in.web.dto.TurnoMeResponse;
import com.fixa.fixa_api.infrastructure.in.web.dto.ChangeEmailRequest;
import com.fixa.fixa_api.infrastructure.in.web.error.ApiException;
import com.fixa.fixa_api.infrastructure.security.CurrentUserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/me")
public class MeController {

    private final UsuarioEmpresaQueryService ueQueryService;
    private final TurnoQueryService turnoQueryService;
    private final CurrentUserService currentUserService;
    private final ValoracionService valoracionService;
    private final AuthService authService;
    private final ReprogramarTurnoUseCase reprogramarTurnoUseCase;
    private final CancelarTurnoUseCase cancelarTurnoUseCase;

    public MeController(UsuarioEmpresaQueryService ueQueryService,
            TurnoQueryService turnoQueryService,
            CurrentUserService currentUserService,
            ValoracionService valoracionService,
            AuthService authService,
            ReprogramarTurnoUseCase reprogramarTurnoUseCase,
            CancelarTurnoUseCase cancelarTurnoUseCase) {
        this.ueQueryService = ueQueryService;
        this.turnoQueryService = turnoQueryService;
        this.currentUserService = currentUserService;
        this.valoracionService = valoracionService;
        this.authService = authService;
        this.reprogramarTurnoUseCase = reprogramarTurnoUseCase;
        this.cancelarTurnoUseCase = cancelarTurnoUseCase;
    }

    @GetMapping("/empresas")
    public ResponseEntity<List<Empresa>> misEmpresas() {
        return ResponseEntity.ok(ueQueryService.empresasDelUsuarioActual());
    }

    @GetMapping("/turnos")
    public ResponseEntity<List<TurnoMeResponse>> misTurnos(
            @RequestParam(value = "estado", required = false) String estado,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size) {

        Long usuarioId = currentUserService.getCurrentUserId()
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Usuario no autenticado"));

        List<Turno> turnos = turnoQueryService.listarPorCliente(usuarioId, estado, page, size);
        Set<Long> turnosValorados = valoracionService.obtenerValoracionesPorUsuario(usuarioId).stream()
                .map(v -> v.getTurnoId())
                .collect(Collectors.toSet());

        List<TurnoMeResponse> respuesta = turnos.stream()
                .map(turno -> TurnoMeResponse.fromDomain(turno, turnosValorados.contains(turno.getId())))
                .collect(Collectors.toList());

        return ResponseEntity.ok(respuesta);
    }

    public static class ReprogramarRequest {
        @jakarta.validation.constraints.NotNull
        private java.time.LocalDateTime fechaHoraInicio;

        public java.time.LocalDateTime getFechaHoraInicio() {
            return fechaHoraInicio;
        }

        public void setFechaHoraInicio(java.time.LocalDateTime fechaHoraInicio) {
            this.fechaHoraInicio = fechaHoraInicio;
        }
    }

    @PostMapping("/turnos/{id}/reprogramar")
    public ResponseEntity<TurnoMeResponse> reprogramarTurno(
            @PathVariable("id") Long id,
            @Valid @RequestBody ReprogramarRequest req) {

        Long usuarioId = currentUserService.getCurrentUserId()
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Usuario no autenticado"));

        Turno turno = reprogramarTurnoUseCase.reprogramar(id, req.getFechaHoraInicio(), usuarioId);

        // Convert to DTO using generic logic (yaValorado false since it's just
        // modified)
        return ResponseEntity.ok(TurnoMeResponse.fromDomain(turno, false));
    }

    @PostMapping("/turnos/{id}/cancelar")
    public ResponseEntity<TurnoMeResponse> cancelarTurno(
            @PathVariable("id") Long id) { // No body required for self-cancellation

        Long usuarioId = currentUserService.getCurrentUserId()
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Usuario no autenticado"));

        // Pass a default "Cancelado por el usuario" motive or handle via overload
        Turno turno = cancelarTurnoUseCase.cancelar(id, "Cancelado por el usuario", usuarioId);

        return ResponseEntity.ok(TurnoMeResponse.fromDomain(turno, false));
    }

    @PutMapping("/email")
    public ResponseEntity<Void> cambiarMiEmail(@Valid @RequestBody ChangeEmailRequest request) {
        Long usuarioId = currentUserService.getCurrentUserId()
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Usuario no autenticado"));

        authService.cambiarEmail(usuarioId, request.getPassword(), request.getNuevoEmail());

        return ResponseEntity.noContent().build();
    }
}
