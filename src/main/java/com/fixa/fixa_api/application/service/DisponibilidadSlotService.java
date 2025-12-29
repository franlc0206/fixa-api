package com.fixa.fixa_api.application.service;

import com.fixa.fixa_api.domain.model.Disponibilidad;
import com.fixa.fixa_api.domain.model.DisponibilidadSlot;
import com.fixa.fixa_api.domain.model.Turno;
import com.fixa.fixa_api.domain.repository.ServicioRepositoryPort;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import com.fixa.fixa_api.domain.model.Empleado;
import com.fixa.fixa_api.infrastructure.in.web.dto.DisponibilidadGlobalSlot;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Servicio para generar slots de disponibilidad específicos por fecha
 * a partir de la configuración de disponibilidad por día de semana.
 */
@Service
public class DisponibilidadSlotService {

    private final DisponibilidadService disponibilidadService;
    private final EmpleadoService empleadoService;
    private final TurnoQueryService turnoQueryService;
    private final TurnoIntervaloCalculator turnoIntervaloCalculator;
    private final ServicioRepositoryPort servicioRepositoryPort;
    private static final int DURACION_SLOT_MINUTOS = 30; // Slots de 30 minutos
    private static final int DIAS_ADELANTE = 30; // Generar slots para los próximos 30 días

    public DisponibilidadSlotService(DisponibilidadService disponibilidadService,
            EmpleadoService empleadoService,
            TurnoQueryService turnoQueryService,
            TurnoIntervaloCalculator turnoIntervaloCalculator,
            ServicioRepositoryPort servicioRepositoryPort) {
        this.disponibilidadService = disponibilidadService;
        this.empleadoService = empleadoService;
        this.turnoQueryService = turnoQueryService;
        this.turnoIntervaloCalculator = turnoIntervaloCalculator;
        this.servicioRepositoryPort = servicioRepositoryPort;
    }

    /**
     * Genera slots de disponibilidad para un empleado en los próximos N días.
     * 
     * @param empleadoId ID del empleado
     * @return Lista de slots disponibles
     */
    public List<DisponibilidadSlot> generarSlotsParaEmpleado(Long empleadoId) {
        // Obtener configuración de disponibilidad por día de semana
        List<Disponibilidad> configuraciones = disponibilidadService.listarPorEmpleado(empleadoId);

        if (configuraciones.isEmpty()) {
            return new ArrayList<>();
        }

        // Generar slots para los próximos N días
        LocalDate fechaInicio = LocalDate.now();
        LocalDate fechaFin = fechaInicio.plusDays(DIAS_ADELANTE);

        List<DisponibilidadSlot> slots = new ArrayList<>();
        Long slotId = 1L; // ID temporal para los slots

        for (LocalDate fecha = fechaInicio; fecha.isBefore(fechaFin); fecha = fecha.plusDays(1)) {
            // Obtener día de la semana en español
            String diaSemana = obtenerDiaSemanaEspanol(fecha.getDayOfWeek());

            // Buscar configuración para este día
            List<Disponibilidad> configsDia = configuraciones.stream()
                    .filter(config -> config.getDiaSemana().equalsIgnoreCase(diaSemana))
                    .collect(Collectors.toList());

            // Generar slots para cada configuración de este día
            for (Disponibilidad config : configsDia) {
                List<DisponibilidadSlot> slotsDia = generarSlotsParaDia(
                        slotId,
                        empleadoId,
                        fecha,
                        config.getHoraInicio(),
                        config.getHoraFin());
                slots.addAll(slotsDia);
                slotId += slotsDia.size();
            }
        }

        // Marcar slots ocupados basándose en turnos existentes
        marcarSlotsOcupados(slots, empleadoId, fechaInicio, fechaFin);

        return slots;
    }

    /**
     * Genera slots agregados de todos los empleados disponibles para un servicio y
     * empresa.
     */
    public List<DisponibilidadGlobalSlot> generarSlotsGlobales(Long empresaId, Long servicioId) {
        List<Empleado> empleados = empleadoService.listarPublicosPorEmpresa(empresaId);
        Map<String, DisponibilidadGlobalSlot> slotsMap = new HashMap<>();

        for (Empleado emp : empleados) {
            if (!Boolean.TRUE.equals(emp.isActivo()))
                continue;

            List<DisponibilidadSlot> slots = generarSlotsParaEmpleado(emp.getId());
            slots = filtrarSlotsDisponibles(slots);
            if (servicioId != null) {
                slots = filtrarSlotsPorServicio(slots, servicioId);
            }

            for (DisponibilidadSlot s : slots) {
                String key = s.getFecha().toString() + " " + s.getHoraInicio().toString();

                slotsMap.computeIfAbsent(key, k -> DisponibilidadGlobalSlot.builder()
                        .fecha(s.getFecha())
                        .horaInicio(s.getHoraInicio())
                        .horaFin(s.getHoraFin())
                        .empleadosDisponibles(new ArrayList<>())
                        .build())
                        .getEmpleadosDisponibles()
                        .add(DisponibilidadGlobalSlot.EmpleadoResumen.builder()
                                .id(emp.getId())
                                .nombre(emp.getNombre())
                                .apellido(emp.getApellido())
                                .fotoUrl(emp.getFotoUrl())
                                .build());
            }
        }

        return slotsMap.values().stream()
                .sorted(Comparator.comparing(DisponibilidadGlobalSlot::getFecha)
                        .thenComparing(DisponibilidadGlobalSlot::getHoraInicio))
                .collect(Collectors.toList());
    }

    /**
     * Genera slots de 30 minutos para un día específico
     */
    private List<DisponibilidadSlot> generarSlotsParaDia(
            Long slotIdInicial,
            Long empleadoId,
            LocalDate fecha,
            LocalTime horaInicio,
            LocalTime horaFin) {
        List<DisponibilidadSlot> slots = new ArrayList<>();
        LocalTime horaActual = horaInicio;
        Long slotId = slotIdInicial;

        while (horaActual.isBefore(horaFin)) {
            LocalTime horaFinSlot = horaActual.plusMinutes(DURACION_SLOT_MINUTOS);

            // No crear slot si se pasa del horario de fin
            if (horaFinSlot.isAfter(horaFin)) {
                break;
            }

            DisponibilidadSlot slot = DisponibilidadSlot.builder()
                    .id(slotId++)
                    .empleadoId(empleadoId)
                    .fecha(fecha)
                    .horaInicio(horaActual)
                    .horaFin(horaFinSlot)
                    .disponible(true) // Por defecto todos disponibles
                    .build();

            slots.add(slot);
            horaActual = horaFinSlot;
        }

        return slots;
    }

    /**
     * Convierte DayOfWeek a nombre en español (mayúsculas)
     */
    private String obtenerDiaSemanaEspanol(DayOfWeek dayOfWeek) {
        return switch (dayOfWeek) {
            case MONDAY -> "LUNES";
            case TUESDAY -> "MARTES";
            case WEDNESDAY -> "MIERCOLES";
            case THURSDAY -> "JUEVES";
            case FRIDAY -> "VIERNES";
            case SATURDAY -> "SABADO";
            case SUNDAY -> "DOMINGO";
        };
    }

    /**
     * Marca slots como no disponibles cuando hay turnos confirmados o pendientes.
     * Usa TurnoIntervaloCalculator para considerar solo los tiempos de TRABAJO.
     */
    private void marcarSlotsOcupados(List<DisponibilidadSlot> slots, Long empleadoId,
            LocalDate fechaInicio, LocalDate fechaFin) {
        // Obtener todos los turnos del empleado en el rango de fechas
        LocalDateTime desde = fechaInicio.atStartOfDay();
        LocalDateTime hasta = fechaFin.atStartOfDay();

        List<Turno> turnos = turnoQueryService.listar(null, empleadoId, null, desde, hasta, null, null);

        // Filtrar solo turnos confirmados y pendientes
        List<Turno> turnosActivos = turnos.stream()
                .filter(t -> "CONFIRMADO".equalsIgnoreCase(t.getEstado()) ||
                        "PENDIENTE".equalsIgnoreCase(t.getEstado()) ||
                        "PENDIENTE_APROBACION".equalsIgnoreCase(t.getEstado()))
                .collect(Collectors.toList());

        // Pre-calcular intervalos ocupados para todos los turnos
        List<TurnoIntervaloCalculator.Intervalo> intervalosOcupados = new ArrayList<>();
        for (Turno turno : turnosActivos) {
            com.fixa.fixa_api.domain.model.Servicio servicio = servicioRepositoryPort.findById(turno.getServicioId())
                    .orElse(null);
            if (servicio != null) {
                intervalosOcupados.addAll(turnoIntervaloCalculator.calcularIntervalosOcupados(turno, servicio));
            } else {
                // Fallback si no hay servicio: ocupar todo el rango
                intervalosOcupados.add(
                        new TurnoIntervaloCalculator.Intervalo(turno.getFechaHoraInicio(), turno.getFechaHoraFin()));
            }
        }

        // Para cada slot, verificar si se superpone con algún intervalo ocupado
        for (DisponibilidadSlot slot : slots) {
            LocalDateTime slotInicio = LocalDateTime.of(slot.getFecha(), slot.getHoraInicio());
            LocalDateTime slotFin = LocalDateTime.of(slot.getFecha(), slot.getHoraFin());

            boolean ocupado = intervalosOcupados.stream().anyMatch(
                    intervalo -> seSuperponenHorarios(slotInicio, slotFin, intervalo.inicio(), intervalo.fin()));

            if (ocupado) {
                slot.setDisponible(false);
            }
        }
    }

    /**
     * Verifica si dos rangos de horarios se superponen
     */
    private boolean seSuperponenHorarios(LocalDateTime inicio1, LocalDateTime fin1,
            LocalDateTime inicio2, LocalDateTime fin2) {
        // Dos rangos se superponen si:
        // - El inicio de uno está dentro del otro
        // - El fin de uno está dentro del otro
        // - Uno contiene completamente al otro
        return (inicio1.isBefore(fin2) && fin1.isAfter(inicio2));
    }

    /**
     * Filtra slots para eliminar los que ya pasaron (fecha/hora anterior a ahora)
     */
    public List<DisponibilidadSlot> filtrarSlotsDisponibles(List<DisponibilidadSlot> slots) {
        LocalDate hoy = LocalDate.now();
        LocalTime ahora = LocalTime.now();

        return slots.stream()
                .filter(slot -> {
                    // Si es una fecha futura, incluirlo
                    if (slot.getFecha().isAfter(hoy)) {
                        return true;
                    }
                    // Si es hoy, verificar que la hora no haya pasado
                    if (slot.getFecha().isEqual(hoy)) {
                        return slot.getHoraInicio().isAfter(ahora);
                    }
                    // Si es una fecha pasada, no incluirlo
                    return false;
                })
                .filter(DisponibilidadSlot::getDisponible) // Solo slots disponibles
                .collect(Collectors.toList());
    }

    /**
     * Filtra los slots para dejar solo aquellos donde es posible INICIAR el
     * servicio indicado,
     * considerando sus etapas de trabajo y espera.
     */
    public List<DisponibilidadSlot> filtrarSlotsPorServicio(List<DisponibilidadSlot> slots, Long servicioId) {
        com.fixa.fixa_api.domain.model.Servicio servicio = servicioRepositoryPort.findById(servicioId).orElse(null);
        if (servicio == null)
            return slots;

        List<com.fixa.fixa_api.domain.model.ServicioEtapa> etapas = servicio.getEtapas();
        // Si es simple (sin etapas o solo 1 trabajo), la lógica actual del front
        // (buscar contiguos) sirve,
        // pero para ser consistentes, validamos aquí también.

        List<DisponibilidadSlot> slotsValidos = new ArrayList<>();

        // Agrupar slots por fecha para facilitar búsqueda
        java.util.Map<LocalDate, List<DisponibilidadSlot>> slotsPorFecha = slots.stream()
                .collect(Collectors.groupingBy(DisponibilidadSlot::getFecha));

        for (DisponibilidadSlot slotInicio : slots) {
            if (!slotInicio.getDisponible())
                continue;

            if (esInicioValido(slotInicio, etapas, slotsPorFecha.get(slotInicio.getFecha()))) {
                slotsValidos.add(slotInicio);
            }
        }
        return slotsValidos;
    }

    private boolean esInicioValido(DisponibilidadSlot slotInicio,
            List<com.fixa.fixa_api.domain.model.ServicioEtapa> etapas, List<DisponibilidadSlot> slotsDia) {
        if (slotsDia == null)
            return false;

        LocalDateTime cursor = LocalDateTime.of(slotInicio.getFecha(), slotInicio.getHoraInicio());

        for (com.fixa.fixa_api.domain.model.ServicioEtapa etapa : etapas) {
            int duracion = etapa.getDuracionMinutos();
            LocalDateTime finEtapa = cursor.plusMinutes(duracion);

            // Si es TRABAJO, necesitamos que todos los slots en [cursor, finEtapa) estén
            // DISPONIBLES
            if (etapa.getTipo() == com.fixa.fixa_api.domain.model.ServicioEtapa.TipoEtapa.TRABAJO) {
                if (!estanSlotsDisponibles(cursor, finEtapa, slotsDia)) {
                    return false;
                }
            }
            // Si es ESPERA, no importa si están ocupados o libres, solo avanzamos el cursor

            cursor = finEtapa;
        }
        return true;
    }

    private boolean estanSlotsDisponibles(LocalDateTime inicio, LocalDateTime fin, List<DisponibilidadSlot> slotsDia) {
        // Verificar cada intervalo de 30 min dentro del rango
        LocalDateTime check = inicio;
        while (check.isBefore(fin)) {
            LocalDateTime checkFin = check.plusMinutes(DURACION_SLOT_MINUTOS);

            // Buscar slot que cubra [check, checkFin)
            final LocalDateTime c = check;
            boolean encontradoYLibre = slotsDia.stream().anyMatch(
                    s -> (s.getHoraInicio().equals(c.toLocalTime()) || s.getHoraInicio().isBefore(c.toLocalTime())) &&
                            (s.getHoraFin().equals(checkFin.toLocalTime())
                                    || s.getHoraFin().isAfter(checkFin.toLocalTime()))
                            &&
                            s.getDisponible());

            if (!encontradoYLibre)
                return false;

            check = checkFin;
        }
        return true;
    }
}
