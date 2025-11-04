package com.fixa.fixa_api.application.service;

import com.fixa.fixa_api.domain.model.Turno;
import com.fixa.fixa_api.domain.repository.TurnoRepositoryPort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Servicio de aplicación para consultas del calendario.
 * Siguiendo arquitectura hexagonal, este servicio:
 * - Solo depende de puertos (interfaces) del dominio
 * - No conoce detalles de implementación de infraestructura
 * - Contiene lógica de negocio relacionada con vistas de calendario
 */
@Service
public class CalendarioQueryService {

    private final TurnoRepositoryPort turnoPort;

    public CalendarioQueryService(TurnoRepositoryPort turnoPort) {
        this.turnoPort = turnoPort;
    }

    /**
     * Obtiene los turnos de una empresa en un rango de fechas para vista de calendario.
     * 
     * @param empresaId ID de la empresa
     * @param desde Fecha/hora de inicio del rango (inclusive)
     * @param hasta Fecha/hora de fin del rango (inclusive)
     * @param empleadoId Filtro opcional por empleado
     * @param estados Filtro opcional por estados (ej: CONFIRMADO, PENDIENTE)
     * @return Lista de turnos en el rango especificado
     */
    public List<Turno> obtenerTurnosParaCalendario(
            Long empresaId,
            LocalDateTime desde,
            LocalDateTime hasta,
            Long empleadoId,
            List<String> estados) {
        
        // Si no se especifica rango, usar rango por defecto (mes actual)
        if (desde == null) {
            desde = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        }
        if (hasta == null) {
            hasta = desde.plusMonths(1).minusSeconds(1);
        }

        // Consultar turnos por empresa y rango
        List<Turno> turnos;
        if (empleadoId != null) {
            turnos = turnoPort.findByEmpleadoIdAndRango(empleadoId, desde, hasta);
        } else {
            // Obtener todos los turnos de la empresa en el rango
            turnos = turnoPort.findByEmpresaIdAndRango(empresaId, desde, hasta);
        }

        // Filtrar por estados si se especificaron
        if (estados != null && !estados.isEmpty()) {
            turnos = turnos.stream()
                    .filter(t -> estados.stream().anyMatch(estado -> estado.equalsIgnoreCase(t.getEstado())))
                    .toList();
        }

        return turnos;
    }
}
