package com.fixa.fixa_api.infrastructure.out.persistence.repository;

import com.fixa.fixa_api.infrastructure.out.persistence.entity.TurnoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface TurnoJpaRepository extends JpaRepository<TurnoEntity, Long> {
    List<TurnoEntity> findByEmpleado_IdAndFechaHoraInicioBetween(Long empleadoId, LocalDateTime desde, LocalDateTime hasta);
    List<TurnoEntity> findByEmpresa_IdAndFechaHoraInicioBetween(Long empresaId, LocalDateTime desde, LocalDateTime hasta);
    List<TurnoEntity> findByCliente_IdOrderByFechaHoraInicioDesc(Long clienteId);
}
