package com.fixa.fixa_api.application.service;

import com.fixa.fixa_api.domain.model.Categoria;
import com.fixa.fixa_api.domain.model.Empresa;
import com.fixa.fixa_api.domain.model.Servicio;
import com.fixa.fixa_api.domain.repository.CategoriaRepositoryPort;
import com.fixa.fixa_api.domain.repository.EmpresaRepositoryPort;
import com.fixa.fixa_api.domain.repository.ServicioRepositoryPort;
import com.fixa.fixa_api.infrastructure.in.web.dto.ServicioCercanoResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ServiciosCercanosService {

    private final EmpresaRepositoryPort empresaPort;
    private final ServicioRepositoryPort servicioPort;
    private final CategoriaRepositoryPort categoriaPort;

    public ServiciosCercanosService(EmpresaRepositoryPort empresaPort, ServicioRepositoryPort servicioPort,
            CategoriaRepositoryPort categoriaPort) {
        this.empresaPort = empresaPort;
        this.servicioPort = servicioPort;
        this.categoriaPort = categoriaPort;
    }

    public List<ServicioCercanoResponse> obtenerServiciosCercanos(double lat, double lon, double radioKm, Integer limit,
            Long categoriaId) {
        // 1. Obtener empresas cercanas
        List<Empresa> empresasCercanas = empresaPort.findCercanas(lat, lon, radioKm);

        if (empresasCercanas.isEmpty()) {
            return new ArrayList<>();
        }

        List<ServicioCercanoResponse> resultados = new ArrayList<>();

        // 2. Para cada empresa, obtener servicios activos y generar DTOs
        for (Empresa empresa : empresasCercanas) {
            List<Servicio> servicios = servicioPort.findByEmpresaId(empresa.getId());

            for (Servicio servicio : servicios) {
                if (!servicio.isActivo())
                    continue;

                // Filtro opcional por cateogría
                if (categoriaId != null && !categoriaId.equals(servicio.getCategoriaId())) {
                    continue;
                }

                ServicioCercanoResponse dto = new ServicioCercanoResponse();
                dto.setId(servicio.getId());
                dto.setEmpresaId(empresa.getId());
                dto.setEmpresaNombre(empresa.getNombre());
                dto.setNombre(servicio.getNombre());
                dto.setDescripcion(servicio.getDescripcion());
                dto.setDuracionMinutos(servicio.getDuracionMinutos());
                dto.setPrecio(servicio.getCosto());
                dto.setDistanciaKm(Math.round(empresa.getDistancia() * 100.0) / 100.0); // Redondear a 2 decimales

                // Foto URL con fallback a categoría
                String fotoUrl = servicio.getFotoUrl();
                if ((fotoUrl == null || fotoUrl.trim().isEmpty()) && servicio.getCategoriaId() != null) {
                    Categoria cat = categoriaPort.findById(servicio.getCategoriaId()).orElse(null);
                    if (cat != null) {
                        fotoUrl = cat.getFotoDefault();
                    }
                }
                dto.setFotoUrl(fotoUrl);

                resultados.add(dto);
            }
        }

        // 3. Ordenar por distancia (primario) y precio/nombre (secundario si se desea,
        // por ahora distancia)
        resultados.sort(Comparator.comparingDouble(ServicioCercanoResponse::getDistanciaKm));

        // 4. Limitar
        if (limit != null && limit > 0 && limit < resultados.size()) {
            return resultados.subList(0, limit);
        }

        return resultados;
    }
}
