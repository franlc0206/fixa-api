package com.fixa.fixa_api.infrastructure.out.persistence.adapter;

import com.fixa.fixa_api.domain.model.Valoracion;
import com.fixa.fixa_api.domain.model.ValoracionResumen;
import com.fixa.fixa_api.domain.repository.ValoracionRepositoryPort;
import com.fixa.fixa_api.infrastructure.out.persistence.entity.*;
import com.fixa.fixa_api.infrastructure.out.persistence.mapper.ValoracionMapper;
import com.fixa.fixa_api.infrastructure.out.persistence.repository.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class ValoracionRepositoryAdapter implements ValoracionRepositoryPort {

    private final ValoracionJpaRepository valoracionRepo;
    private final EmpresaJpaRepository empresaRepo;
    private final UsuarioJpaRepository usuarioRepo;
    private final TurnoJpaRepository turnoRepo;

    public ValoracionRepositoryAdapter(
            ValoracionJpaRepository valoracionRepo,
            EmpresaJpaRepository empresaRepo,
            UsuarioJpaRepository usuarioRepo,
            TurnoJpaRepository turnoRepo) {
        this.valoracionRepo = valoracionRepo;
        this.empresaRepo = empresaRepo;
        this.usuarioRepo = usuarioRepo;
        this.turnoRepo = turnoRepo;
    }

    @Override
    public Valoracion save(Valoracion valoracion) {
        ValoracionEntity entity = valoracion.getId() != null 
            ? valoracionRepo.findById(valoracion.getId()).orElse(new ValoracionEntity()) 
            : new ValoracionEntity();
        
        EmpresaEntity empresa = valoracion.getEmpresaId() != null 
            ? empresaRepo.findById(valoracion.getEmpresaId()).orElse(null) 
            : null;
        UsuarioEntity usuario = valoracion.getUsuarioId() != null 
            ? usuarioRepo.findById(valoracion.getUsuarioId()).orElse(null) 
            : null;
        TurnoEntity turno = valoracion.getTurnoId() != null 
            ? turnoRepo.findById(valoracion.getTurnoId()).orElse(null) 
            : null;
        
        ValoracionMapper.copyToEntity(valoracion, entity, empresa, usuario, turno);
        ValoracionEntity saved = valoracionRepo.save(entity);
        return ValoracionMapper.toDomain(saved);
    }

    @Override
    public Optional<Valoracion> findById(Long id) {
        return valoracionRepo.findById(id).map(ValoracionMapper::toDomain);
    }

    @Override
    public Optional<Valoracion> findByTurnoId(Long turnoId) {
        return valoracionRepo.findByTurno_Id(turnoId).map(ValoracionMapper::toDomain);
    }

    @Override
    public List<Valoracion> findByEmpresaId(Long empresaId) {
        return valoracionRepo.findByEmpresa_IdAndActivoTrue(empresaId)
                .stream()
                .map(ValoracionMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Valoracion> findByUsuarioId(Long usuarioId) {
        return valoracionRepo.findByUsuario_Id(usuarioId)
                .stream()
                .map(ValoracionMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByTurnoId(Long turnoId) {
        return valoracionRepo.existsByTurno_Id(turnoId);
    }

    @Override
    public Optional<ValoracionResumen> obtenerResumenPorEmpresa(Long empresaId) {
        List<ValoracionEntity> entities = valoracionRepo.findByEmpresa_IdAndActivoTrue(empresaId);

        if (entities.isEmpty()) {
            return Optional.empty();
        }

        long total = entities.size();
        long totalConResena = entities.stream()
                .filter(e -> e.getResena() != null && !e.getResena().isBlank())
                .count();

        double promedio = entities.stream()
                .mapToInt(ValoracionEntity::getPuntuacion)
                .average()
                .orElse(0.0);

        double promedioRedondeado = Math.round(promedio * 10.0) / 10.0;

        return Optional.of(new ValoracionResumen(empresaId, promedioRedondeado, total, totalConResena));
    }

    @Override
    public List<Valoracion> findAllActivas() {
        return valoracionRepo.findAll().stream()
                .filter(ValoracionEntity::isActivo)
                .map(ValoracionMapper::toDomain)
                .collect(Collectors.toList());
    }
}
