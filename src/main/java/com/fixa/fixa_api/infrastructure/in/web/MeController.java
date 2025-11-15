package com.fixa.fixa_api.infrastructure.in.web;

import com.fixa.fixa_api.application.service.UsuarioEmpresaQueryService;
import com.fixa.fixa_api.application.service.TurnoQueryService;
import com.fixa.fixa_api.application.service.ValoracionService;
import com.fixa.fixa_api.domain.model.Empresa;
import com.fixa.fixa_api.domain.model.Turno;
import com.fixa.fixa_api.infrastructure.in.web.dto.TurnoMeResponse;
import com.fixa.fixa_api.infrastructure.in.web.error.ApiException;
import com.fixa.fixa_api.infrastructure.security.CurrentUserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
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

    public MeController(UsuarioEmpresaQueryService ueQueryService,
                        TurnoQueryService turnoQueryService,
                        CurrentUserService currentUserService,
                        ValoracionService valoracionService) {
        this.ueQueryService = ueQueryService;
        this.turnoQueryService = turnoQueryService;
        this.currentUserService = currentUserService;
        this.valoracionService = valoracionService;
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
}
