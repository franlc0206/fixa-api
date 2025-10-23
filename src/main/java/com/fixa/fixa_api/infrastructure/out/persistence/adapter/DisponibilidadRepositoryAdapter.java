package com.fixa.fixa_api.infrastructure.out.persistence.adapter;

import com.fixa.fixa_api.domain.model.Disponibilidad;
import com.fixa.fixa_api.domain.repository.DisponibilidadRepositoryPort;
import com.fixa.fixa_api.infrastructure.out.persistence.entity.DisponibilidadEntity;
import com.fixa.fixa_api.infrastructure.out.persistence.entity.EmpleadoEntity;
import com.fixa.fixa_api.infrastructure.out.persistence.mapper.DisponibilidadMapper;
import com.fixa.fixa_api.infrastructure.out.persistence.repository.DisponibilidadJpaRepository;
import com.fixa.fixa_api.infrastructure.out.persistence.repository.EmpleadoJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class DisponibilidadRepositoryAdapter implements DisponibilidadRepositoryPort {

    private final DisponibilidadJpaRepository disponibilidadRepo;
    private final EmpleadoJpaRepository empleadoRepo;

    public DisponibilidadRepositoryAdapter(DisponibilidadJpaRepository disponibilidadRepo, EmpleadoJpaRepository empleadoRepo) {
        this.disponibilidadRepo = disponibilidadRepo;
        this.empleadoRepo = empleadoRepo;
    }

    @Override
    public List<Disponibilidad> findByEmpleadoId(Long empleadoId) {
        return disponibilidadRepo.findAll().stream()
                .filter(d -> d.getEmpleado() != null && d.getEmpleado().getId().equals(empleadoId))
                .map(DisponibilidadMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Disponibilidad> findById(Long id) {
        return disponibilidadRepo.findById(id).map(DisponibilidadMapper::toDomain);
    }

    @Override
    public Disponibilidad save(Disponibilidad disponibilidad) {
        DisponibilidadEntity entity = disponibilidad.getId() != null ? disponibilidadRepo.findById(disponibilidad.getId()).orElse(new DisponibilidadEntity()) : new DisponibilidadEntity();
        EmpleadoEntity empleado = null;
        if (disponibilidad.getEmpleadoId() != null) {
            empleado = empleadoRepo.findById(disponibilidad.getEmpleadoId()).orElse(null);
        }
        DisponibilidadMapper.copyToEntity(disponibilidad, entity, empleado);
        DisponibilidadEntity saved = disponibilidadRepo.save(entity);
        return DisponibilidadMapper.toDomain(saved);
    }

    @Override
    public void deleteById(Long id) {
        disponibilidadRepo.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return disponibilidadRepo.existsById(id);
    }
}
