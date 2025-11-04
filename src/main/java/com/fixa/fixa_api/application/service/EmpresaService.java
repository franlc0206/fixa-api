package com.fixa.fixa_api.application.service;

import com.fixa.fixa_api.domain.model.Empresa;
import com.fixa.fixa_api.domain.repository.EmpresaRepositoryPort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EmpresaService {

    private final EmpresaRepositoryPort empresaPort;

    public EmpresaService(EmpresaRepositoryPort empresaPort) {
        this.empresaPort = empresaPort;
    }

    public List<Empresa> listar(Boolean visibles) {
        return Boolean.TRUE.equals(visibles) ? empresaPort.findVisibles() : empresaPort.findAll();
    }

    public List<Empresa> listarConFiltros(Boolean visibles, Boolean activo, Long categoriaId) {
        List<Empresa> base = listar(visibles);
        return base.stream()
                .filter(e -> activo == null || e.isActivo() == activo)
                .filter(e -> categoriaId == null || (e.getCategoriaId() != null && e.getCategoriaId().equals(categoriaId)))
                .collect(Collectors.toList());
    }

    public List<Empresa> listarConFiltrosPaginado(Boolean visibles, Boolean activo, Long categoriaId, Integer page, Integer size) {
        List<Empresa> filtrado = listarConFiltros(visibles, activo, categoriaId);
        if (page == null || size == null || page < 0 || size <= 0) return filtrado;
        int from = Math.min(page * size, filtrado.size());
        int to = Math.min(from + size, filtrado.size());
        return filtrado.subList(from, to);
    }

    public Optional<Empresa> obtener(Long id) {
        return empresaPort.findById(id);
    }

    public Optional<Empresa> obtenerPorSlug(String slug) {
        return empresaPort.findBySlug(slug);
    }

    public Empresa guardar(Empresa empresa) {
        // Generar slug automáticamente si no está presente
        if (empresa.getSlug() == null || empresa.getSlug().isBlank()) {
            String slug = generarSlug(empresa.getNombre());
            empresa.setSlug(slug);
        }
        return empresaPort.save(empresa);
    }

    public boolean activar(Long id, boolean activo) {
        Optional<Empresa> opt = empresaPort.findById(id);
        if (opt.isEmpty()) return false;
        Empresa e = opt.get();
        e.setActivo(activo);
        empresaPort.save(e);
        return true;
    }

    /**
     * Genera un slug a partir de un nombre.
     * Convierte a minúsculas, reemplaza espacios por guiones,
     * elimina caracteres especiales y acentos.
     */
    private String generarSlug(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            return "empresa-" + System.currentTimeMillis();
        }
        
        return nombre
                .toLowerCase()
                .trim()
                // Reemplazar acentos
                .replace("á", "a").replace("é", "e").replace("í", "i")
                .replace("ó", "o").replace("ú", "u").replace("ñ", "n")
                // Reemplazar espacios y caracteres especiales por guiones
                .replaceAll("[\\s]+", "-")
                .replaceAll("[^a-z0-9-]", "")
                // Eliminar guiones duplicados
                .replaceAll("-+", "-")
                // Eliminar guiones al inicio y final
                .replaceAll("^-|-$", "");
    }
}
