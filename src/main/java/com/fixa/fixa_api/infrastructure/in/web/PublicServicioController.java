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
