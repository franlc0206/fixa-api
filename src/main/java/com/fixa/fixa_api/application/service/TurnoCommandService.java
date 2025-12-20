package com.fixa.fixa_api.application.service;

import com.fixa.fixa_api.application.usecase.AprobarTurnoUseCase;
import com.fixa.fixa_api.application.usecase.CrearTurnoUseCase;
import com.fixa.fixa_api.domain.model.Turno;
import com.fixa.fixa_api.domain.repository.TurnoRepositoryPort;
import com.fixa.fixa_api.domain.repository.EmpresaRepositoryPort;
import com.fixa.fixa_api.domain.repository.ServicioRepositoryPort;
import com.fixa.fixa_api.domain.repository.EmpleadoRepositoryPort;
import com.fixa.fixa_api.domain.repository.ConfigReglaQueryPort;
import com.fixa.fixa_api.domain.service.NotificationServicePort;
import com.fixa.fixa_api.infrastructure.in.web.error.ApiException;
import com.fixa.fixa_api.infrastructure.security.CurrentUserService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TurnoCommandService
        implements CrearTurnoUseCase, AprobarTurnoUseCase, com.fixa.fixa_api.application.usecase.CancelarTurnoUseCase,
        com.fixa.fixa_api.application.usecase.CompletarTurnoUseCase {

    private final TurnoRepositoryPort turnoPort;
    private final ServicioRepositoryPort servicioPort;
    private final EmpleadoRepositoryPort empleadoPort;
    private final EmpresaRepositoryPort empresaPort;
    private final ConfigReglaQueryPort configReglaPort;
    private final TurnoIntervaloCalculator turnoIntervaloCalculator;
    private final NotificationServicePort notificationPort;
    private final CurrentUserService currentUserService;

    public TurnoCommandService(
            TurnoRepositoryPort turnoPort,
            ServicioRepositoryPort servicioPort,
            EmpleadoRepositoryPort empleadoPort,
            EmpresaRepositoryPort empresaPort,
            ConfigReglaQueryPort configReglaPort,
            TurnoIntervaloCalculator turnoIntervaloCalculator,
            NotificationServicePort notificationPort,
            CurrentUserService currentUserService) {
        this.turnoPort = turnoPort;
        this.servicioPort = servicioPort;
        this.empleadoPort = empleadoPort;
        this.empresaPort = empresaPort;
        this.configReglaPort = configReglaPort;
        this.turnoIntervaloCalculator = turnoIntervaloCalculator;
        this.notificationPort = notificationPort;
        this.currentUserService = currentUserService;
    }

    @Override
    @Transactional
    public Turno ejecutar(Turno turno) {
        // BLOQUEO TRANSACCIONAL:
        // @Transactional provee aislamiento REPEATABLE_READ (MySQL default) que
        // previene lecturas no repetibles.
        // Para mayor seguridad ante alta concurrencia, se podría agregar
        // @Lock(LockModeType.PESSIMISTIC_WRITE)
        // en el repositorio JPA al consultar turnos existentes, bloqueando las filas
        // hasta fin de transacción.
        // En MVP actual, el bloqueo optimista + validación de solapamiento es
        // suficiente.

        // Cargar entidades requeridas
        var servicio = servicioPort.findById(turno.getServicioId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Servicio no encontrado"));
        var empleado = empleadoPort.findById(turno.getEmpleadoId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Empleado no encontrado"));
        var empresa = empresaPort.findById(turno.getEmpresaId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Empresa no encontrada"));

        // Calcular fin según duración del servicio
        if (turno.getFechaHoraInicio() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "fechaHoraInicio es requerido");
        }
        Integer durMin = servicio.getDuracionMinutos();
        if (durMin == null || durMin <= 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Servicio con duracion_minutos inválida");
        }
        LocalDateTime inicio = turno.getFechaHoraInicio();
        LocalDateTime fin = inicio.plusMinutes(durMin);
        turno.setFechaHoraFin(fin);

        // Regla: anticipación mínima en minutos
        var minAntMinOpt = configReglaPort.getInt(empresa.getId(), "min_anticipacion_minutos");
        if (minAntMinOpt.isPresent()) {
            LocalDateTime minInicio = LocalDateTime.now().plusMinutes(minAntMinOpt.get());
            if (inicio.isBefore(minInicio)) {
                throw new ApiException(HttpStatus.CONFLICT,
                        "El turno debe tomarse con al menos " + minAntMinOpt.get() + " minutos de anticipación");
            }
        }

        // Regla: anticipación máxima en días
        var maxAntDiasOpt = configReglaPort.getInt(empresa.getId(), "max_anticipacion_dias");
        if (maxAntDiasOpt.isPresent()) {
            LocalDateTime maxInicio = LocalDateTime.now().plusDays(maxAntDiasOpt.get());
            if (inicio.isAfter(maxInicio)) {
                throw new ApiException(HttpStatus.CONFLICT,
                        "El turno no puede reservarse con más de " + maxAntDiasOpt.get() + " días de anticipación");
            }
        }

        // Regla: máximo turnos por día por empleado (configurable)
        var maxTurnosDiaOpt = configReglaPort.getInt(empresa.getId(), "max_turnos_por_dia");
        if (maxTurnosDiaOpt.isPresent()) {
            LocalDateTime diaInicio = inicio.toLocalDate().atStartOfDay();
            LocalDateTime diaFin = diaInicio.plusDays(1); // rango [diaInicio, diaFin)
            var delDia = turnoPort.findByEmpleadoIdAndRango(empleado.getId(), diaInicio, diaFin);
            if (delDia.size() >= maxTurnosDiaOpt.get()) {
                throw new ApiException(HttpStatus.CONFLICT, "Se superó el máximo de turnos por día para el empleado");
            }
        }

        // Regla: máximo turnos por semana por empleado (configurable, semana
        // Lunes-Domingo)
        var maxTurnosSemanaOpt = configReglaPort.getInt(empresa.getId(), "max_turnos_por_semana");
        if (maxTurnosSemanaOpt.isPresent()) {
            var fecha = inicio.toLocalDate();
            var semanaInicio = fecha.with(java.time.DayOfWeek.MONDAY).atStartOfDay();
            var semanaFin = semanaInicio.plusDays(7);
            var deLaSemana = turnoPort.findByEmpleadoIdAndRango(empleado.getId(), semanaInicio, semanaFin);
            if (deLaSemana.size() >= maxTurnosSemanaOpt.get()) {
                throw new ApiException(HttpStatus.CONFLICT,
                        "Se superó el máximo de turnos por semana para el empleado");
            }
        }

        // Validar solapamiento con intervalos: obtener turnos del día y filtrar
        LocalDateTime ventanaInicio = inicio.minusHours(12);
        var existentes = turnoPort.findByEmpleadoIdAndRango(empleado.getId(), ventanaInicio, fin);

        // Filtrar solo turnos que ocupan espacio (no cancelados/completados si se desea
        // liberar, pero completado suele ocupar)
        // Asumimos CONFIRMADO, PENDIENTE, PENDIENTE_APROBACION ocupan.
        List<Turno> conflictosPotenciales = existentes.stream()
                .filter(t -> "CONFIRMADO".equalsIgnoreCase(t.getEstado()) ||
                        "PENDIENTE".equalsIgnoreCase(t.getEstado()) ||
                        "PENDIENTE_APROBACION".equalsIgnoreCase(t.getEstado()))
                .toList();

        boolean solapa = turnoIntervaloCalculator.haySolapamiento(
                turno,
                servicio,
                conflictosPotenciales,
                (id) -> servicioPort.findById(id).orElse(null));

        if (solapa) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "Existe solapamiento de turnos para el empleado (intervalos ocupados)");
        }

        // Setear estado inicial según reglas (empresa)
        turno.setEstado(empresa.isRequiereAprobacionTurno() ? "PENDIENTE" : "CONFIRMADO");
        turno.setRequiereValidacion(empresa.isRequiereValidacionTelefono());

        // Persistir via puerto
        Turno guardado = turnoPort.save(turno);

        // Notificar creación al cliente
        try {
            Map<String, String> vars = new HashMap<>();
            vars.put("nombre", guardado.getClienteNombre());
            vars.put("servicio", servicio.getNombre());
            vars.put("fecha", guardado.getFechaHoraInicio().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            vars.put("empresa", empresa.getNombre());

            String template = "Hola {{nombre}}, tu turno para {{servicio}} en {{empresa}} el día {{fecha}} ha sido registrado. Estado: "
                    + guardado.getEstado();
            notificationPort.sendEmail(guardado.getClienteEmail(), template, vars);
        } catch (Exception e) {
            // Loguear error pero no fallar la transacción
        }

        return guardado;
    }

    @Override
    @Transactional
    public Turno aprobar(Long turnoId) {
        Turno t = turnoPort.findById(turnoId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Turno no encontrado"));
        if ("CONFIRMADO".equalsIgnoreCase(t.getEstado())) {
            return t;
        }
        if (!"PENDIENTE".equalsIgnoreCase(t.getEstado())) {
            throw new ApiException(HttpStatus.CONFLICT, "Solo se puede aprobar un turno en estado PENDIENTE");
        }
        t.setEstado("CONFIRMADO");
        Turno guardado = turnoPort.save(t);

        // Notificar aprobación al cliente
        try {
            var servicio = servicioPort.findById(guardado.getServicioId()).orElse(null);
            var empresa = empresaPort.findById(guardado.getEmpresaId()).orElse(null);

            Map<String, String> vars = new HashMap<>();
            vars.put("nombre", guardado.getClienteNombre());
            vars.put("servicio", servicio != null ? servicio.getNombre() : "Servicio");
            vars.put("fecha", guardado.getFechaHoraInicio().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            vars.put("empresa", empresa != null ? empresa.getNombre() : "la empresa");

            String template = "¡Buenas noticias {{nombre}}! Tu turno para {{servicio}} en {{empresa}} el día {{fecha}} ha sido CONFIRMADO.";
            notificationPort.sendEmail(guardado.getClienteEmail(), template, vars);
        } catch (Exception e) {
            // Loguear error
        }

        return guardado;
    }

    @Override
    @Transactional
    public Turno cancelar(Long turnoId, String motivo) {
        Turno t = turnoPort.findById(turnoId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Turno no encontrado"));
        if ("CANCELADO".equalsIgnoreCase(t.getEstado())) {
            return t;
        }
        if (!"PENDIENTE".equalsIgnoreCase(t.getEstado()) && !"CONFIRMADO".equalsIgnoreCase(t.getEstado())) {
            throw new ApiException(HttpStatus.CONFLICT, "Solo se puede cancelar un turno PENDIENTE o CONFIRMADO");
        }
        t.setEstado("CANCELADO");
        if (motivo != null && !motivo.isBlank()) {
            String obs = t.getObservaciones() == null ? "" : (t.getObservaciones() + "\n");
            t.setObservaciones(obs + "Cancelación: " + motivo);
        }
        Turno guardado = turnoPort.save(t);

        // Notificar cancelación
        try {
            var currentUser = currentUserService.getCurrentUser().orElse(null);
            var servicio = servicioPort.findById(guardado.getServicioId()).orElse(null);
            var empresa = empresaPort.findById(guardado.getEmpresaId()).orElse(null);

            Map<String, String> vars = new HashMap<>();
            vars.put("nombre", guardado.getClienteNombre());
            vars.put("servicio", servicio != null ? servicio.getNombre() : "Servicio");
            vars.put("fecha", guardado.getFechaHoraInicio().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            vars.put("empresa", empresa != null ? empresa.getNombre() : "la empresa");
            vars.put("motivo", motivo != null ? motivo : "No especificado");

            if (currentUser != null && "CLIENTE".equalsIgnoreCase(currentUser.getRol())) {
                // Notificar a la empresa
                String template = "El turno de {{nombre}} para {{servicio}} el día {{fecha}} ha sido CANCELADO por el cliente. Motivo: {{motivo}}";
                notificationPort.sendEmail(empresa != null ? empresa.getEmail() : null, template, vars);
            } else {
                // Notificar al cliente (cancelado por empresa o empleado)
                String template = "Hola {{nombre}}, lamentamos informarte que tu turno para {{servicio}} en {{empresa}} el día {{fecha}} ha sido CANCELADO. Motivo: {{motivo}}";
                notificationPort.sendEmail(guardado.getClienteEmail(), template, vars);
            }
        } catch (Exception e) {
            // Loguear error
        }

        return guardado;
    }

    @Override
    @Transactional
    public Turno completar(Long turnoId) {
        Turno t = turnoPort.findById(turnoId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Turno no encontrado"));
        if ("COMPLETADO".equalsIgnoreCase(t.getEstado())) {
            return t;
        }
        if (!"CONFIRMADO".equalsIgnoreCase(t.getEstado())) {
            throw new ApiException(HttpStatus.CONFLICT, "Solo se puede completar un turno CONFIRMADO");
        }
        t.setEstado("COMPLETADO");
        return turnoPort.save(t);
    }
}
