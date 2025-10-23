package com.fixa.fixa_api.domain.repository;

import com.fixa.fixa_api.domain.model.Servicio;

import java.util.List;
import java.util.Optional;

public interface ServicioRepositoryPort {
    List<Servicio> findByEmpresaId(Long empresaId);
    Optional<Servicio> findById(Long id);
    Servicio save(Servicio servicio);
    void deleteById(Long id);
    boolean existsById(Long id);
}
