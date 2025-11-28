package com.fixa.fixa_api.infrastructure.out.persistence.repository;

import com.fixa.fixa_api.infrastructure.out.persistence.entity.SuscripcionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SuscripcionJpaRepository extends JpaRepository<SuscripcionEntity, Long> {
    List<SuscripcionEntity> findByEmpresaId(Long empresaId);

    @Query("SELECT s FROM SuscripcionEntity s WHERE s.empresaId = :empresaId AND s.activo = true AND (s.fechaFin IS NULL OR s.fechaFin > CURRENT_TIMESTAMP)")
    Optional<SuscripcionEntity> findActivaByEmpresaId(@Param("empresaId") Long empresaId);
}
