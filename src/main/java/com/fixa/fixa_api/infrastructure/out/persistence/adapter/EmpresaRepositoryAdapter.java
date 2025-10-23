package com.fixa.fixa_api.infrastructure.out.persistence.adapter;

import com.fixa.fixa_api.domain.model.Empresa;
import com.fixa.fixa_api.domain.repository.EmpresaRepositoryPort;
import com.fixa.fixa_api.infrastructure.out.persistence.entity.CategoriaEntity;
import com.fixa.fixa_api.infrastructure.out.persistence.entity.EmpresaEntity;
import com.fixa.fixa_api.infrastructure.out.persistence.mapper.EmpresaMapper;
import com.fixa.fixa_api.infrastructure.out.persistence.repository.CategoriaJpaRepository;
import com.fixa.fixa_api.infrastructure.out.persistence.repository.EmpresaJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class EmpresaRepositoryAdapter implements EmpresaRepositoryPort {

    private final EmpresaJpaRepository empresaRepo;
    private final CategoriaJpaRepository categoriaRepo;

    public EmpresaRepositoryAdapter(EmpresaJpaRepository empresaRepo, CategoriaJpaRepository categoriaRepo) {
        this.empresaRepo = empresaRepo;
        this.categoriaRepo = categoriaRepo;
    }

    @Override
    public Optional<Empresa> findById(Long id) {
        return empresaRepo.findById(id).map(EmpresaMapper::toDomain);
    }

    @Override
    public Empresa save(Empresa empresa) {
        EmpresaEntity entity = empresa.getId() != null ? empresaRepo.findById(empresa.getId()).orElse(new EmpresaEntity()) : new EmpresaEntity();
        CategoriaEntity cat = null;
        if (empresa.getCategoriaId() != null) {
            cat = categoriaRepo.findById(empresa.getCategoriaId()).orElse(null);
        }
        EmpresaMapper.copyToEntity(empresa, entity, cat);
        EmpresaEntity saved = empresaRepo.save(entity);
        return EmpresaMapper.toDomain(saved);
    }

    @Override
    public List<Empresa> findAll() {
        return empresaRepo.findAll().stream().map(EmpresaMapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<Empresa> findVisibles() {
        return empresaRepo.findByVisibilidadPublicaTrue().stream().map(EmpresaMapper::toDomain).collect(Collectors.toList());
    }
}
