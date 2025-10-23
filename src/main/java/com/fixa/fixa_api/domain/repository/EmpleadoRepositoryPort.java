package com.fixa.fixa_api.domain.repository;

import com.fixa.fixa_api.domain.model.Empleado;

import java.util.List;
import java.util.Optional;

public interface EmpleadoRepositoryPort {
    List<Empleado> findByEmpresaId(Long empresaId);
    Optional<Empleado> findById(Long id);
    Empleado save(Empleado empleado);
    void deleteById(Long id);
    boolean existsById(Long id);
}
