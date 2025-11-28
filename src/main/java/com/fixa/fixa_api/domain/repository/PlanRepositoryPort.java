package com.fixa.fixa_api.domain.repository;

import com.fixa.fixa_api.domain.model.Plan;

import java.util.List;
import java.util.Optional;

public interface PlanRepositoryPort {
    List<Plan> findAll();

    List<Plan> findByActivo(boolean activo);

    Optional<Plan> findById(Long id);

    Plan save(Plan plan);

    void deleteById(Long id);
}
