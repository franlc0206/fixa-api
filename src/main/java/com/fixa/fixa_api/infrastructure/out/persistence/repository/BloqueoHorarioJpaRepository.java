package com.fixa.fixa_api.infrastructure.out.persistence.repository;

import com.fixa.fixa_api.infrastructure.out.persistence.entity.BloqueoHorarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BloqueoHorarioJpaRepository extends JpaRepository<BloqueoHorarioEntity, Long> {
}
