package com.fixa.fixa_api.infrastructure.in.web;

import com.fixa.fixa_api.application.service.DisponibilidadService;
import com.fixa.fixa_api.application.service.EmpresaService;
import com.fixa.fixa_api.application.service.EmpleadoService;
import com.fixa.fixa_api.application.service.ServicioService;
import com.fixa.fixa_api.application.service.ValoracionService;
import com.fixa.fixa_api.application.service.RankingEmpresaService;
import com.fixa.fixa_api.domain.model.Disponibilidad;
import com.fixa.fixa_api.domain.model.Empresa;
import com.fixa.fixa_api.domain.model.Empleado;
import com.fixa.fixa_api.domain.model.Servicio;
import com.fixa.fixa_api.domain.model.Valoracion;
import com.fixa.fixa_api.domain.model.ValoracionResumen;
import com.fixa.fixa_api.infrastructure.in.web.dto.EmpresaDestacadaResponse;
import com.fixa.fixa_api.infrastructure.in.web.dto.ValoracionPublicaResponse;
import com.fixa.fixa_api.infrastructure.in.web.dto.ValoracionResumenResponse;
import com.fixa.fixa_api.application.service.DisponibilidadSlotService;
import com.fixa.fixa_api.infrastructure.in.web.dto.DisponibilidadGlobalSlot;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/public/empresas")
public class PublicEmpresaController {

    private final EmpresaService empresaService;
    private final ServicioService servicioService;
    private final EmpleadoService empleadoService;
    private final DisponibilidadService disponibilidadService;
    private final ValoracionService valoracionService;
    private final RankingEmpresaService rankingEmpresaService;
    private final DisponibilidadSlotService disponibilidadSlotService;

    public PublicEmpresaController(EmpresaService empresaService,
            ServicioService servicioService,
            EmpleadoService empleadoService,
            DisponibilidadService disponibilidadService,
            ValoracionService valoracionService,
            RankingEmpresaService rankingEmpresaService,
            DisponibilidadSlotService disponibilidadSlotService) {
        this.empresaService = empresaService;
        this.servicioService = servicioService;
        this.empleadoService = empleadoService;
        this.disponibilidadService = disponibilidadService;
        this.valoracionService = valoracionService;
        this.rankingEmpresaService = rankingEmpresaService;
        this.disponibilidadSlotService = disponibilidadSlotService;
    }

    // Catálogo público: solo empresas visibles
    @GetMapping
    public ResponseEntity<List<Empresa>> listarEmpresasVisibles(
            @RequestParam(value = "categoriaId", required = false) Long categoriaId,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size) {
        List<Empresa> result = empresaService.listarPublicasPorCategoriaServicioPaginado(categoriaId, page, size);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/destacadas")
    public ResponseEntity<List<EmpresaDestacadaResponse>> listarEmpresasDestacadas(
            @RequestParam(value = "categoriaId", required = false) Long categoriaId,
            @RequestParam(value = "limit", required = false) Integer limit) {
        List<RankingEmpresaService.EmpresaDestacada> ranking = rankingEmpresaService
                .listarEmpresasDestacadas(categoriaId, limit);

        List<EmpresaDestacadaResponse> response = ranking.stream()
                .map(EmpresaDestacadaResponse::fromDomain)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    // Servicios públicos por empresa (por defecto solo activos)
    @GetMapping("/{empresaId}/servicios")
    public ResponseEntity<List<Servicio>> listarServiciosPublicos(@PathVariable Long empresaId,
            @RequestParam(value = "soloActivos", required = false, defaultValue = "true") boolean soloActivos,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size) {
        // Usar métodos públicos que NO requieren autenticación
        List<Servicio> result = (page != null && size != null)
                ? servicioService.listarPorEmpresaPaginadoPublico(empresaId, soloActivos, page, size)
                : servicioService.listarPorEmpresaPublico(empresaId, soloActivos);
        return ResponseEntity.ok(result);
    }

    // Empleados públicos por empresa ID (para reserva anónima)
    @GetMapping("/{empresaId}/empleados")
    public ResponseEntity<List<Empleado>> listarEmpleadosPublicosPorId(@PathVariable Long empresaId) {
        List<Empleado> empleados = empleadoService.listarPublicosPorEmpresa(empresaId);
        return ResponseEntity.ok(empleados);
    }

    // Disponibilidad pública de empleado (horarios de trabajo)
    @GetMapping("/{empresaId}/empleados/{empleadoId}/disponibilidad")
    public ResponseEntity<List<Disponibilidad>> obtenerDisponibilidadPublica(
            @PathVariable Long empresaId,
            @PathVariable Long empleadoId) {
        List<Disponibilidad> disponibilidad = disponibilidadService.listarPorEmpleado(empleadoId);
        return ResponseEntity.ok(disponibilidad);
    }

    // Disponibilidad pública de empleado (alias sin empresaId)
    @GetMapping("/empleados/{empleadoId}/disponibilidad")
    public ResponseEntity<List<Disponibilidad>> obtenerDisponibilidadPublicaCorta(
            @PathVariable Long empleadoId) {
        List<Disponibilidad> disponibilidad = disponibilidadService.listarPorEmpleado(empleadoId);
        return ResponseEntity.ok(disponibilidad);
    }

    // Detalle público de empresa por ID (para banner/SEO)
    @GetMapping("/{empresaId}")
    public ResponseEntity<Empresa> obtenerPublico(@PathVariable Long empresaId) {
        return empresaService.obtener(empresaId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Detalle público de empresa por slug (para URLs amigables)
    @GetMapping("/slug/{slug}")
    public ResponseEntity<Empresa> obtenerPorSlug(@PathVariable String slug) {
        return empresaService.obtenerPorSlug(slug)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Empleados públicos de empresa por slug
    @GetMapping("/slug/{slug}/empleados")
    public ResponseEntity<List<Empleado>> listarEmpleadosPublicos(@PathVariable String slug) {
        return empresaService.obtenerPorSlug(slug)
                .map(empresa -> {
                    List<Empleado> empleados = empleadoService.listarPublicosPorEmpresa(empresa.getId());
                    return ResponseEntity.ok(empleados);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{empresaId}/valoraciones/resumen")
    public ResponseEntity<ValoracionResumenResponse> obtenerResumenValoraciones(@PathVariable Long empresaId) {
        return empresaService.obtener(empresaId)
                .map(empresa -> {
                    ValoracionResumen resumen = valoracionService.obtenerResumenValoracionesPorEmpresa(empresaId);
                    return ResponseEntity.ok(ValoracionResumenResponse.fromDomain(resumen));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{empresaId}/valoraciones")
    public ResponseEntity<List<ValoracionPublicaResponse>> listarValoracionesPublicas(@PathVariable Long empresaId,
            @RequestParam(value = "soloConResena", defaultValue = "false") boolean soloConResena,
            @RequestParam(value = "limit", required = false) Integer limit) {
        return empresaService.obtener(empresaId)
                .map(empresa -> {
                    List<Valoracion> valoraciones = valoracionService.obtenerComentariosPublicos(empresaId,
                            soloConResena, limit);
                    List<ValoracionPublicaResponse> respuesta = valoraciones.stream()
                            .map(ValoracionPublicaResponse::fromDomain)
                            .collect(Collectors.toList());
                    return ResponseEntity.ok(respuesta);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/slug/{slug}/valoraciones/resumen")
    public ResponseEntity<ValoracionResumenResponse> obtenerResumenValoracionesPorSlug(@PathVariable String slug) {
        return empresaService.obtenerPorSlug(slug)
                .map(empresa -> {
                    ValoracionResumen resumen = valoracionService.obtenerResumenValoracionesPorEmpresa(empresa.getId());
                    return ResponseEntity.ok(ValoracionResumenResponse.fromDomain(resumen));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/slug/{slug}/valoraciones")
    public ResponseEntity<List<ValoracionPublicaResponse>> listarValoracionesPublicasPorSlug(@PathVariable String slug,
            @RequestParam(value = "soloConResena", defaultValue = "false") boolean soloConResena,
            @RequestParam(value = "limit", required = false) Integer limit) {
        return empresaService.obtenerPorSlug(slug)
                .map(empresa -> {
                    List<Valoracion> valoraciones = valoracionService.obtenerComentariosPublicos(empresa.getId(),
                            soloConResena, limit);
                    List<ValoracionPublicaResponse> respuesta = valoraciones.stream()
                            .map(ValoracionPublicaResponse::fromDomain)
                            .collect(Collectors.toList());
                    return ResponseEntity.ok(respuesta);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Disponibilidad global agregada (sin preferencia de empleado)
    @GetMapping("/{empresaId}/servicios/{servicioId}/disponibilidad-global")
    public ResponseEntity<List<DisponibilidadGlobalSlot>> obtenerDisponibilidadGlobal(
            @PathVariable Long empresaId,
            @PathVariable Long servicioId) {
        List<DisponibilidadGlobalSlot> slots = disponibilidadSlotService.generarSlotsGlobales(empresaId, servicioId);
        return ResponseEntity.ok(slots);
    }
}
