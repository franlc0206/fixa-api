package com.fixa.fixa_api.infrastructure.out.persistence.repository;

import com.fixa.fixa_api.infrastructure.out.persistence.entity.EmpresaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmpresaJpaRepository extends JpaRepository<EmpresaEntity, Long> {
    List<EmpresaEntity> findByVisibilidadPublicaTrue();
}
