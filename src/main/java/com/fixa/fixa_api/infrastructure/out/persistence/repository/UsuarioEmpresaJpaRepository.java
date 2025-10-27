package com.fixa.fixa_api.infrastructure.out.persistence.repository;

import com.fixa.fixa_api.infrastructure.out.persistence.entity.UsuarioEmpresaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioEmpresaJpaRepository extends JpaRepository<UsuarioEmpresaEntity, Long> {
    boolean existsByUsuario_IdAndEmpresa_Id(Long usuarioId, Long empresaId);
    Optional<UsuarioEmpresaEntity> findByUsuario_IdAndEmpresa_Id(Long usuarioId, Long empresaId);
    List<UsuarioEmpresaEntity> findByUsuario_IdAndActivoTrue(Long usuarioId);
    List<UsuarioEmpresaEntity> findByUsuario_Id(Long usuarioId);
    List<UsuarioEmpresaEntity> findByEmpresa_Id(Long empresaId);
}
