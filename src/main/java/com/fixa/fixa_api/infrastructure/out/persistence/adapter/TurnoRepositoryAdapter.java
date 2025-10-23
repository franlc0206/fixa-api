package com.fixa.fixa_api.infrastructure.out.persistence.adapter;

import com.fixa.fixa_api.domain.model.Turno;
import com.fixa.fixa_api.domain.repository.TurnoRepositoryPort;
import com.fixa.fixa_api.infrastructure.out.persistence.entity.*;
import com.fixa.fixa_api.infrastructure.out.persistence.mapper.TurnoMapper;
import com.fixa.fixa_api.infrastructure.out.persistence.repository.*;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class TurnoRepositoryAdapter implements TurnoRepositoryPort {

    private final TurnoJpaRepository turnoRepo;
    private final ServicioJpaRepository servicioRepo;
    private final EmpleadoJpaRepository empleadoRepo;
    private final EmpresaJpaRepository empresaRepo;
    private final UsuarioJpaRepository usuarioRepo;

    public TurnoRepositoryAdapter(TurnoJpaRepository turnoRepo,
                                  ServicioJpaRepository servicioRepo,
                                  EmpleadoJpaRepository empleadoRepo,
                                  EmpresaJpaRepository empresaRepo,
                                  UsuarioJpaRepository usuarioRepo) {
        this.turnoRepo = turnoRepo;
        this.servicioRepo = servicioRepo;
        this.empleadoRepo = empleadoRepo;
        this.empresaRepo = empresaRepo;
        this.usuarioRepo = usuarioRepo;
    }

    @Override
    public Turno save(Turno turno) {
        TurnoEntity entity = turno.getId() != null ? turnoRepo.findById(turno.getId()).orElse(new TurnoEntity()) : new TurnoEntity();
        ServicioEntity servicio = turno.getServicioId() != null ? servicioRepo.findById(turno.getServicioId()).orElse(null) : null;
        EmpleadoEntity empleado = turno.getEmpleadoId() != null ? empleadoRepo.findById(turno.getEmpleadoId()).orElse(null) : null;
        EmpresaEntity empresa = turno.getEmpresaId() != null ? empresaRepo.findById(turno.getEmpresaId()).orElse(null) : null;
        UsuarioEntity cliente = turno.getClienteId() != null ? usuarioRepo.findById(turno.getClienteId()).orElse(null) : null;
        TurnoMapper.copyToEntity(turno, entity, servicio, empleado, empresa, cliente);
        TurnoEntity saved = turnoRepo.save(entity);
        return TurnoMapper.toDomain(saved);
    }

    @Override
    public Optional<Turno> findById(Long id) {
        return turnoRepo.findById(id).map(TurnoMapper::toDomain);
    }

    @Override
    public List<Turno> findByEmpleadoIdAndRango(Long empleadoId, LocalDateTime desde, LocalDateTime hasta) {
        return turnoRepo.findByEmpleado_IdAndFechaHoraInicioBetween(empleadoId, desde, hasta)
                .stream().map(TurnoMapper::toDomain).collect(Collectors.toList());
    }
}
