package com.fixa.fixa_api.domain.repository;

import com.fixa.fixa_api.domain.model.Suscripcion;

import java.util.List;
import java.util.Optional;

public interface SuscripcionRepositoryPort {
    List<Suscripcion> findAll();

    Optional<Suscripcion> findById(Long id);

    Optional<Suscripcion> findActivaByEmpresaId(Long empresaId);

    List<Suscripcion> findByEmpresaId(Long empresaId);

    Suscripcion save(Suscripcion suscripcion);

    void deleteById(Long id);
}
