package com.fixa.fixa_api.domain.repository;

import com.fixa.fixa_api.domain.model.Turno;
import java.util.List;
import java.util.Optional;

public interface TurnoRepositoryPort {
    Turno save(Turno turno);
    Optional<Turno> findById(Long id);
    List<Turno> findByEmpleadoIdAndRango(Long empleadoId, java.time.LocalDateTime desde, java.time.LocalDateTime hasta);
    List<Turno> findByEmpresaIdAndRango(Long empresaId, java.time.LocalDateTime desde, java.time.LocalDateTime hasta);
}
