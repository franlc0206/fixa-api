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
        List<ServicioRecomendado> ranking = construirRanking(categoriaId);
        if (ranking.isEmpty()) {
            return ranking;
        }

        int limiteSeguro = (limit == null || limit <= 0) ? 10 : Math.min(limit, 50);
        return ranking.stream()
                .limit(limiteSeguro)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene el ranking completo de servicios recomendados (sin límite),
     * ordenado por score descendente. Pensado para endpoints paginados.
     */
    public List<ServicioRecomendado> obtenerRankingServicios(Long categoriaId) {
        return construirRanking(categoriaId);
    }

    /**
     * Construye el ranking de servicios recomendados aplicando:
     * - Solo servicios activos
     * - Solo empresas visibles y activas
     * - Filtro opcional por categoría de servicio
     * - Score basado en valoraciones cuando existen, o 0 si no hay valoraciones
     */
    private List<ServicioRecomendado> construirRanking(Long categoriaId) {
        // 0) Empresas visibles y activas
        List<Empresa> empresasVisibles = empresaPort.findVisibles();
        if (empresasVisibles.isEmpty()) {
            return List.of();
        }

        Map<Long, Empresa> empresaPorId = empresasVisibles.stream()
                .filter(Empresa::isActivo)
                .collect(Collectors.toMap(Empresa::getId, e -> e));

        if (empresaPorId.isEmpty()) {
            return List.of();
        }

        // 1) Tomar todos los servicios activos de empresas visibles (y opcionalmente filtrados por categoría)
        List<Servicio> serviciosActivos = servicioPort.findAll().stream()
                .filter(Servicio::isActivo)
                .filter(s -> s.getEmpresaId() != null && empresaPorId.containsKey(s.getEmpresaId()))
                .filter(s -> categoriaId == null || (s.getCategoriaId() != null && s.getCategoriaId().equals(categoriaId)))
                .collect(Collectors.toList());

        if (serviciosActivos.isEmpty()) {
            return List.of();
        }

        Map<Long, Servicio> servicioPorId = serviciosActivos.stream()
                .collect(Collectors.toMap(Servicio::getId, s -> s));

        // 2) Obtener todas las valoraciones activas (si no hay, igual construimos el ranking con score 0)
        List<Valoracion> valoraciones = valoracionPort.findAllActivas();

        // 3) Mapear turnoId -> servicioId usando TurnoRepositoryPort (cache básico en memoria)
        Map<Long, Long> servicioIdPorTurnoId = new HashMap<>();

        class ServicioStats {
            long totalValoraciones = 0L;
            long sumaPuntuacion = 0L;
        }

        Map<Long, ServicioStats> statsPorServicio = new HashMap<>();

        if (valoraciones != null && !valoraciones.isEmpty()) {
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
                    // El servicio no está activo o no cumple el filtro de categoría / empresa visible
                    continue;
                }

                ServicioStats stats = statsPorServicio.computeIfAbsent(servicioId, id -> new ServicioStats());
                stats.totalValoraciones++;
                stats.sumaPuntuacion += v.getPuntuacion();
            }
        }

        // 4) Construir DTO de recomendados calculando promedio y score para TODOS los servicios activos
        return serviciosActivos.stream()
                .map(servicio -> {
                    ServicioStats stats = statsPorServicio.getOrDefault(servicio.getId(), new ServicioStats());
                    double promedio = stats.totalValoraciones > 0
                            ? (double) stats.sumaPuntuacion / (double) stats.totalValoraciones
                            : 0.0;
                    double promedioRedondeado = Math.round(promedio * 10.0) / 10.0;

                    Empresa empresa = empresaPorId.get(servicio.getEmpresaId());
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
                .collect(Collectors.toList());
    }

    private double calcularScore(double promedio, long totalValoraciones) {
        if (totalValoraciones <= 0) return 0.0;
        double factorCantidad = Math.log10(totalValoraciones + 1); // penaliza servicios con muy pocas valoraciones
        return promedio * factorCantidad;
    }
}
