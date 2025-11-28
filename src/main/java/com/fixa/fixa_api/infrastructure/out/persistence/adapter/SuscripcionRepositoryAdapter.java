package com.fixa.fixa_api.infrastructure.out.persistence.adapter;

import com.fixa.fixa_api.domain.model.Suscripcion;
import com.fixa.fixa_api.domain.repository.SuscripcionRepositoryPort;
import com.fixa.fixa_api.infrastructure.out.persistence.mapper.SuscripcionMapper;
import com.fixa.fixa_api.infrastructure.out.persistence.repository.SuscripcionJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class SuscripcionRepositoryAdapter implements SuscripcionRepositoryPort {

    private final SuscripcionJpaRepository repo;

    public SuscripcionRepositoryAdapter(SuscripcionJpaRepository repo) {
        this.repo = repo;
    }

    @Override
    public List<Suscripcion> findAll() {
        return repo.findAll().stream()
                .map(SuscripcionMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Suscripcion> findById(Long id) {
        return repo.findById(id).map(SuscripcionMapper::toDomain);
    }

    @Override
    public Optional<Suscripcion> findActivaByEmpresaId(Long empresaId) {
        return repo.findActivaByEmpresaId(empresaId)
                .map(SuscripcionMapper::toDomain);
    }

    @Override
    public List<Suscripcion> findByEmpresaId(Long empresaId) {
        return repo.findByEmpresaId(empresaId).stream()
                .map(SuscripcionMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Suscripcion save(Suscripcion suscripcion) {
        var entity = SuscripcionMapper.toEntity(suscripcion);
        var saved = repo.save(entity);
        return SuscripcionMapper.toDomain(saved);
    }

    @Override
    public void deleteById(Long id) {
        repo.deleteById(id);
    }
}
