package com.fixa.fixa_api.infrastructure.out.persistence.repository;

import com.fixa.fixa_api.infrastructure.out.persistence.entity.VerificacionTelefonoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VerificacionTelefonoJpaRepository extends JpaRepository<VerificacionTelefonoEntity, Long> {
    Optional<VerificacionTelefonoEntity> findTopByTelefonoOrderByFechaEnvioDesc(String telefono);
    Optional<VerificacionTelefonoEntity> findFirstByTelefonoAndValidadoFalseOrderByFechaEnvioDesc(String telefono);
}
