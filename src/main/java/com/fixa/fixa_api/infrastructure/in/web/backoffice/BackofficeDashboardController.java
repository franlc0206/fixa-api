package com.fixa.fixa_api.infrastructure.in.web.backoffice;

import com.fixa.fixa_api.application.service.DashboardService;
import com.fixa.fixa_api.domain.model.dashboard.DashboardMetrics;
import com.fixa.fixa_api.domain.repository.UsuarioEmpresaRepositoryPort;
import com.fixa.fixa_api.infrastructure.in.web.dto.response.DashboardMetricsResponse;
import com.fixa.fixa_api.infrastructure.in.web.error.ApiException;
import com.fixa.fixa_api.infrastructure.security.CurrentUserService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/backoffice/dashboard")
public class BackofficeDashboardController {

    private final DashboardService dashboardService;
    private final CurrentUserService currentUserService;
    private final UsuarioEmpresaRepositoryPort usuarioEmpresaPort;

    public BackofficeDashboardController(
            DashboardService dashboardService,
            CurrentUserService currentUserService,
            UsuarioEmpresaRepositoryPort usuarioEmpresaPort) {
        this.dashboardService = dashboardService;
        this.currentUserService = currentUserService;
        this.usuarioEmpresaPort = usuarioEmpresaPort;
    }

    @GetMapping("/metrics")
    public ResponseEntity<DashboardMetricsResponse> getMetrics(
            @RequestParam(required = false) Long empresaId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin) {

        Long userId = currentUserService.getCurrentUserId()
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Usuario no autenticado"));

        var usuarioEmpresas = usuarioEmpresaPort.findByUsuario(userId);

        Long empresaIdSeleccionada;

        if (empresaId != null) {
            boolean pertenece = usuarioEmpresas.stream()
                    .anyMatch(ue -> ue.isActivo() && ue.getEmpresaId().equals(empresaId));
            if (!pertenece) {
                throw new ApiException(HttpStatus.FORBIDDEN, "No tienes acceso a la empresa seleccionada");
            }
            empresaIdSeleccionada = empresaId;
        } else {
            var primeraEmpresaActiva = usuarioEmpresas.stream()
                    .filter(ue -> ue.isActivo())
                    .findFirst()
                    .orElseThrow(() -> new ApiException(HttpStatus.FORBIDDEN, "No tienes empresas activas"));
            empresaIdSeleccionada = primeraEmpresaActiva.getEmpresaId();
        }

        DashboardMetrics metrics = dashboardService.obtenerMetricas(empresaIdSeleccionada, inicio, fin);

        return ResponseEntity.ok(mapToResponse(metrics));
    }

    private DashboardMetricsResponse mapToResponse(DashboardMetrics metrics) {
        return DashboardMetricsResponse.builder()
                .totalTurnosMes(metrics.getTotalTurnosMes())
                .ingresosEstimadosMes(metrics.getIngresosEstimadosMes())
                .turnosPorEstado(metrics.getTurnosPorEstado())
                .turnosPorMesUltimoAno(metrics.getTurnosPorMesUltimoAno())
                .topEmpleados(metrics.getTopEmpleados().stream()
                        .map(e -> DashboardMetricsResponse.EmpleadoMetricaDTO.builder()
                                .empleadoId(e.getEmpleadoId())
                                .nombre(e.getNombre())
                                .apellido(e.getApellido())
                                .cantidadTurnos(e.getCantidadTurnos())
                                .build())
                        .collect(Collectors.toList()))
                .topServicios(metrics.getTopServicios().stream()
                        .map(s -> DashboardMetricsResponse.ServicioMetricaDTO.builder()
                                .servicioId(s.getServicioId())
                                .nombre(s.getNombre())
                                .cantidadTurnos(s.getCantidadTurnos())
                                .ingresosGenerados(s.getIngresosGenerados())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }
}
