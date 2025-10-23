package com.fixa.fixa_api.infrastructure.out.persistence.adapter;

import com.fixa.fixa_api.domain.model.Empleado;
import com.fixa.fixa_api.domain.repository.EmpleadoRepositoryPort;
import com.fixa.fixa_api.infrastructure.out.persistence.entity.EmpleadoEntity;
import com.fixa.fixa_api.infrastructure.out.persistence.entity.EmpresaEntity;
import com.fixa.fixa_api.infrastructure.out.persistence.entity.UsuarioEntity;
import com.fixa.fixa_api.infrastructure.out.persistence.mapper.EmpleadoMapper;
import com.fixa.fixa_api.infrastructure.out.persistence.repository.EmpleadoJpaRepository;
import com.fixa.fixa_api.infrastructure.out.persistence.repository.EmpresaJpaRepository;
import com.fixa.fixa_api.infrastructure.out.persistence.repository.UsuarioJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class EmpleadoRepositoryAdapter implements EmpleadoRepositoryPort {

    private final EmpleadoJpaRepository empleadoRepo;
    private final EmpresaJpaRepository empresaRepo;
    private final UsuarioJpaRepository usuarioRepo;

    public EmpleadoRepositoryAdapter(EmpleadoJpaRepository empleadoRepo, EmpresaJpaRepository empresaRepo, UsuarioJpaRepository usuarioRepo) {
        this.empleadoRepo = empleadoRepo;
        this.empresaRepo = empresaRepo;
        this.usuarioRepo = usuarioRepo;
    }

    @Override
    public List<Empleado> findByEmpresaId(Long empresaId) {
        return empleadoRepo.findByEmpresa_Id(empresaId).stream().map(EmpleadoMapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public Optional<Empleado> findById(Long id) {
        return empleadoRepo.findById(id).map(EmpleadoMapper::toDomain);
    }

    @Override
    public Empleado save(Empleado empleado) {
        EmpleadoEntity entity = empleado.getId() != null ? empleadoRepo.findById(empleado.getId()).orElse(new EmpleadoEntity()) : new EmpleadoEntity();
        EmpresaEntity empresa = null;
        if (empleado.getEmpresaId() != null) {
            empresa = empresaRepo.findById(empleado.getEmpresaId()).orElse(null);
        }
        UsuarioEntity usuario = null;
        if (empleado.getUsuarioId() != null) {
            usuario = usuarioRepo.findById(empleado.getUsuarioId()).orElse(null);
        }
        EmpleadoMapper.copyToEntity(empleado, entity, empresa, usuario);
        EmpleadoEntity saved = empleadoRepo.save(entity);
        return EmpleadoMapper.toDomain(saved);
    }

    @Override
    public void deleteById(Long id) {
        empleadoRepo.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return empleadoRepo.existsById(id);
    }
}
