package com.fixa.fixa_api.infrastructure.out.persistence.adapter;

import com.fixa.fixa_api.domain.model.Servicio;
import com.fixa.fixa_api.domain.repository.ServicioRepositoryPort;
import com.fixa.fixa_api.infrastructure.out.persistence.entity.CategoriaEntity;
import com.fixa.fixa_api.infrastructure.out.persistence.entity.EmpresaEntity;
import com.fixa.fixa_api.infrastructure.out.persistence.entity.ServicioEntity;
import com.fixa.fixa_api.infrastructure.out.persistence.mapper.ServicioMapper;
import com.fixa.fixa_api.infrastructure.out.persistence.repository.CategoriaJpaRepository;
import com.fixa.fixa_api.infrastructure.out.persistence.repository.EmpresaJpaRepository;
import com.fixa.fixa_api.infrastructure.out.persistence.repository.ServicioJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class ServicioRepositoryAdapter implements ServicioRepositoryPort {

    private final ServicioJpaRepository servicioRepo;
    private final EmpresaJpaRepository empresaRepo;
    private final CategoriaJpaRepository categoriaRepo;

    public ServicioRepositoryAdapter(ServicioJpaRepository servicioRepo, EmpresaJpaRepository empresaRepo, CategoriaJpaRepository categoriaRepo) {
        this.servicioRepo = servicioRepo;
        this.empresaRepo = empresaRepo;
        this.categoriaRepo = categoriaRepo;
    }

    @Override
    public List<Servicio> findByEmpresaId(Long empresaId) {
        return servicioRepo.findAll().stream()
                .filter(s -> s.getEmpresa() != null && s.getEmpresa().getId().equals(empresaId))
                .map(ServicioMapper::toDomain)
                .collect(Collectors.toList());
    }

     @Override
    public List<Servicio> findAll() {
        return servicioRepo.findAll().stream()
                .map(ServicioMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Servicio> findById(Long id) {
        return servicioRepo.findById(id).map(ServicioMapper::toDomain);
    }

    @Override
    public Servicio save(Servicio servicio) {
        ServicioEntity entity = servicio.getId() != null ? servicioRepo.findById(servicio.getId()).orElse(new ServicioEntity()) : new ServicioEntity();
        EmpresaEntity empresa = null;
        if (servicio.getEmpresaId() != null) {
            empresa = empresaRepo.findById(servicio.getEmpresaId()).orElse(null);
        }
        CategoriaEntity categoria = null;
        if (servicio.getCategoriaId() != null) {
            categoria = categoriaRepo.findById(servicio.getCategoriaId()).orElse(null);
        }
        ServicioMapper.copyToEntity(servicio, entity, empresa, categoria);
        ServicioEntity saved = servicioRepo.save(entity);
        return ServicioMapper.toDomain(saved);
    }

    @Override
    public void deleteById(Long id) {
        servicioRepo.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return servicioRepo.existsById(id);
    }
}
