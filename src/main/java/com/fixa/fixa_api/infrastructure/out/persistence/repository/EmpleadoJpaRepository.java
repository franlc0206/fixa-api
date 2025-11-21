package com.fixa.fixa_api.infrastructure.out.persistence.repository;

import com.fixa.fixa_api.infrastructure.out.persistence.entity.EmpleadoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmpleadoJpaRepository extends JpaRepository<EmpleadoEntity, Long> {
    List<EmpleadoEntity> findByEmpresa_Id(Long empresaId);
    List<EmpleadoEntity> findByEmpresa_IdAndTrabajaPublicamenteTrueAndActivoTrue(Long empresaId);
    List<EmpleadoEntity> findByEmailAndUsuarioIsNullAndActivoTrue(String email);
    List<EmpleadoEntity> findByUsuario_Id(Long usuarioId);
}
