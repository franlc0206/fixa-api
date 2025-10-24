package com.fixa.fixa_api.application.service;

import com.fixa.fixa_api.domain.model.Turno;
import com.fixa.fixa_api.domain.repository.TurnoRepositoryPort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class TurnoQueryService {

    private final TurnoRepositoryPort turnoPort;

    public TurnoQueryService(TurnoRepositoryPort turnoPort) {
        this.turnoPort = turnoPort;
    }

    public List<Turno> listar(Long empresaId,
                              Long empleadoId,
                              String estado,
                              LocalDateTime desde,
                              LocalDateTime hasta,
                              Integer page,
                              Integer size) {
        LocalDateTime from = desde != null ? desde : LocalDateTime.now().minusDays(30);
        LocalDateTime to = hasta != null ? hasta : LocalDateTime.now().plusDays(30);

        List<Turno> base;
        if (empleadoId != null) {
            base = turnoPort.findByEmpleadoIdAndRango(empleadoId, from, to);
        } else if (empresaId != null) {
            base = turnoPort.findByEmpresaIdAndRango(empresaId, from, to);
        } else {
            // Sin filtros de empresa/empleado, devolver vacío para evitar dump completo
            base = List.of();
        }

        List<Turno> filtrado = base.stream()
                .filter(t -> estado == null || (t.getEstado() != null && t.getEstado().equalsIgnoreCase(estado)))
                .sorted(Comparator.comparing(Turno::getFechaHoraInicio))
                .collect(Collectors.toList());

        if (page == null || size == null || page < 0 || size <= 0) {
            return filtrado;
        }
        int fromIdx = Math.min(page * size, filtrado.size());
        int toIdx = Math.min(fromIdx + size, filtrado.size());
        return filtrado.subList(fromIdx, toIdx);
    }

    public java.util.Optional<Turno> obtener(Long id) {
        return turnoPort.findById(id);
    }
}
