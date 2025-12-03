package com.fixa.fixa_api.application.service;

import com.fixa.fixa_api.domain.model.Servicio;
import com.fixa.fixa_api.domain.model.ServicioEtapa;
import com.fixa.fixa_api.domain.model.Turno;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class TurnoIntervaloCalculator {

    public record Intervalo(LocalDateTime inicio, LocalDateTime fin) {
    }

    public record EtapaIntervalo(LocalDateTime inicio, LocalDateTime fin, ServicioEtapa.TipoEtapa tipo) {
    }

    /**
     * Calcula los intervalos de tiempo en los que el empleado está OCUPADO
     * durante un turno, considerando las etapas de trabajo y espera.
     */
    public List<Intervalo> calcularIntervalosOcupados(Turno turno, Servicio servicio) {
        List<Intervalo> ocupados = new ArrayList<>();
        LocalDateTime cursor = turno.getFechaHoraInicio();

        for (ServicioEtapa etapa : servicio.getEtapas()) {
            LocalDateTime finEtapa = cursor.plusMinutes(etapa.getDuracionMinutos());

            if (etapa.getTipo() == ServicioEtapa.TipoEtapa.TRABAJO) {
                ocupados.add(new Intervalo(cursor, finEtapa));
            }
            // Si es ESPERA, no agregamos intervalo (el empleado está libre)

            cursor = finEtapa;
        }

        return ocupados;
    }

    /**
     * Desglosa todas las etapas del turno con sus horarios calculados.
     */
    public List<EtapaIntervalo> desglosarEtapas(Turno turno, Servicio servicio) {
        List<EtapaIntervalo> etapas = new ArrayList<>();
        LocalDateTime cursor = turno.getFechaHoraInicio();

        for (ServicioEtapa etapa : servicio.getEtapas()) {
            LocalDateTime finEtapa = cursor.plusMinutes(etapa.getDuracionMinutos());
            etapas.add(new EtapaIntervalo(cursor, finEtapa, etapa.getTipo()));
            cursor = finEtapa;
        }

        return etapas;
    }

    /**
     * Verifica si un nuevo turno (con sus intervalos) solapa con intervalos
     * ocupados existentes.
     */
    public boolean haySolapamiento(Turno nuevoTurno, Servicio nuevoServicio, List<Turno> turnosExistentes,
            java.util.function.Function<Long, Servicio> servicioProvider) {
        List<Intervalo> intervalosNuevo = calcularIntervalosOcupados(nuevoTurno, nuevoServicio);

        for (Turno existente : turnosExistentes) {
            // Optimización: si los rangos totales no se tocan, no analizar detalles
            if (!seTocan(nuevoTurno.getFechaHoraInicio(), nuevoTurno.getFechaHoraFin(),
                    existente.getFechaHoraInicio(), existente.getFechaHoraFin())) {
                continue;
            }

            Servicio servicioExistente = servicioProvider.apply(existente.getServicioId());
            // Si no se encuentra servicio (caso raro), asumir bloque completo
            List<Intervalo> intervalosExistente;
            if (servicioExistente != null) {
                intervalosExistente = calcularIntervalosOcupados(existente, servicioExistente);
            } else {
                intervalosExistente = List
                        .of(new Intervalo(existente.getFechaHoraInicio(), existente.getFechaHoraFin()));
            }

            // Verificar choque de intervalos
            for (Intervalo i1 : intervalosNuevo) {
                for (Intervalo i2 : intervalosExistente) {
                    if (seSuperponen(i1.inicio(), i1.fin(), i2.inicio(), i2.fin())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean seTocan(LocalDateTime i1, LocalDateTime f1, LocalDateTime i2, LocalDateTime f2) {
        return i1.isBefore(f2) && f1.isAfter(i2);
    }

    private boolean seSuperponen(LocalDateTime i1, LocalDateTime f1, LocalDateTime i2, LocalDateTime f2) {
        return i1.isBefore(f2) && f1.isAfter(i2);
    }
}
