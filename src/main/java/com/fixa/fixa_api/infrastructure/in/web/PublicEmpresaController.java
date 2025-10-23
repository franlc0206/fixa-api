package com.fixa.fixa_api.infrastructure.in.web;

import com.fixa.fixa_api.application.service.EmpresaService;
import com.fixa.fixa_api.application.service.ServicioService;
import com.fixa.fixa_api.domain.model.Empresa;
import com.fixa.fixa_api.domain.model.Servicio;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/public/empresas")
public class PublicEmpresaController {

    private final EmpresaService empresaService;
    private final ServicioService servicioService;

    public PublicEmpresaController(EmpresaService empresaService, ServicioService servicioService) {
        this.empresaService = empresaService;
        this.servicioService = servicioService;
    }

    // Catálogo público: solo empresas visibles
    @GetMapping
    public ResponseEntity<List<Empresa>> listarEmpresasVisibles(
            @RequestParam(value = "categoriaId", required = false) Long categoriaId,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size
    ) {
        List<Empresa> result = empresaService.listarConFiltrosPaginado(true, null, categoriaId, page, size);
        return ResponseEntity.ok(result);
    }

    // Servicios públicos por empresa (por defecto solo activos)
    @GetMapping("/{empresaId}/servicios")
    public ResponseEntity<List<Servicio>> listarServiciosPublicos(@PathVariable Long empresaId,
                                                                  @RequestParam(value = "soloActivos", required = false, defaultValue = "true") boolean soloActivos,
                                                                  @RequestParam(value = "page", required = false) Integer page,
                                                                  @RequestParam(value = "size", required = false) Integer size) {
        List<Servicio> result = (page != null && size != null)
                ? servicioService.listarPorEmpresaPaginado(empresaId, soloActivos, page, size)
                : (soloActivos ? servicioService.listarPorEmpresa(empresaId, true) : servicioService.listarPorEmpresa(empresaId));
        return ResponseEntity.ok(result);
    }
}
