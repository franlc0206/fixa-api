package com.fixa.fixa_api.infrastructure.out.persistence.adapter;

import com.fixa.fixa_api.domain.model.Categoria;
import com.fixa.fixa_api.domain.repository.CategoriaRepositoryPort;
import com.fixa.fixa_api.infrastructure.out.persistence.entity.CategoriaEntity;
import com.fixa.fixa_api.infrastructure.out.persistence.mapper.CategoriaMapper;
import com.fixa.fixa_api.infrastructure.out.persistence.repository.CategoriaJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class CategoriaRepositoryAdapter implements CategoriaRepositoryPort {

    private final CategoriaJpaRepository repo;

    public CategoriaRepositoryAdapter(CategoriaJpaRepository repo) {
        this.repo = repo;
    }

    @Override
    public List<Categoria> findAll() {
        return repo.findAll().stream().map(CategoriaMapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public Optional<Categoria> findById(Long id) {
        return repo.findById(id).map(CategoriaMapper::toDomain);
    }

    @Override
    public Categoria save(Categoria categoria) {
        CategoriaEntity e = CategoriaMapper.toEntity(categoria);
        CategoriaEntity saved = repo.save(e);
        return CategoriaMapper.toDomain(saved);
    }
}
