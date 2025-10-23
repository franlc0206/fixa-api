package com.fixa.fixa_api.domain.repository;

import com.fixa.fixa_api.domain.model.Disponibilidad;

import java.util.List;
import java.util.Optional;

public interface DisponibilidadRepositoryPort {
    List<Disponibilidad> findByEmpleadoId(Long empleadoId);
    Optional<Disponibilidad> findById(Long id);
    Disponibilidad save(Disponibilidad disponibilidad);
    void deleteById(Long id);
    boolean existsById(Long id);
}
