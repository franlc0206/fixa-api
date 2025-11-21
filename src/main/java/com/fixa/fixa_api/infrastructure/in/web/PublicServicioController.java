package com.fixa.fixa_api.infrastructure.in.web;

import com.fixa.fixa_api.application.service.RecomendacionService;
import com.fixa.fixa_api.infrastructure.in.web.dto.ServicioRecomendadoResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/public/servicios")
public class PublicServicioController {

    private final RecomendacionService recomendacionService;

    public PublicServicioController(RecomendacionService recomendacionService) {
        this.recomendacionService = recomendacionService;
    }

    @GetMapping
    public ResponseEntity<List<ServicioRecomendadoResponse>> listarServiciosHome(
            @RequestParam(value = "categoriaId", required = false) Long categoriaId,
            @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "20") Integer size
    ) {
        if (page == null || page < 0) {
            page = 0;
        }
        if (size == null || size <= 0) {
            size = 20;
        }
        if (size > 50) {
            size = 50;
        }

        List<RecomendacionService.ServicioRecomendado> ranking =
                recomendacionService.obtenerRankingServicios(categoriaId);

        if (ranking.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }

        int from = Math.max(0, page * size);
        if (from >= ranking.size()) {
            return ResponseEntity.ok(List.of());
        }
        int to = Math.min(from + size, ranking.size());

        List<ServicioRecomendadoResponse> response = ranking.subList(from, to).stream()
                .map(ServicioRecomendadoResponse::fromDomain)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/recomendados")
    public ResponseEntity<List<ServicioRecomendadoResponse>> listarRecomendados(
            @RequestParam(value = "categoriaId", required = false) Long categoriaId,
            @RequestParam(value = "limit", required = false) Integer limit
    ) {
        List<RecomendacionService.ServicioRecomendado> recomendados =
                recomendacionService.obtenerServiciosRecomendados(categoriaId, limit);

        List<ServicioRecomendadoResponse> response = recomendados.stream()
                .map(ServicioRecomendadoResponse::fromDomain)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }
}
