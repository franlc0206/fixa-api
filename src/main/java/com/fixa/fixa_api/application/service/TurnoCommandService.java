package com.fixa.fixa_api.application.service;

import com.fixa.fixa_api.application.usecase.AprobarTurnoUseCase;
import com.fixa.fixa_api.application.usecase.CrearTurnoUseCase;
import com.fixa.fixa_api.domain.model.Turno;
import com.fixa.fixa_api.domain.repository.TurnoRepositoryPort;
import com.fixa.fixa_api.domain.repository.EmpresaRepositoryPort;
import com.fixa.fixa_api.domain.repository.ServicioRepositoryPort;
import com.fixa.fixa_api.domain.repository.EmpleadoRepositoryPort;
import com.fixa.fixa_api.domain.repository.ConfigReglaQueryPort;
import com.fixa.fixa_api.infrastructure.in.web.error.ApiException;
import com.fixa.fixa_api.infrastructure.security.CurrentUserService;
import com.fixa.fixa_api.domain.repository.DisponibilidadRepositoryPort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TurnoCommandService
        implements CrearTurnoUseCase, AprobarTurnoUseCase, com.fixa.fixa_api.application.usecase.CancelarTurnoUseCase,
        com.fixa.fixa_api.application.usecase.CompletarTurnoUseCase,
        com.fixa.fixa_api.application.usecase.ReprogramarTurnoUseCase {

    private final TurnoRepositoryPort turnoPort;
    private final ServicioRepositoryPort servicioPort;
    private final EmpleadoRepositoryPort empleadoPort;
    private final EmpresaRepositoryPort empresaPort;
    private final ConfigReglaQueryPort configReglaPort;
    private final TurnoIntervaloCalculator turnoIntervaloCalculator;
    private final TurnoNotificationService notificationService;
    private final CurrentUserService currentUserService;
    private final DisponibilidadRepositoryPort disponibilidadPort;

    public TurnoCommandService(
            TurnoRepositoryPort turnoPort,
            ServicioRepositoryPort servicioPort,
            EmpleadoRepositoryPort empleadoPort,
            EmpresaRepositoryPort empresaPort,
            ConfigReglaQueryPort configReglaPort,
            TurnoIntervaloCalculator turnoIntervaloCalculator,
            TurnoNotificationService notificationService,
            CurrentUserService currentUserService,
            DisponibilidadRepositoryPort disponibilidadPort) {
        this.turnoPort = turnoPort;
        this.servicioPort = servicioPort;
        this.empleadoPort = empleadoPort;
        this.empresaPort = empresaPort;
        this.configReglaPort = configReglaPort;
        this.turnoIntervaloCalculator = turnoIntervaloCalculator;
        this.notificationService = notificationService;
        this.currentUserService = currentUserService;
        this.disponibilidadPort = disponibilidadPort;
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

        // --- NUEVA VALIDACIÓN: Disponibilidad Horaria (Working Hours) ---
        validarDisponibilidadHoraria(empleado.getId(), inicio, fin);

        // Validaciones Reglas de Negocio
        validarReglasDeNegocio(empresa.getId(), empleado.getId(), inicio, fin, null);

        // Validar Solapamiento
        validarSolapamiento(turno, servicio, empleado.getId(), inicio, fin, null);

        // Setear estado inicial según reglas (empresa)
        turno.setEstado(empresa.isRequiereAprobacionTurno() ? "PENDIENTE" : "CONFIRMADO");

        // La validación es OBLIGATORIA si el usuario NO está logueado (sesión JWT
        // ausente).
        // Incluso si encontramos un cliente por email, si no está logueado, verificamos
        // para evitar suplantación.
        boolean estaAutenticado = currentUserService.getCurrentUserId().isPresent();
        turno.setRequiereValidacion(!estaAutenticado);

        // Persistir via puerto
        Turno guardado = turnoPort.save(turno);

        // Notificar creación al cliente solo si no requiere validación inmediata
        // Si requiere validación, el flujo de verificación enviará el código.
        if (!guardado.isRequiereValidacion()) {
            notificationService.enviarNotificacionCreacion(guardado);
        }

        return guardado;
    }

    @Override
    @Transactional
    public Turno reprogramar(Long turnoId, LocalDateTime nuevaFechaInicio, Long usuarioIdSolicitante) {
        try {
            Turno t = turnoPort.findById(turnoId)
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Turno no encontrado"));

            // Validar pertenencia (si se pasa usuarioId)
            if (usuarioIdSolicitante != null) {
                if (t.getClienteId() != null && !t.getClienteId().equals(usuarioIdSolicitante)) {
                    throw new ApiException(HttpStatus.FORBIDDEN, "No tienes permiso para reprogramar este turno");
                }
            }

            // Validar Estado
            if ("CANCELADO".equalsIgnoreCase(t.getEstado()) || "COMPLETADO".equalsIgnoreCase(t.getEstado())) {
                throw new ApiException(HttpStatus.CONFLICT, "No se puede reprogramar un turno CANCELADO o COMPLETADO");
            }

            // Cargar entidades
            var servicio = servicioPort.findById(t.getServicioId())
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Servicio original no encontrado"));
            var empresa = empresaPort.findById(t.getEmpresaId())
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Empresa no encontrada"));

            // Calcular nuevo fin (SAFEGUARD NULL POINTER)
            Integer durMin = servicio.getDuracionMinutos();
            if (durMin == null || durMin <= 0) {
                // Fallback or Error. Usually service must have duration.
                // If legacy data has null, we can default to e.g. 30, but better to fail or fix
                // data.
                // Given the 500 reported, this is likely the culprit if data is bad.
                throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "El servicio tiene una configuración inválida (duración nula)");
            }

            LocalDateTime fin = nuevaFechaInicio.plusMinutes(durMin);

            // --- NUEVA VALIDACIÓN: Disponibilidad Horaria (Working Hours) ---
            validarDisponibilidadHoraria(t.getEmpleadoId(), nuevaFechaInicio, fin);

            // Validaciones de Reglas (Anticipación, limites, etc)
            validarReglasDeNegocio(empresa.getId(), t.getEmpleadoId(), nuevaFechaInicio, fin, t.getId());

            // Validar Solapamiento (excluyendo el turno actual)
            // Para solapamiento, pasamos el turno tal cual pero con las nuevas fechas
            // simuladas
            // O mejor, validamos contra los demas.
            Turno turnoSimulado = new Turno();
            turnoSimulado.setId(t.getId());
            turnoSimulado.setFechaHoraInicio(nuevaFechaInicio);
            turnoSimulado.setFechaHoraFin(fin);
            turnoSimulado.setEmpleadoId(t.getEmpleadoId());

            validarSolapamiento(turnoSimulado, servicio, t.getEmpleadoId(), nuevaFechaInicio, fin, t.getId());

            // Actualizar fechas
            t.setFechaHoraInicio(nuevaFechaInicio);
            t.setFechaHoraFin(fin);

            // Regla negocio: al reprogramar, ¿cambia de estado?
            // Si estaba CONFIRMADO, pasa a PENDIENTE? O se mantiene?
            // Si era CONFIRMADO, quizás debería seguir CONFIRMADO salvo reglas estrictas.
            // Por simplicidad mantenemos estado o volvemos a PENDIENTE si es cambio
            // drástico.
            // User request: "Cambiar estado a REPROGRAMADO o mantener PENDIENTE."
            // Usaremos: Si empresa requiere aprobación, PENDIENTE. Si no, CONFIRMADO.
            t.setEstado(empresa.isRequiereAprobacionTurno() ? "PENDIENTE" : "CONFIRMADO");

            Turno guardado = turnoPort.save(t);

            // Notificar cambio
            notificationService.enviarNotificacionReprogramacion(guardado);

            return guardado;
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error interno al reprogramar: " + e.getMessage() + " | " + e.getClass().getName());
        }
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

        notificationService.enviarNotificacionAprobacion(guardado);

        return guardado;
    }

    @Override
    @Transactional
    public Turno cancelar(Long turnoId, String motivo) {
        return cancelar(turnoId, motivo, null);
    }

    @Override
    @Transactional
    public Turno cancelar(Long turnoId, String motivo, Long usuarioSolicitanteId) {
        Turno t = turnoPort.findById(turnoId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Turno no encontrado"));

        // Validar pertenencia
        if (usuarioSolicitanteId != null) {
            if (t.getClienteId() != null && !t.getClienteId().equals(usuarioSolicitanteId)) {
                throw new ApiException(HttpStatus.FORBIDDEN, "No tienes permiso para cancelar este turno");
            }
        }

        if ("CANCELADO".equalsIgnoreCase(t.getEstado())) {
            return t;
        }
        if (!"PENDIENTE".equalsIgnoreCase(t.getEstado()) && !"CONFIRMADO".equalsIgnoreCase(t.getEstado())) {
            throw new ApiException(HttpStatus.CONFLICT, "Solo se puede cancelar un turno PENDIENTE o CONFIRMADO");
        }

        // Validar anticipación para cancelar? (Opcional según request)
        // Por ahora lo dejamos pasar.

        t.setEstado("CANCELADO");
        if (motivo != null && !motivo.isBlank()) {
            String obs = t.getObservaciones() == null ? "" : (t.getObservaciones() + "\n");
            t.setObservaciones(obs + "Cancelación: " + motivo);
        }
        Turno guardado = turnoPort.save(t);

        notificationService.enviarNotificacionCancelacion(guardado, motivo);

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

    // --- Helpers Privados ---

    private void validarDisponibilidadHoraria(Long empleadoId, LocalDateTime inicio, LocalDateTime fin) {
        // Mapeo simple de DayOfWeek a String español (según DB)
        String diaSemana = switch (inicio.getDayOfWeek()) {
            case MONDAY -> "LUNES";
            case TUESDAY -> "MARTES";
            case WEDNESDAY -> "MIERCOLES";
            case THURSDAY -> "JUEVES";
            case FRIDAY -> "VIERNES";
            case SATURDAY -> "SABADO";
            case SUNDAY -> "DOMINGO";
        };

        var disponibilidades = disponibilidadPort.findByEmpleadoId(empleadoId);

        boolean horarioValido = disponibilidades.stream()
                .filter(d -> d.getDiaSemana().equalsIgnoreCase(diaSemana))
                .anyMatch(d -> {
                    // Verificar contención: d.inicio <= turno.inicio && d.fin >= turno.fin
                    return !inicio.toLocalTime().isBefore(d.getHoraInicio()) &&
                            !fin.toLocalTime().isAfter(d.getHoraFin());
                });

        if (!horarioValido) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "El horario seleccionado no corresponde a un turno laboral del empleado.");
        }
    }

    private void validarReglasDeNegocio(Long empresaId, Long empleadoId, LocalDateTime inicio, LocalDateTime fin,
            Long turnoIdExcluido) {
        var empresa = empresaPort.findById(empresaId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Empresa no encontrada"));
        var empleado = empleadoPort.findById(empleadoId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Empleado no encontrado"));

        // Regla: anticipación mínima en minutos
        var minAntMinOpt = configReglaPort.getInt(empresaId, "min_anticipacion_minutos");
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

            long count = delDia.stream()
                    .filter(t -> turnoIdExcluido == null || !t.getId().equals(turnoIdExcluido))
                    .count();

            if (count >= maxTurnosDiaOpt.get()) {
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

            long count = deLaSemana.stream()
                    .filter(t -> turnoIdExcluido == null || !t.getId().equals(turnoIdExcluido))
                    .count();

            if (count >= maxTurnosSemanaOpt.get()) {
                throw new ApiException(HttpStatus.CONFLICT,
                        "Se superó el máximo de turnos por semana para el empleado");
            }
        }
    }

    private void validarSolapamiento(Turno turno, com.fixa.fixa_api.domain.model.Servicio servicio, Long empleadoId,
            LocalDateTime inicio, LocalDateTime fin, Long turnoIdExcluido) {
        LocalDateTime ventanaInicio = inicio.minusHours(12);
        var existentes = turnoPort.findByEmpleadoIdAndRango(empleadoId, ventanaInicio, fin);

        // Filtrar solo turnos que ocupan espacio (no cancelados/completados si se desea
        // liberar, pero completado suele ocupar)
        // Asumimos CONFIRMADO, PENDIENTE, PENDIENTE_APROBACION ocupan.
        List<Turno> conflictosPotenciales = existentes.stream()
                .filter(t -> "CONFIRMADO".equalsIgnoreCase(t.getEstado()) ||
                        "PENDIENTE".equalsIgnoreCase(t.getEstado()) ||
                        "PENDIENTE_APROBACION".equalsIgnoreCase(t.getEstado()))
                .filter(t -> turnoIdExcluido == null || !t.getId().equals(turnoIdExcluido))
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
    }
}
