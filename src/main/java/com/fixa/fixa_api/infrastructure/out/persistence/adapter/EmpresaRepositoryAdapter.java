package com.fixa.fixa_api.infrastructure.out.persistence.adapter;

import com.fixa.fixa_api.domain.model.Empresa;
import com.fixa.fixa_api.domain.repository.EmpresaRepositoryPort;
import com.fixa.fixa_api.infrastructure.out.persistence.entity.CategoriaEntity;
import com.fixa.fixa_api.infrastructure.out.persistence.entity.EmpresaEntity;
import com.fixa.fixa_api.infrastructure.out.persistence.entity.UsuarioEntity;
import com.fixa.fixa_api.infrastructure.out.persistence.mapper.EmpresaMapper;
import com.fixa.fixa_api.infrastructure.out.persistence.repository.CategoriaJpaRepository;
import com.fixa.fixa_api.infrastructure.out.persistence.repository.EmpresaJpaRepository;
import com.fixa.fixa_api.infrastructure.out.persistence.repository.PlanJpaRepository;
import com.fixa.fixa_api.infrastructure.out.persistence.repository.UsuarioJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class EmpresaRepositoryAdapter implements EmpresaRepositoryPort {

    private final EmpresaJpaRepository empresaRepo;
    private final CategoriaJpaRepository categoriaRepo;
    private final UsuarioJpaRepository usuarioRepo;
    private final PlanJpaRepository planRepo;

    public EmpresaRepositoryAdapter(EmpresaJpaRepository empresaRepo, CategoriaJpaRepository categoriaRepo,
            UsuarioJpaRepository usuarioRepo, PlanJpaRepository planRepo) {
        this.empresaRepo = empresaRepo;
        this.categoriaRepo = categoriaRepo;
        this.usuarioRepo = usuarioRepo;
        this.planRepo = planRepo;
    }

    @Override
    public Optional<Empresa> findById(Long id) {
        return empresaRepo.findById(id).map(EmpresaMapper::toDomain);
    }

    @Override
    public Optional<Empresa> findBySlug(String slug) {
        return empresaRepo.findBySlug(slug).map(EmpresaMapper::toDomain);
    }

    @Override
    public Optional<Empresa> findByUsuarioAdminId(Long id) {
        return empresaRepo.findByUsuarioAdminId(id).map(EmpresaMapper::toDomain);
    }

    @Override
    public Empresa save(Empresa empresa) {
        EmpresaEntity entity = empresa.getId() != null
                ? empresaRepo.findById(empresa.getId()).orElse(new EmpresaEntity())
                : new EmpresaEntity();

        CategoriaEntity cat = null;
        if (empresa.getCategoriaId() != null) {
            cat = categoriaRepo.findById(empresa.getCategoriaId()).orElse(null);
        }

        UsuarioEntity admin = null;
        if (empresa.getUsuarioAdminId() != null) {
            admin = usuarioRepo.findById(empresa.getUsuarioAdminId()).orElse(null);
        }

        com.fixa.fixa_api.infrastructure.out.persistence.entity.PlanEntity plan = null;
        if (empresa.getPlanActualId() != null) {
            plan = planRepo.findById(empresa.getPlanActualId()).orElse(null);
        }

        EmpresaMapper.copyToEntity(empresa, entity, cat, admin, plan);
        EmpresaEntity saved = empresaRepo.save(entity);
        return EmpresaMapper.toDomain(saved);
    }

    @Override
    public List<Empresa> findAll() {
        return empresaRepo.findAll().stream().map(EmpresaMapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<Empresa> findVisibles() {
        return empresaRepo.findByVisibilidadPublicaTrue().stream().map(EmpresaMapper::toDomain)
                .collect(Collectors.toList());
    }
}
