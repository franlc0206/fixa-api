package com.fixa.fixa_api.infrastructure.out.persistence.adapter;

import com.fixa.fixa_api.domain.model.VerificacionTelefono;
import com.fixa.fixa_api.domain.repository.VerificacionTelefonoRepositoryPort;
import com.fixa.fixa_api.infrastructure.out.persistence.entity.TurnoEntity;
import com.fixa.fixa_api.infrastructure.out.persistence.entity.VerificacionTelefonoEntity;
import com.fixa.fixa_api.infrastructure.out.persistence.mapper.VerificacionTelefonoMapper;
import com.fixa.fixa_api.infrastructure.out.persistence.repository.TurnoJpaRepository;
import com.fixa.fixa_api.infrastructure.out.persistence.repository.VerificacionTelefonoJpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Adapter que implementa el puerto VerificacionTelefonoRepositoryPort.
 * Siguiendo arquitectura hexagonal, este adapter:
 * - Implementa la interface del dominio (puerto)
 * - Usa Spring Data JPA para persistencia
 * - Convierte entre dominio y entidades usando mappers
 * - Pertenece a la capa de infraestructura
 */
@Component
public class VerificacionTelefonoRepositoryAdapter implements VerificacionTelefonoRepositoryPort {

    private final VerificacionTelefonoJpaRepository verificacionRepo;
    private final TurnoJpaRepository turnoRepo;

    public VerificacionTelefonoRepositoryAdapter(
            VerificacionTelefonoJpaRepository verificacionRepo,
            TurnoJpaRepository turnoRepo) {
        this.verificacionRepo = verificacionRepo;
        this.turnoRepo = turnoRepo;
    }

    @Override
    public VerificacionTelefono save(VerificacionTelefono verificacion) {
        VerificacionTelefonoEntity entity = verificacion.getId() != null 
                ? verificacionRepo.findById(verificacion.getId()).orElse(new VerificacionTelefonoEntity())
                : new VerificacionTelefonoEntity();

        TurnoEntity turno = null;
        if (verificacion.getTurnoId() != null) {
            turno = turnoRepo.findById(verificacion.getTurnoId()).orElse(null);
        }

        VerificacionTelefonoMapper.copyToEntity(verificacion, entity, turno);
        VerificacionTelefonoEntity saved = verificacionRepo.save(entity);
        return VerificacionTelefonoMapper.toDomain(saved);
    }

    @Override
    public Optional<VerificacionTelefono> findById(Long id) {
        return verificacionRepo.findById(id)
                .map(VerificacionTelefonoMapper::toDomain);
    }

    @Override
    public Optional<VerificacionTelefono> findByTelefonoAndValidadoFalse(String telefono) {
        return verificacionRepo.findFirstByTelefonoAndValidadoFalseOrderByFechaEnvioDesc(telefono)
                .map(VerificacionTelefonoMapper::toDomain);
    }
}
