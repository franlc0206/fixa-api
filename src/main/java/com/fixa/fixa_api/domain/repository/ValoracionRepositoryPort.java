package com.fixa.fixa_api.domain.repository;

import com.fixa.fixa_api.domain.model.Valoracion;
import com.fixa.fixa_api.domain.model.ValoracionResumen;
import java.util.List;
import java.util.Optional;

public interface ValoracionRepositoryPort {
    Valoracion save(Valoracion valoracion);
    Optional<Valoracion> findById(Long id);
    Optional<Valoracion> findByTurnoId(Long turnoId);
    List<Valoracion> findByEmpresaId(Long empresaId);
    List<Valoracion> findByUsuarioId(Long usuarioId);
    List<Valoracion> findAllActivas();
    boolean existsByTurnoId(Long turnoId);
    Optional<ValoracionResumen> obtenerResumenPorEmpresa(Long empresaId);
}
