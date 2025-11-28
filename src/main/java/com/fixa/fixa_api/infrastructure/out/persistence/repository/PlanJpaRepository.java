package com.fixa.fixa_api.infrastructure.out.persistence.repository;

import com.fixa.fixa_api.infrastructure.out.persistence.entity.PlanEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlanJpaRepository extends JpaRepository<PlanEntity, Long> {
    List<PlanEntity> findByActivo(boolean activo);
}
