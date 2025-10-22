package com.fixa.fixa_api.domain.repository;

import com.fixa.fixa_api.domain.model.Empresa;
import java.util.Optional;

public interface EmpresaRepositoryPort {
    Optional<Empresa> findById(Long id);
    Empresa save(Empresa empresa);
}
