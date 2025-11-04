package com.fixa.fixa_api.domain.repository;

import com.fixa.fixa_api.domain.model.Empresa;
import java.util.Optional;
import java.util.List;

public interface EmpresaRepositoryPort {
    Optional<Empresa> findById(Long id);
    Optional<Empresa> findBySlug(String slug);
    Empresa save(Empresa empresa);
    List<Empresa> findAll();
    List<Empresa> findVisibles();
}
