package com.fixa.fixa_api.infrastructure.out.persistence.adapter;

import com.fixa.fixa_api.domain.model.UsuarioEmpresa;
import com.fixa.fixa_api.domain.repository.UsuarioEmpresaRepositoryPort;
import com.fixa.fixa_api.infrastructure.out.persistence.entity.EmpresaEntity;
import com.fixa.fixa_api.infrastructure.out.persistence.entity.UsuarioEmpresaEntity;
import com.fixa.fixa_api.infrastructure.out.persistence.entity.UsuarioEntity;
import com.fixa.fixa_api.infrastructure.out.persistence.mapper.UsuarioEmpresaMapper;
import com.fixa.fixa_api.infrastructure.out.persistence.repository.EmpresaJpaRepository;
import com.fixa.fixa_api.infrastructure.out.persistence.repository.UsuarioEmpresaJpaRepository;
import com.fixa.fixa_api.infrastructure.out.persistence.repository.UsuarioJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class UsuarioEmpresaRepositoryAdapter implements UsuarioEmpresaRepositoryPort {

    private final UsuarioEmpresaJpaRepository ueRepo;
    private final UsuarioJpaRepository usuarioRepo;
    private final EmpresaJpaRepository empresaRepo;

    public UsuarioEmpresaRepositoryAdapter(UsuarioEmpresaJpaRepository ueRepo,
                                           UsuarioJpaRepository usuarioRepo,
                                           EmpresaJpaRepository empresaRepo) {
        this.ueRepo = ueRepo;
        this.usuarioRepo = usuarioRepo;
        this.empresaRepo = empresaRepo;
    }

    @Override
    public List<Long> findEmpresaIdsByUsuario(Long usuarioId) {
        return ueRepo.findByUsuario_IdAndActivoTrue(usuarioId)
                .stream()
                .map(e -> e.getEmpresa().getId())
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByUsuarioAndEmpresa(Long usuarioId, Long empresaId) {
        return ueRepo.existsByUsuario_IdAndEmpresa_Id(usuarioId, empresaId);
    }

    @Override
    public UsuarioEmpresa save(UsuarioEmpresa ue) {
        UsuarioEntity usuario = ue.getUsuarioId() != null ? usuarioRepo.findById(ue.getUsuarioId()).orElse(null) : null;
        EmpresaEntity empresa = ue.getEmpresaId() != null ? empresaRepo.findById(ue.getEmpresaId()).orElse(null) : null;
        UsuarioEmpresaEntity entity = UsuarioEmpresaMapper.toEntity(ue, usuario, empresa);
        UsuarioEmpresaEntity saved = ueRepo.save(entity);
        return UsuarioEmpresaMapper.toDomain(saved);
    }

    @Override
    public void deleteByUsuarioAndEmpresa(Long usuarioId, Long empresaId) {
        ueRepo.findByUsuario_IdAndEmpresa_Id(usuarioId, empresaId).ifPresent(ueRepo::delete);
    }

    @Override
    public Optional<UsuarioEmpresa> findByUsuarioAndEmpresa(Long usuarioId, Long empresaId) {
        return ueRepo.findByUsuario_IdAndEmpresa_Id(usuarioId, empresaId).map(UsuarioEmpresaMapper::toDomain);
    }
}
