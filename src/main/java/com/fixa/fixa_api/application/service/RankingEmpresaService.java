package com.fixa.fixa_api.application.service;

import com.fixa.fixa_api.domain.model.Empresa;
import com.fixa.fixa_api.domain.model.ValoracionResumen;
import com.fixa.fixa_api.domain.repository.EmpresaRepositoryPort;
import com.fixa.fixa_api.domain.repository.ValoracionRepositoryPort;
import lombok.Data;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RankingEmpresaService {

    private final EmpresaRepositoryPort empresaPort;
    private final ValoracionRepositoryPort valoracionPort;

    public RankingEmpresaService(EmpresaRepositoryPort empresaPort,
                                 ValoracionRepositoryPort valoracionPort) {
        this.empresaPort = empresaPort;
        this.valoracionPort = valoracionPort;
    }

    @Data
    public static class EmpresaDestacada {
        private Long id;
        private String nombre;
        private String slug;
        private String descripcion;
        private String telefono;
        private String email;
        private Long categoriaId;
        private double promedioValoracion;
        private long totalValoraciones;
        private double score;
    }

    public List<EmpresaDestacada> listarEmpresasDestacadas(Long categoriaId, Integer limit) {
        int limiteSeguro = (limit == null || limit <= 0) ? 10 : Math.min(limit, 50);

        List<Empresa> visibles = empresaPort.findVisibles();

        List<EmpresaDestacada> ranking = visibles.stream()
                .filter(Empresa::isActivo)
                .filter(Empresa::isVisibilidadPublica)
                .filter(e -> categoriaId == null || (e.getCategoriaId() != null && e.getCategoriaId().equals(categoriaId)))
                .map(empresa -> {
                    Optional<ValoracionResumen> resumenOpt = valoracionPort.obtenerResumenPorEmpresa(empresa.getId());
                    double promedio = resumenOpt.map(ValoracionResumen::getPromedio).orElse(0.0);
                    long total = resumenOpt.map(ValoracionResumen::getTotalValoraciones).orElse(0L);

                    if (total <= 0) {
                        // Empresas sin valoraciones quedan con score 0 (se pueden filtrar después si se desea)
                    }

                    EmpresaDestacada dto = new EmpresaDestacada();
                    dto.setId(empresa.getId());
                    dto.setNombre(empresa.getNombre());
                    dto.setSlug(empresa.getSlug());
                    dto.setDescripcion(empresa.getDescripcion());
                    dto.setTelefono(empresa.getTelefono());
                    dto.setEmail(empresa.getEmail());
                    dto.setCategoriaId(empresa.getCategoriaId());
                    dto.setPromedioValoracion(promedio);
                    dto.setTotalValoraciones(total);
                    dto.setScore(calcularScoreEmpresa(promedio, total));
                    return dto;
                })
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(EmpresaDestacada::getScore).reversed())
                .limit(limiteSeguro)
                .collect(Collectors.toList());

        return ranking;
    }

    private double calcularScoreEmpresa(double promedio, long totalValoraciones) {
        if (totalValoraciones <= 0) return 0.0;
        double factorCantidad = Math.log10(totalValoraciones + 1); // prioriza empresas con más valoraciones
        return promedio * factorCantidad;
    }
}
