package com.fixa.fixa_api.domain.repository;

import com.fixa.fixa_api.domain.model.UsuarioEmpresa;

import java.util.List;
import java.util.Optional;

public interface UsuarioEmpresaRepositoryPort {
    List<Long> findEmpresaIdsByUsuario(Long usuarioId);
    boolean existsByUsuarioAndEmpresa(Long usuarioId, Long empresaId);
    UsuarioEmpresa save(UsuarioEmpresa ue);
    void deleteByUsuarioAndEmpresa(Long usuarioId, Long empresaId);
    Optional<UsuarioEmpresa> findByUsuarioAndEmpresa(Long usuarioId, Long empresaId);
    List<UsuarioEmpresa> findByUsuario(Long usuarioId);
    List<UsuarioEmpresa> findByEmpresa(Long empresaId);
    List<UsuarioEmpresa> findAll();
}
