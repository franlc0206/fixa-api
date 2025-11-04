package com.fixa.fixa_api.domain.repository;

import com.fixa.fixa_api.domain.model.VerificacionTelefono;

import java.util.Optional;

/**
 * Puerto (interface) para el repositorio de VerificacionTelefono.
 * Siguiendo arquitectura hexagonal, esta interface pertenece al dominio
 * y será implementada por un adapter en la capa de infraestructura.
 */
public interface VerificacionTelefonoRepositoryPort {
    VerificacionTelefono save(VerificacionTelefono verificacion);
    Optional<VerificacionTelefono> findById(Long id);
    Optional<VerificacionTelefono> findByTelefonoAndValidadoFalse(String telefono);
}
