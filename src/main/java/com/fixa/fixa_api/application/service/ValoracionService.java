package com.fixa.fixa_api.application.service;

import com.fixa.fixa_api.domain.model.Turno;
import com.fixa.fixa_api.domain.model.Valoracion;
import com.fixa.fixa_api.domain.model.ValoracionResumen;
import com.fixa.fixa_api.domain.repository.TurnoRepositoryPort;
import com.fixa.fixa_api.domain.repository.ValoracionRepositoryPort;
import com.fixa.fixa_api.infrastructure.in.web.error.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ValoracionService {

    private final ValoracionRepositoryPort valoracionPort;
    private final TurnoRepositoryPort turnoPort;

    public ValoracionService(ValoracionRepositoryPort valoracionPort, TurnoRepositoryPort turnoPort) {
        this.valoracionPort = valoracionPort;
        this.turnoPort = turnoPort;
    }

    @Transactional
    public Valoracion crearValoracion(Valoracion valoracion) {
        // Validar puntuación
        if (valoracion.getPuntuacion() == null || valoracion.getPuntuacion() < 1 || valoracion.getPuntuacion() > 5) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "La puntuación debe estar entre 1 y 5 estrellas");
        }

        // Validar que el turno existe
        Turno turno = turnoPort.findById(valoracion.getTurnoId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Turno no encontrado"));

        // Validar que el turno está completado
        if (!"COMPLETADO".equalsIgnoreCase(turno.getEstado())
                && !"REALIZADO".equalsIgnoreCase(turno.getEstado())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Solo se pueden valorar turnos completados o realizados");
        }

        // Validar que el usuario es el cliente del turno
        if (turno.getClienteId() == null || !turno.getClienteId().equals(valoracion.getUsuarioId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Solo el cliente del turno puede valorarlo");
        }

        // Validar empresa si viene en la request
        if (valoracion.getEmpresaId() != null && !valoracion.getEmpresaId().equals(turno.getEmpresaId())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "La empresa indicada no coincide con el turno");
        }

        // Validar que no existe ya una valoración para este turno
        if (valoracionPort.existsByTurnoId(valoracion.getTurnoId())) {
            throw new ApiException(HttpStatus.CONFLICT, "Ya existe una valoración para este turno");
        }

        // Establecer empresa del turno
        valoracion.setEmpresaId(turno.getEmpresaId());
        
        // Establecer fecha de creación
        if (valoracion.getFechaCreacion() == null) {
            valoracion.setFechaCreacion(LocalDateTime.now());
        }
        
        // Establecer como activo por defecto
        valoracion.setActivo(true);

        return valoracionPort.save(valoracion);
    }

    @Transactional(readOnly = true)
    public List<Valoracion> obtenerValoracionesPorEmpresa(Long empresaId) {
        return valoracionPort.findByEmpresaId(empresaId);
    }

    @Transactional(readOnly = true)
    public List<Valoracion> obtenerValoracionesPorUsuario(Long usuarioId) {
        return valoracionPort.findByUsuarioId(usuarioId);
    }

    @Transactional(readOnly = true)
    public Valoracion obtenerValoracionPorId(Long id) {
        return valoracionPort.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Valoración no encontrada"));
    }

    @Transactional(readOnly = true)
    public Valoracion obtenerValoracionPorTurno(Long turnoId) {
        return valoracionPort.findByTurnoId(turnoId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "No existe valoración para este turno"));
    }

    @Transactional
    public void desactivarValoracion(Long id, Long usuarioId) {
        Valoracion valoracion = obtenerValoracionPorId(id);
        
        // Solo el usuario que creó la valoración puede desactivarla
        if (!valoracion.getUsuarioId().equals(usuarioId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "No tienes permiso para desactivar esta valoración");
        }
        
        valoracion.setActivo(false);
        valoracionPort.save(valoracion);
    }

    @Transactional(readOnly = true)
    public ValoracionResumen obtenerResumenValoracionesPorEmpresa(Long empresaId) {
        return valoracionPort.obtenerResumenPorEmpresa(empresaId)
                .orElseGet(() -> calcularResumenDesdeListado(empresaId));
    }

    @Transactional(readOnly = true)
    public List<Valoracion> obtenerComentariosPublicos(Long empresaId, boolean soloConResena, Integer limit) {
        List<Valoracion> valoraciones = valoracionPort.findByEmpresaId(empresaId);

        Comparator<Valoracion> porFechaDesc = Comparator.comparing(
                        Valoracion::getFechaCreacion,
                        Comparator.nullsLast(Comparator.naturalOrder()))
                .reversed();

        int limiteSeguro = (limit == null || limit <= 0) ? 20 : Math.min(limit, 100);

        return valoraciones.stream()
                .filter(v -> !soloConResena || (v.getResena() != null && !v.getResena().isBlank()))
                .sorted(porFechaDesc)
                .limit(limiteSeguro)
                .collect(Collectors.toList());
    }

    private ValoracionResumen calcularResumenDesdeListado(Long empresaId) {
        List<Valoracion> valoraciones = valoracionPort.findByEmpresaId(empresaId);

        long total = valoraciones.size();
        long totalConResena = valoraciones.stream()
                .filter(v -> v.getResena() != null && !v.getResena().isBlank())
                .count();

        double promedio = total > 0
                ? valoraciones.stream()
                .mapToInt(Valoracion::getPuntuacion)
                .average()
                .orElse(0.0)
                : 0.0;

        double promedioRedondeado = Math.round(promedio * 10.0) / 10.0;

        return new ValoracionResumen(empresaId, promedioRedondeado, total, totalConResena);
    }
}

