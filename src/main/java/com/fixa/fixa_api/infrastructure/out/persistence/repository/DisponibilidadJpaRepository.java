package com.fixa.fixa_api.infrastructure.out.persistence.repository;

import com.fixa.fixa_api.infrastructure.out.persistence.entity.DisponibilidadEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DisponibilidadJpaRepository extends JpaRepository<DisponibilidadEntity, Long> {
    List<DisponibilidadEntity> findByEmpleado_IdAndDiaSemana(Long empleadoId, String diaSemana);
}
