package com.fixa.fixa_api.application.service;

import com.fixa.fixa_api.domain.model.Empresa;
import com.fixa.fixa_api.domain.model.Servicio;
import com.fixa.fixa_api.domain.model.Turno;
import com.fixa.fixa_api.domain.model.Valoracion;
import com.fixa.fixa_api.domain.repository.EmpresaRepositoryPort;
import com.fixa.fixa_api.domain.repository.ServicioRepositoryPort;
import com.fixa.fixa_api.domain.repository.TurnoRepositoryPort;
import com.fixa.fixa_api.domain.repository.ValoracionRepositoryPort;
import lombok.Data;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecomendacionService {

    private final ServicioRepositoryPort servicioPort;
    private final EmpresaRepositoryPort empresaPort;
    private final ValoracionRepositoryPort valoracionPort;
    private final TurnoRepositoryPort turnoPort;

    public RecomendacionService(ServicioRepositoryPort servicioPort,
                                EmpresaRepositoryPort empresaPort,
                                ValoracionRepositoryPort valoracionPort,
                                TurnoRepositoryPort turnoPort) {
        this.servicioPort = servicioPort;
        this.empresaPort = empresaPort;
        this.valoracionPort = valoracionPort;
        this.turnoPort = turnoPort;
    }

    @Data
    public static class ServicioRecomendado {
        private Long id;
        private Long empresaId;
        private String empresaNombre;
        private String nombre;
        private String descripcion;
        private Integer duracionMinutos;
        private BigDecimal precio;
        private double promedioValoracion;
        private long totalValoraciones;
        private double score;
    }

    public List<ServicioRecomendado> obtenerServiciosRecomendados(Long categoriaId, Integer limit) {
        int limiteSeguro = (limit == null || limit <= 0) ? 10 : Math.min(limit, 50);

        // 1) Tomar todos los servicios activos (y opcionalmente filtrados por categoría)
        List<Servicio> serviciosActivos = servicioPort.findAll().stream()
                .filter(Servicio::isActivo)
                .filter(s -> categoriaId == null || (s.getCategoriaId() != null && s.getCategoriaId().equals(categoriaId)))
                .collect(Collectors.toList());

        if (serviciosActivos.isEmpty()) {
            return List.of();
        }

        Map<Long, Servicio> servicioPorId = serviciosActivos.stream()
                .collect(Collectors.toMap(Servicio::getId, s -> s));

        // 2) Obtener todas las valoraciones activas
        List<Valoracion> valoraciones = valoracionPort.findAllActivas();
        if (valoraciones.isEmpty()) {
            return List.of();
        }

        // 3) Mapear turnoId -> servicioId usando TurnoRepositoryPort (cache básico en memoria)
        Map<Long, Long> servicioIdPorTurnoId = new HashMap<>();

        class ServicioStats {
            long totalValoraciones = 0L;
            long sumaPuntuacion = 0L;
        }

        Map<Long, ServicioStats> statsPorServicio = new HashMap<>();

        for (Valoracion v : valoraciones) {
            if (v.getTurnoId() == null || v.getPuntuacion() == null) continue;

            Long turnoId = v.getTurnoId();
            Long servicioId = servicioIdPorTurnoId.get(turnoId);
            if (servicioId == null && !servicioIdPorTurnoId.containsKey(turnoId)) {
                Turno turno = turnoPort.findById(turnoId).orElse(null);
                if (turno != null) {
                    servicioId = turno.getServicioId();
                }
                servicioIdPorTurnoId.put(turnoId, servicioId);
            }

            if (servicioId == null) continue;

            Servicio servicio = servicioPorId.get(servicioId);
            if (servicio == null) {
                // El servicio no está activo o no cumple el filtro de categoría
                continue;
            }

            ServicioStats stats = statsPorServicio.computeIfAbsent(servicioId, id -> new ServicioStats());
            stats.totalValoraciones++;
            stats.sumaPuntuacion += v.getPuntuacion();
        }

        int minValoraciones = 3;

        // 4) Construir DTO de recomendados calculando promedio y score
        List<ServicioRecomendado> recomendados = statsPorServicio.entrySet().stream()
                .filter(e -> e.getValue().totalValoraciones >= minValoraciones)
                .map(e -> {
                    Long servicioId = e.getKey();
                    Servicio servicio = servicioPorId.get(servicioId);
                    if (servicio == null) return null;

                    ServicioStats stats = e.getValue();
                    double promedio = stats.totalValoraciones > 0
                            ? (double) stats.sumaPuntuacion / (double) stats.totalValoraciones
                            : 0.0;
                    double promedioRedondeado = Math.round(promedio * 10.0) / 10.0;

                    Empresa empresa = empresaPort.findById(servicio.getEmpresaId()).orElse(null);
                    String empresaNombre = empresa != null ? empresa.getNombre() : null;

                    ServicioRecomendado dto = new ServicioRecomendado();
                    dto.setId(servicio.getId());
                    dto.setEmpresaId(servicio.getEmpresaId());
                    dto.setEmpresaNombre(empresaNombre);
                    dto.setNombre(servicio.getNombre());
                    dto.setDescripcion(servicio.getDescripcion());
                    dto.setDuracionMinutos(servicio.getDuracionMinutos());
                    dto.setPrecio(servicio.getCosto());
                    dto.setPromedioValoracion(promedioRedondeado);
                    dto.setTotalValoraciones(stats.totalValoraciones);
                    dto.setScore(calcularScore(promedioRedondeado, stats.totalValoraciones));
                    return dto;
                })
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(ServicioRecomendado::getScore).reversed())
                .limit(limiteSeguro)
                .collect(Collectors.toList());

        return recomendados;
    }

    private double calcularScore(double promedio, long totalValoraciones) {
        if (totalValoraciones <= 0) return 0.0;
        double factorCantidad = Math.log10(totalValoraciones + 1); // penaliza servicios con muy pocas valoraciones
        return promedio * factorCantidad;
    }
}
