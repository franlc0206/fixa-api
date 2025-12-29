package com.fixa.fixa_api.application.service;

import com.fixa.fixa_api.domain.model.Empresa;
import com.fixa.fixa_api.domain.model.Servicio;
import com.fixa.fixa_api.domain.repository.EmpresaRepositoryPort;
import com.fixa.fixa_api.domain.repository.ServicioRepositoryPort;
import com.fixa.fixa_api.domain.repository.SuscripcionRepositoryPort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EmpresaService {

    private final EmpresaRepositoryPort empresaPort;
    private final ServicioRepositoryPort servicioPort;
    private final SuscripcionRepositoryPort suscripcionPort;
    private final SuscripcionService suscripcionService;

    public EmpresaService(EmpresaRepositoryPort empresaPort, ServicioRepositoryPort servicioPort,
            SuscripcionRepositoryPort suscripcionPort, SuscripcionService suscripcionService) {
        this.empresaPort = empresaPort;
        this.servicioPort = servicioPort;
        this.suscripcionPort = suscripcionPort;
        this.suscripcionService = suscripcionService;
    }

    public List<Empresa> listar(Boolean visibles) {
        return Boolean.TRUE.equals(visibles) ? empresaPort.findVisibles() : empresaPort.findAll();
    }

    public List<Empresa> listarConFiltros(Boolean visibles, Boolean activo, Long categoriaId) {
        List<Empresa> base = listar(visibles);
        return base.stream()
                .filter(e -> activo == null || e.isActivo() == activo)
                .filter(e -> categoriaId == null
                        || (e.getCategoriaId() != null && e.getCategoriaId().equals(categoriaId)))
                .collect(Collectors.toList());
    }

    public List<Empresa> listarConFiltrosPaginado(Boolean visibles, Boolean activo, Long categoriaId, Integer page,
            Integer size) {
        List<Empresa> filtrado = listarConFiltros(visibles, activo, categoriaId);
        if (page == null || size == null || page < 0 || size <= 0)
            return filtrado;
        int from = Math.min(page * size, filtrado.size());
        int to = Math.min(from + size, filtrado.size());
        return filtrado.subList(from, to);
    }

    /**
     * Empresas públicas (visibles) y activas que además tienen una suscripción
     * activa.
     */
    public List<Empresa> listarPublicasConSuscripcionActivaPaginado(Long categoriaId, Integer page, Integer size) {
        // visibles = true, activo = true
        List<Empresa> base = listarConFiltrosPaginado(true, true, categoriaId, page, size);
        return base.stream()
                .filter(e -> suscripcionPort.findActivaByEmpresaId(e.getId()).isPresent())
                .collect(Collectors.toList());
    }

    public List<Empresa> listarPublicasConSuscripcionActiva(Boolean visibles, Boolean activo, Long categoriaId) {
        List<Empresa> base = listarConFiltros(visibles, activo, categoriaId);
        return base.stream()
                .filter(e -> suscripcionPort.findActivaByEmpresaId(e.getId()).isPresent())
                .collect(Collectors.toList());
    }

    public List<Empresa> listarPublicasConSuscripcionActivaPaginado(Boolean visibles, Boolean activo, Long categoriaId,
            Integer page, Integer size) {
        List<Empresa> filtrado = listarPublicasConSuscripcionActiva(visibles, activo, categoriaId);
        if (page == null || size == null || page < 0 || size <= 0)
            return filtrado;
        int from = Math.min(page * size, filtrado.size());
        int to = Math.min(from + size, filtrado.size());
        return filtrado.subList(from, to);
    }

    public List<Empresa> listarPublicasPorCategoriaServicioPaginado(Long categoriaServicioId, Integer page,
            Integer size) {
        List<Empresa> visibles = listar(true).stream()
                .filter(Empresa::isActivo)
                .collect(Collectors.toList());

        if (categoriaServicioId == null) {
            if (page == null || size == null || page < 0 || size <= 0)
                return visibles;
            int from = Math.min(page * size, visibles.size());
            int to = Math.min(from + size, visibles.size());
            return visibles.subList(from, to);
        }

        List<Servicio> servicios = servicioPort.findAll().stream()
                .filter(Servicio::isActivo)
                .filter(s -> s.getCategoriaId() != null && s.getCategoriaId().equals(categoriaServicioId))
                .collect(Collectors.toList());

        if (servicios.isEmpty()) {
            return List.of();
        }

        java.util.Set<Long> empresaIds = servicios.stream()
                .map(Servicio::getEmpresaId)
                .filter(java.util.Objects::nonNull)
                .collect(java.util.stream.Collectors.toSet());

        List<Empresa> filtrado = visibles.stream()
                .filter(e -> empresaIds.contains(e.getId()))
                .collect(Collectors.toList());

        if (page == null || size == null || page < 0 || size <= 0)
            return filtrado;
        int from = Math.min(page * size, filtrado.size());
        int to = Math.min(from + size, filtrado.size());
        return filtrado.subList(from, to);
    }

    public Optional<Empresa> obtener(Long id) {
        return empresaPort.findById(id);
    }

    public boolean tieneSuscripcionActiva(Long empresaId) {
        return suscripcionPort.findActivaByEmpresaId(empresaId).isPresent();
    }

    public Optional<Empresa> obtenerPorSlug(String slug) {
        return empresaPort.findBySlug(slug);
    }

    public Optional<Empresa> buscarPorAdmin(Long usuarioId) {
        return empresaPort.findByUsuarioAdminId(usuarioId);
    }

    public Empresa actualizar(Empresa empresa) {
        return empresaPort.save(empresa);
    }

    public Empresa guardar(Empresa empresa) {
        // Generar slug automáticamente si no está presente
        if (empresa.getSlug() == null || empresa.getSlug().isBlank()) {
            String slug = generarSlugUnico(empresa.getNombre());
            empresa.setSlug(slug);
        }

        boolean esNueva = empresa.getId() == null;
        Empresa saved = empresaPort.save(empresa);

        if (esNueva) {
            // Asignar plan gratuito por defecto (ID 1)
            // TODO: Buscar plan por nombre o configuración en lugar de ID fijo
            try {
                // Buscamos el plan gratuito (asumiendo que es el ID 1 o el primero con precio
                // 0)
                // Por simplicidad usaremos ID 1 como dice la migración
                suscripcionService.asignarPlan(saved.getId(), 1L, java.math.BigDecimal.ZERO);

                // Actualizar referencia en empresa
                saved.setPlanActualId(1L);
                empresaPort.save(saved);
            } catch (Exception e) {
                // Log error pero no fallar la creación de empresa?
                // O fallar para garantizar consistencia?
                // Por ahora logueamos (system out) y continuamos
                System.err.println("Error asignando plan por defecto: " + e.getMessage());
            }
        }

        return saved;
    }

    public boolean activar(Long id, boolean activo) {
        Optional<Empresa> opt = empresaPort.findById(id);
        if (opt.isEmpty())
            return false;
        Empresa e = opt.get();
        e.setActivo(activo);
        empresaPort.save(e);
        return true;
    }

    public Optional<com.fixa.fixa_api.domain.model.Suscripcion> asignarPlan(Long empresaId, Long planId,
            java.math.BigDecimal precioPactado) {
        Optional<Empresa> opt = empresaPort.findById(empresaId);
        if (opt.isEmpty())
            return Optional.empty();

        com.fixa.fixa_api.domain.model.Suscripcion suscripcion = suscripcionService.asignarPlan(empresaId, planId,
                precioPactado);

        // Actualizar referencia en empresa
        Empresa e = opt.get();
        e.setPlanActualId(planId);
        empresaPort.save(e);

        return Optional.of(suscripcion);
    }

    private String generarSlugUnico(String nombre) {
        String baseSlug = generarSlug(nombre);
        String currentSlug = baseSlug;
        int counter = 1;

        while (empresaPort.findBySlug(currentSlug).isPresent()) {
            currentSlug = baseSlug + "-" + counter;
            counter++;
        }

        return currentSlug;
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

    public Empresa actualizarDatosEmpresa(Long empresaId,
            com.fixa.fixa_api.infrastructure.in.web.dto.ActualizarEmpresaRequest req) {
        Empresa e = empresaPort.findById(empresaId)
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada"));

        if (req.getNombre() != null)
            e.setNombre(req.getNombre());
        if (req.getDescripcion() != null)
            e.setDescripcion(req.getDescripcion());
        if (req.getDireccion() != null)
            e.setDireccion(req.getDireccion());
        if (req.getTelefono() != null)
            e.setTelefono(req.getTelefono());
        if (req.getEmail() != null)
            e.setEmail(req.getEmail());
        if (req.getBannerUrl() != null)
            e.setBannerUrl(req.getBannerUrl());
        if (req.getLogoUrl() != null)
            e.setLogoUrl(req.getLogoUrl());
        if (req.getCategoriaId() != null)
            e.setCategoriaId(req.getCategoriaId());

        e.setPermiteReservasSinUsuario(req.isPermiteReservasSinUsuario());
        e.setRequiereValidacionTelefono(req.isRequiereValidacionTelefono());
        e.setRequiereAprobacionTurno(req.isRequiereAprobacionTurno());
        e.setMensajeValidacionPersonalizado(req.getMensajeValidacionPersonalizado());
        e.setVisibilidadPublica(req.isVisibilidadPublica());

        // NO TOCAMOS 'activo', 'slug', 'planActual', 'usuarioAdmin'

        return empresaPort.save(e);
    }
}
