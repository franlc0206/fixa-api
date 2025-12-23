package com.fixa.fixa_api.infrastructure.out.persistence.repository;

import com.fixa.fixa_api.infrastructure.out.persistence.entity.EmpresaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmpresaJpaRepository extends JpaRepository<EmpresaEntity, Long> {
    List<EmpresaEntity> findByVisibilidadPublicaTrue();

    Optional<EmpresaEntity> findBySlug(String slug);

    Optional<EmpresaEntity> findByUsuarioAdminId(Long usuarioId);
}
