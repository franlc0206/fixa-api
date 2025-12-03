package com.fixa.fixa_api.infrastructure.in.web;

import com.fixa.fixa_api.application.service.CalendarioQueryService;
import com.fixa.fixa_api.application.service.EmpleadoService;
import com.fixa.fixa_api.application.service.EmpresaService;
import com.fixa.fixa_api.application.service.ServicioService;
import com.fixa.fixa_api.application.service.SuscripcionService;
import com.fixa.fixa_api.application.service.TurnoIntervaloCalculator;
import com.fixa.fixa_api.domain.model.Empleado;
import com.fixa.fixa_api.domain.model.Empresa;
import com.fixa.fixa_api.domain.model.Servicio;
import com.fixa.fixa_api.domain.model.Turno;
import com.fixa.fixa_api.domain.repository.UsuarioEmpresaRepositoryPort;
import com.fixa.fixa_api.infrastructure.in.web.dto.CalendarioEventoDTO;
import com.fixa.fixa_api.infrastructure.in.web.dto.EmpresaRequest;
import com.fixa.fixa_api.infrastructure.in.web.dto.PlanInfoResponse;
import com.fixa.fixa_api.infrastructure.in.web.dto.SuscripcionResponse;
import com.fixa.fixa_api.infrastructure.in.web.error.ApiException;
import com.fixa.fixa_api.infrastructure.security.CurrentUserService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/backoffice")
public class BackOfficeController {

    private final EmpresaService empresaService;
    private final CurrentUserService currentUserService;
    private final UsuarioEmpresaRepositoryPort usuarioEmpresaPort;
    private final CalendarioQueryService calendarioQueryService;
    private final ServicioService servicioService;
    private final EmpleadoService empleadoService;
    private final SuscripcionService suscripcionService;
    private final TurnoIntervaloCalculator turnoIntervaloCalculator;

    public BackOfficeController(
            EmpresaService empresaService,
            CurrentUserService currentUserService,
            UsuarioEmpresaRepositoryPort usuarioEmpresaPort,
            CalendarioQueryService calendarioQueryService,
            ServicioService servicioService,
            EmpleadoService empleadoService,
            SuscripcionService suscripcionService,
            TurnoIntervaloCalculator turnoIntervaloCalculator) {
        this.empresaService = empresaService;
        this.currentUserService = currentUserService;
        this.usuarioEmpresaPort = usuarioEmpresaPort;
        this.calendarioQueryService = calendarioQueryService;
        this.servicioService = servicioService;
        this.empleadoService = empleadoService;
        this.suscripcionService = suscripcionService;
        this.turnoIntervaloCalculator = turnoIntervaloCalculator;
    }

    /**
     * Obtiene la empresa activa del usuario autenticado.
     * Este endpoint ya está protegido por BackofficeAccessFilter.
     */
    @GetMapping("/empresa")
    public ResponseEntity<Empresa> obtenerEmpresaActiva() {
        Long userId = currentUserService.getCurrentUserId()
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Usuario no autenticado"));

        var usuarioEmpresas = usuarioEmpresaPort.findByUsuario(userId);

        var primeraEmpresaActiva = usuarioEmpresas.stream()
                .filter(ue -> ue.isActivo())
                .findFirst()
                .orElseThrow(() -> new ApiException(HttpStatus.FORBIDDEN, "No tienes empresas activas"));

        Empresa empresa = empresaService.obtener(primeraEmpresaActiva.getEmpresaId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Empresa no encontrada"));

        return ResponseEntity.ok(empresa);
    }

    @PutMapping("/empresa")
    public ResponseEntity<Empresa> actualizarEmpresaActiva(@RequestBody EmpresaRequest req) {
        Long userId = currentUserService.getCurrentUserId()
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Usuario no autenticado"));

        var usuarioEmpresas = usuarioEmpresaPort.findByUsuario(userId);

        var primeraEmpresaActiva = usuarioEmpresas.stream()
                .filter(ue -> ue.isActivo())
                .findFirst()
                .orElseThrow(() -> new ApiException(HttpStatus.FORBIDDEN, "No tienes empresas activas"));

        Long empresaId = primeraEmpresaActiva.getEmpresaId();

        empresaService.obtener(empresaId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Empresa no encontrada"));

        Empresa d = new Empresa();
        d.setId(empresaId);
        d.setNombre(req.getNombre());
        d.setDescripcion(req.getDescripcion());
        d.setDireccion(req.getDireccion());
        d.setTelefono(req.getTelefono());
        d.setEmail(req.getEmail());
        d.setBannerUrl(req.getBannerUrl());
        d.setPermiteReservasSinUsuario(req.isPermiteReservasSinUsuario());
        d.setRequiereValidacionTelefono(req.isRequiereValidacionTelefono());
        d.setRequiereAprobacionTurno(req.isRequiereAprobacionTurno());
        d.setMensajeValidacionPersonalizado(req.getMensajeValidacionPersonalizado());
        d.setVisibilidadPublica(req.isVisibilidadPublica());
        d.setActivo(req.isActivo());
        d.setCategoriaId(req.getCategoriaId());

        Empresa saved = empresaService.guardar(d);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/plan-info")
    public ResponseEntity<PlanInfoResponse> obtenerInfoPlan(@RequestParam(required = false) Long empresaId) {
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

        var plan = suscripcionService.obtenerPlanActual(empresaIdSeleccionada);

        // Calcular uso actual
        long empleadosUsados = empleadoService.listarPorEmpresa(empresaIdSeleccionada).stream()
                .filter(Empleado::isActivo)
                .count();
        long serviciosUsados = servicioService.listarPorEmpresa(empresaIdSeleccionada).stream()
                .filter(Servicio::isActivo)
                .count();

        // TODO: Implementar conteo real de turnos mensuales. Por ahora mockeamos o
        // devolvemos 0.
        long turnosUsados = 0;

        PlanInfoResponse response = new PlanInfoResponse();
        response.setPlanNombre(plan.getNombre());

        PlanInfoResponse.LimitesInfo limites = new PlanInfoResponse.LimitesInfo();
        limites.setEmpleados(new PlanInfoResponse.DetalleLimite(empleadosUsados, plan.getMaxEmpleados()));
        limites.setServicios(new PlanInfoResponse.DetalleLimite(serviciosUsados, plan.getMaxServicios()));
        limites.setTurnosMensuales(new PlanInfoResponse.DetalleLimite(turnosUsados, plan.getMaxTurnosMensuales()));

        response.setLimites(limites);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/suscripcion")
    public ResponseEntity<SuscripcionResponse> obtenerSuscripcion() {
        Long userId = currentUserService.getCurrentUserId()
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Usuario no autenticado"));

        var usuarioEmpresas = usuarioEmpresaPort.findByUsuario(userId);
        var primeraEmpresaActiva = usuarioEmpresas.stream()
                .filter(ue -> ue.isActivo())
                .findFirst()
                .orElseThrow(() -> new ApiException(HttpStatus.FORBIDDEN, "No tienes empresas activas"));

        Long empresaId = primeraEmpresaActiva.getEmpresaId();

        return suscripcionService.obtenerSuscripcionActiva(empresaId)
                .map(s -> {
                    SuscripcionResponse resp = new SuscripcionResponse();
                    resp.setId(s.getId());
                    resp.setEmpresaId(s.getEmpresaId());
                    resp.setPlanId(s.getPlanId());
                    resp.setPrecioPactado(s.getPrecioPactado());
                    resp.setFechaInicio(s.getFechaInicio());
                    resp.setFechaFin(s.getFechaFin());
                    resp.setActivo(s.isActivo());
                    return ResponseEntity.ok(resp);
                })
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "No se encontró una suscripción activa"));
    }

    /**
     * GET /api/backoffice/calendario
     * Obtiene los turnos de la empresa en formato compatible con FullCalendar.
     * 
     * Query params:
     * - desde: Fecha/hora inicio (ISO 8601, opcional, default: inicio del mes
     * actual)
     * - hasta: Fecha/hora fin (ISO 8601, opcional, default: fin del mes actual)
     * - empleadoId: Filtrar por empleado (opcional)
     * - estados: Lista de estados a incluir separados por coma (opcional, ej:
     * "CONFIRMADO,PENDIENTE")
     * 
     * @return Lista de eventos en formato FullCalendar
     */
    @GetMapping("/calendario")
    public ResponseEntity<List<CalendarioEventoDTO>> obtenerCalendario(
            @RequestParam(required = false) Long empresaId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta,
            @RequestParam(required = false) Long empleadoId,
            @RequestParam(required = false) String estados) {

        System.out.println("========================================");
        System.out.println(" [CALENDARIO CONTROLLER] Método obtenerCalendario iniciado");
        System.out.println(" [CALENDARIO CONTROLLER] Params:");
        System.out.println("  - empresaId: " + empresaId);
        System.out.println("  - desde: " + desde);
        System.out.println("  - hasta: " + hasta);
        System.out.println("  - empleadoId: " + empleadoId);
        System.out.println("  - estados: " + estados);

        // Obtener empresa del usuario autenticado
        System.out.println(" [CALENDARIO CONTROLLER] Intentando obtener usuario actual...");
        Long userId = currentUserService.getCurrentUserId()
                .orElseThrow(() -> {
                    System.out.println(" [CALENDARIO CONTROLLER] NO SE PUDO OBTENER USUARIO - Lanzando excepción");
                    return new ApiException(HttpStatus.UNAUTHORIZED, "Usuario no autenticado");
                });

        System.out.println(" [CALENDARIO CONTROLLER] Usuario obtenido: " + userId);

        var usuarioEmpresas = usuarioEmpresaPort.findByUsuario(userId);
        System.out.println(" [CALENDARIO CONTROLLER] Empresas del usuario: " + usuarioEmpresas.size());

        Long empresaIdSeleccionada;

        if (empresaId != null) {
            // Validar que la empresa recibida pertenece al usuario y está activa
            boolean pertenece = usuarioEmpresas.stream()
                    .anyMatch(ue -> ue.isActivo() && ue.getEmpresaId().equals(empresaId));
            if (!pertenece) {
                System.out.println(" [CALENDARIO CONTROLLER] Empresa enviada no pertenece al usuario o no está activa: "
                        + empresaId);
                throw new ApiException(HttpStatus.FORBIDDEN, "No tienes acceso a la empresa seleccionada");
            }
            empresaIdSeleccionada = empresaId;
        } else {
            // Fallback: primera empresa activa
            var primeraEmpresaActiva = usuarioEmpresas.stream()
                    .filter(ue -> ue.isActivo())
                    .findFirst()
                    .orElseThrow(() -> {
                        System.out.println(" [CALENDARIO CONTROLLER] Usuario sin empresa activa");
                        return new ApiException(HttpStatus.FORBIDDEN, "No tienes empresas activas");
                    });
            empresaIdSeleccionada = primeraEmpresaActiva.getEmpresaId();
        }

        System.out.println(" [CALENDARIO CONTROLLER] Empresa ID seleccionada: " + empresaIdSeleccionada);

        // Parsear estados si viene como string separado por comas
        List<String> listaEstados = null;
        if (estados != null && !estados.isBlank()) {
            listaEstados = List.of(estados.split(","));
            System.out.println(" [CALENDARIO CONTROLLER] Estados parseados: " + listaEstados);
        }

        // Obtener turnos del calendario
        System.out.println(" [CALENDARIO CONTROLLER] Consultando turnos...");
        List<Turno> turnos = calendarioQueryService.obtenerTurnosParaCalendario(
                empresaIdSeleccionada, desde, hasta, empleadoId, listaEstados);

        System.out.println(" [CALENDARIO CONTROLLER] Turnos obtenidos: " + turnos.size());

        // Mapear turnos a DTOs en formato FullCalendar (expandiendo intervalos)
        List<CalendarioEventoDTO> eventos = turnos.stream()
                .flatMap(turno -> expandirTurnoAEventos(turno).stream())
                .collect(Collectors.toList());

        System.out.println(" [CALENDARIO CONTROLLER] Eventos mapeados: " + eventos.size());
        System.out.println(" [CALENDARIO CONTROLLER] Retornando respuesta 200 OK");
        System.out.println("========================================");

        return ResponseEntity.ok(eventos);
    }

    /**
     * Expande un turno en múltiples eventos si tiene intervalos de trabajo
     * discontinuos.
     */
    private List<CalendarioEventoDTO> expandirTurnoAEventos(Turno turno) {
        Servicio servicio = null;
        if (turno.getServicioId() != null) {
            servicio = servicioService.obtener(turno.getServicioId()).orElse(null);
        }

        // Si no hay servicio o no tiene etapas complejas, retornar evento simple
        if (servicio == null || servicio.getEtapas().size() <= 1) {
            return List.of(mapearTurnoAEvento(turno, null, null, null));
        }

        // Desglosar todas las etapas (TRABAJO y ESPERA)
        var etapas = turnoIntervaloCalculator.desglosarEtapas(turno, servicio);

        List<CalendarioEventoDTO> eventos = new java.util.ArrayList<>();
        int index = 1;
        for (var etapa : etapas) {
            // Crear evento para este sub-bloque
            CalendarioEventoDTO evento = mapearTurnoAEvento(turno, etapa.inicio(), etapa.fin(), etapa.tipo());
            evento.setId(turno.getId() * 10000 + index);

            eventos.add(evento);
            index++;
        }
        return eventos;
    }

    /**
     * Mapea un Turno a CalendarioEventoDTO.
     * Si start/end son null, usa los del turno.
     */
    private CalendarioEventoDTO mapearTurnoAEvento(Turno turno, LocalDateTime startOverride, LocalDateTime endOverride,
            com.fixa.fixa_api.domain.model.ServicioEtapa.TipoEtapa tipoEtapa) {
        CalendarioEventoDTO evento = new CalendarioEventoDTO();

        // Campos básicos
        evento.setId(turno.getId());

        LocalDateTime start = startOverride != null ? startOverride : turno.getFechaHoraInicio();
        LocalDateTime end = endOverride != null ? endOverride : turno.getFechaHoraFin();

        evento.setStart(start.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        evento.setEnd(end.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        evento.setEstado(turno.getEstado());
        evento.setAllDay(false);

        // Nombre del cliente y título
        String nombreCliente = (turno.getClienteNombre() != null ? turno.getClienteNombre() : "") +
                (turno.getClienteApellido() != null ? " " + turno.getClienteApellido() : "");
        evento.setClienteNombre(nombreCliente.trim());
        evento.setClienteTelefono(turno.getClienteTelefono());

        // Obtener nombre del servicio
        String nombreServicio = "Servicio";
        if (turno.getServicioId() != null) {
            nombreServicio = servicioService.obtener(turno.getServicioId())
                    .map(Servicio::getNombre)
                    .orElse("Servicio");
        }
        evento.setServicioNombre(nombreServicio);
        evento.setServicioId(turno.getServicioId());

        // Obtener nombre del empleado
        String nombreEmpleado = "Empleado";
        if (turno.getEmpleadoId() != null) {
            Empleado empleado = empleadoService.obtener(turno.getEmpleadoId()).orElse(null);
            if (empleado != null) {
                nombreEmpleado = empleado.getNombre() + " " + empleado.getApellido();
            }
        }
        evento.setEmpleadoNombre(nombreEmpleado);
        evento.setEmpleadoId(turno.getEmpleadoId());

        // Título del evento para FullCalendar
        if (tipoEtapa == com.fixa.fixa_api.domain.model.ServicioEtapa.TipoEtapa.ESPERA) {
            evento.setTitle("(Espera) " + nombreServicio);
            // Estilo visual para ESPERA (gris claro, borde punteado si fuera posible, etc)
            evento.setBackgroundColor("#e9ecef"); // Gris muy claro
            evento.setBorderColor("#adb5bd");
            evento.setTextColor("#495057"); // Texto oscuro
        } else {
            evento.setTitle(nombreCliente + " - " + nombreServicio);
            // Colores según estado del turno
            aplicarColoresPorEstado(evento, turno.getEstado());
        }

        // Campos adicionales
        evento.setObservaciones(turno.getObservaciones());
        evento.setRequiereValidacion(turno.isRequiereValidacion());
        evento.setTelefonoValidado(turno.isTelefonoValidado());

        return evento;
    }

    private void aplicarColoresPorEstado(CalendarioEventoDTO evento, String estado) {
        switch (estado.toUpperCase()) {
            case "CONFIRMADO":
                evento.setBackgroundColor("#28a745"); // Verde
                evento.setBorderColor("#28a745");
                evento.setTextColor("#ffffff");
                break;
            case "PENDIENTE":
                evento.setBackgroundColor("#ffc107"); // Amarillo
                evento.setBorderColor("#ffc107");
                evento.setTextColor("#000000");
                break;
            case "CANCELADO":
                evento.setBackgroundColor("#dc3545"); // Rojo
                evento.setBorderColor("#dc3545");
                evento.setTextColor("#ffffff");
                break;
            case "COMPLETADO":
            case "REALIZADO":
                evento.setBackgroundColor("#6c757d"); // Gris
                evento.setBorderColor("#6c757d");
                evento.setTextColor("#ffffff");
                break;
            default:
                evento.setBackgroundColor("#007bff"); // Azul
                evento.setBorderColor("#007bff");
                evento.setTextColor("#ffffff");
        }
    }
}
