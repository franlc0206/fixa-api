package com.fixa.fixa_api.infrastructure.out.persistence.adapter;

import com.fixa.fixa_api.domain.model.Plan;
import com.fixa.fixa_api.domain.repository.PlanRepositoryPort;
import com.fixa.fixa_api.infrastructure.out.persistence.mapper.PlanMapper;
import com.fixa.fixa_api.infrastructure.out.persistence.repository.PlanJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class PlanRepositoryAdapter implements PlanRepositoryPort {

    private final PlanJpaRepository repo;

    public PlanRepositoryAdapter(PlanJpaRepository repo) {
        this.repo = repo;
    }

    @Override
    public List<Plan> findAll() {
        return repo.findAll().stream()
                .map(PlanMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Plan> findByActivo(boolean activo) {
        return repo.findByActivo(activo).stream()
                .map(PlanMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Plan> findById(Long id) {
        return repo.findById(id).map(PlanMapper::toDomain);
    }

    @Override
    public Plan save(Plan plan) {
        var entity = PlanMapper.toEntity(plan);
        var saved = repo.save(entity);
        return PlanMapper.toDomain(saved);
    }

    @Override
    public void deleteById(Long id) {
        repo.deleteById(id);
    }
}
