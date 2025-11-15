package com.fixa.fixa_api.infrastructure.out.persistence.repository;

import com.fixa.fixa_api.infrastructure.out.persistence.entity.ValoracionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ValoracionJpaRepository extends JpaRepository<ValoracionEntity, Long> {
    Optional<ValoracionEntity> findByTurno_Id(Long turnoId);
    List<ValoracionEntity> findByEmpresa_IdAndActivoTrue(Long empresaId);
    List<ValoracionEntity> findByUsuario_Id(Long usuarioId);
    boolean existsByTurno_Id(Long turnoId);
}
