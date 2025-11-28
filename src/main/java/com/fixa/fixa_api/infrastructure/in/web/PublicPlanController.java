package com.fixa.fixa_api.infrastructure.in.web;

import com.fixa.fixa_api.domain.model.Plan;
import com.fixa.fixa_api.domain.repository.PlanRepositoryPort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/public/planes")
public class PublicPlanController {

    private final PlanRepositoryPort planPort;

    public PublicPlanController(PlanRepositoryPort planPort) {
        this.planPort = planPort;
    }

    @GetMapping
    public ResponseEntity<List<Plan>> listarPlanesActivos() {
        // Listar todos y filtrar solo los activos
        List<Plan> planes = planPort.findAll().stream()
                .filter(Plan::isActivo)
                .collect(Collectors.toList());
        return ResponseEntity.ok(planes);
    }
}
