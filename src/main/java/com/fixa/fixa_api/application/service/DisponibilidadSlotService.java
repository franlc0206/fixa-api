package com.fixa.fixa_api.application.service;

import com.fixa.fixa_api.domain.model.Disponibilidad;
import com.fixa.fixa_api.domain.model.DisponibilidadSlot;
import com.fixa.fixa_api.domain.model.Turno;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Servicio para generar slots de disponibilidad específicos por fecha
 * a partir de la configuración de disponibilidad por día de semana.
 */
@Service
public class DisponibilidadSlotService {

    private final DisponibilidadService disponibilidadService;
    private final TurnoQueryService turnoQueryService;
    private static final int DURACION_SLOT_MINUTOS = 30; // Slots de 30 minutos
    private static final int DIAS_ADELANTE = 30; // Generar slots para los próximos 30 días

    public DisponibilidadSlotService(DisponibilidadService disponibilidadService, 
                                     TurnoQueryService turnoQueryService) {
        this.disponibilidadService = disponibilidadService;
        this.turnoQueryService = turnoQueryService;
    }

    /**
     * Genera slots de disponibilidad para un empleado en los próximos N días.
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
                    config.getHoraFin()
                );
                slots.addAll(slotsDia);
                slotId += slotsDia.size();
            }
        }

        // Marcar slots ocupados basándose en turnos existentes
        marcarSlotsOcupados(slots, empleadoId, fechaInicio, fechaFin);

        return slots;
    }

    /**
     * Genera slots de 30 minutos para un día específico
     */
    private List<DisponibilidadSlot> generarSlotsParaDia(
            Long slotIdInicial,
            Long empleadoId,
            LocalDate fecha,
            LocalTime horaInicio,
            LocalTime horaFin
    ) {
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
     * Marca slots como no disponibles cuando hay turnos confirmados o pendientes
     */
    private void marcarSlotsOcupados(List<DisponibilidadSlot> slots, Long empleadoId, 
                                     LocalDate fechaInicio, LocalDate fechaFin) {
        // Obtener todos los turnos del empleado en el rango de fechas
        LocalDateTime desde = fechaInicio.atStartOfDay();
        LocalDateTime hasta = fechaFin.atStartOfDay();
        
        List<Turno> turnos = turnoQueryService.listar(null, empleadoId, null, desde, hasta, null, null);
        
        // Filtrar solo turnos confirmados y pendientes (que realmente ocupan el horario)
        List<Turno> turnosOcupados = turnos.stream()
                .filter(t -> "CONFIRMADO".equalsIgnoreCase(t.getEstado()) || 
                            "PENDIENTE".equalsIgnoreCase(t.getEstado()) ||
                            "PENDIENTE_APROBACION".equalsIgnoreCase(t.getEstado()))
                .collect(Collectors.toList());
        
        // Para cada slot, verificar si se superpone con algún turno
        for (DisponibilidadSlot slot : slots) {
            LocalDateTime slotInicio = LocalDateTime.of(slot.getFecha(), slot.getHoraInicio());
            LocalDateTime slotFin = LocalDateTime.of(slot.getFecha(), slot.getHoraFin());
            
            boolean ocupado = turnosOcupados.stream().anyMatch(turno -> 
                seSuperponenHorarios(slotInicio, slotFin, turno.getFechaHoraInicio(), turno.getFechaHoraFin())
            );
            
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
}
