package com.fixa.fixa_api.infrastructure.in.web;

import com.fixa.fixa_api.application.service.ServiciosCercanosService;
import com.fixa.fixa_api.infrastructure.in.web.dto.ServicioCercanoResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/public/location")
public class PublicLocationController {

    private final ServiciosCercanosService serviciosCercanosService;

    public PublicLocationController(ServiciosCercanosService serviciosCercanosService) {
        this.serviciosCercanosService = serviciosCercanosService;
    }

    @GetMapping("/servicios-cercanos")
    public ResponseEntity<List<ServicioCercanoResponse>> getServiciosCercanos(
            @RequestParam double lat,
            @RequestParam double lon,
            @RequestParam(defaultValue = "10.0") double radioKm,
            @RequestParam(defaultValue = "10") Integer limit,
            @RequestParam(required = false) Long categoriaId) {

        List<ServicioCercanoResponse> servicios = serviciosCercanosService.obtenerServiciosCercanos(
                lat, lon, radioKm, limit, categoriaId);

        return ResponseEntity.ok(servicios);
    }
}
