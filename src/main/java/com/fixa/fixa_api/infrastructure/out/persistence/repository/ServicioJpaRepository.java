package com.fixa.fixa_api.infrastructure.out.persistence.repository;

import com.fixa.fixa_api.infrastructure.out.persistence.entity.ServicioEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServicioJpaRepository extends JpaRepository<ServicioEntity, Long> {
    List<ServicioEntity> findByEmpresa_IdAndActivoTrue(Long empresaId);
}
