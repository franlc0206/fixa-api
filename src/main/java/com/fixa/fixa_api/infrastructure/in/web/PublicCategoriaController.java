package com.fixa.fixa_api.infrastructure.in.web;

import com.fixa.fixa_api.domain.model.Categoria;
import com.fixa.fixa_api.domain.repository.CategoriaRepositoryPort;
import com.fixa.fixa_api.infrastructure.in.web.dto.CategoriaPublicResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/public/categorias")
public class PublicCategoriaController {

    private final CategoriaRepositoryPort categoriaPort;

    public PublicCategoriaController(CategoriaRepositoryPort categoriaPort) {
        this.categoriaPort = categoriaPort;
    }

    @GetMapping
    public ResponseEntity<List<CategoriaPublicResponse>> listar(
            @RequestParam(value = "tipo", required = false) String tipo
    ) {
        List<Categoria> all = categoriaPort.findAll();

        List<CategoriaPublicResponse> result = all.stream()
                .filter(Categoria::isActivo)
                .filter(c -> {
                    if (tipo == null || tipo.isBlank()) return true;
                    String t = tipo.toLowerCase(Locale.ROOT);
                    return c.getTipo() != null && c.getTipo().toLowerCase(Locale.ROOT).equals(t);
                })
                .map(CategoriaPublicResponse::fromDomain)
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }
}
