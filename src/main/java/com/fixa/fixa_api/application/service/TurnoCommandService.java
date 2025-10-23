package com.fixa.fixa_api.application.service;

import com.fixa.fixa_api.application.usecase.AprobarTurnoUseCase;
import com.fixa.fixa_api.application.usecase.CrearTurnoUseCase;
import com.fixa.fixa_api.domain.model.Turno;
import com.fixa.fixa_api.domain.repository.TurnoRepositoryPort;
import com.fixa.fixa_api.domain.repository.EmpresaRepositoryPort;
import com.fixa.fixa_api.domain.repository.ServicioRepositoryPort;
import com.fixa.fixa_api.domain.repository.EmpleadoRepositoryPort;
import com.fixa.fixa_api.infrastructure.in.web.error.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TurnoCommandService implements CrearTurnoUseCase, AprobarTurnoUseCase {

    private final TurnoRepositoryPort turnoPort;
    private final ServicioRepositoryPort servicioPort;
    private final EmpleadoRepositoryPort empleadoPort;
    private final EmpresaRepositoryPort empresaPort;

    public TurnoCommandService(
            TurnoRepositoryPort turnoPort,
            ServicioRepositoryPort servicioPort,
            EmpleadoRepositoryPort empleadoPort,
            EmpresaRepositoryPort empresaPort) {
        this.turnoPort = turnoPort;
        this.servicioPort = servicioPort;
        this.empleadoPort = empleadoPort;
        this.empresaPort = empresaPort;
    }

    @Override
    @Transactional
    public Turno ejecutar(Turno turno) {
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

        // Validar solapamiento básico: obtener turnos del día y filtrar en memoria
        LocalDateTime ventanaInicio = inicio.minusHours(12);
        var existentes = turnoPort.findByEmpleadoIdAndRango(empleado.getId(), ventanaInicio, fin);
        boolean solapa = existentes.stream().anyMatch(t ->
                t.getFechaHoraInicio().isBefore(fin) && t.getFechaHoraFin().isAfter(inicio)
        );
        if (solapa) {
            throw new ApiException(HttpStatus.CONFLICT, "Existe solapamiento de turnos para el empleado");
        }

        // Setear estado inicial según reglas (empresa)
        turno.setEstado(empresa.isRequiereAprobacionTurno() ? "PENDIENTE" : "CONFIRMADO");
        turno.setRequiereValidacion(empresa.isRequiereValidacionTelefono());

        // Persistir via puerto
        return turnoPort.save(turno);
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
        return turnoPort.save(t);
    }
}
