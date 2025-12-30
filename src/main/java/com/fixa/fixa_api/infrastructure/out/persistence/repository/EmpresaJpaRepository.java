package com.fixa.fixa_api.infrastructure.out.persistence.repository;

import com.fixa.fixa_api.infrastructure.out.persistence.entity.EmpresaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmpresaJpaRepository extends JpaRepository<EmpresaEntity, Long> {
    List<EmpresaEntity> findByVisibilidadPublicaTrue();

    Optional<EmpresaEntity> findBySlug(String slug);

    Optional<EmpresaEntity> findByUsuarioAdminId(Long usuarioId);

    @org.springframework.data.jpa.repository.Query(value = "SELECT e.*, e.fk_categoria as categoriaId, (6371 * acos(cos(radians(:usuarioLat)) * cos(radians(e.latitud)) * "
            +
            "cos(radians(e.longitud) - radians(:usuarioLon)) + sin(radians(:usuarioLat)) * " +
            "sin(radians(e.latitud)))) AS distancia " +
            "FROM empresa e " +
            "WHERE e.latitud IS NOT NULL AND e.longitud IS NOT NULL AND e.activo = true AND e.visibilidad_publica = true "
            +
            "HAVING distancia < :radioKm " +
            "ORDER BY distancia ASC", nativeQuery = true)
    List<com.fixa.fixa_api.infrastructure.out.persistence.projection.EmpresaCercanaProjection> findCercanas(
            @org.springframework.data.repository.query.Param("usuarioLat") double usuarioLat,
            @org.springframework.data.repository.query.Param("usuarioLon") double usuarioLon,
            @org.springframework.data.repository.query.Param("radioKm") double radioKm);
}
