package com.fixa.fixa_api.application.service;

import com.fixa.fixa_api.domain.model.UsuarioEmpresa;
import com.fixa.fixa_api.domain.repository.EmpresaRepositoryPort;
import com.fixa.fixa_api.domain.repository.UsuarioEmpresaRepositoryPort;
import com.fixa.fixa_api.domain.repository.UsuarioRepositoryPort;
import com.fixa.fixa_api.infrastructure.in.web.error.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SuperAdminRelacionService {

    public static class RelacionDTO {
        public Long usuarioId;
        public Long empresaId;
        public String rolEmpresa;
        public boolean activo;
        public String usuarioEmail;
        public String usuarioNombre;
        public String empresaNombre;

        public static RelacionDTO of(UsuarioEmpresa d) {
            RelacionDTO r = new RelacionDTO();
            r.usuarioId = d.getUsuarioId();
            r.empresaId = d.getEmpresaId();
            r.rolEmpresa = d.getRolEmpresa();
            r.activo = d.isActivo();
            return r;
        }
    }

    public static class PageResponse<T> {
        public List<T> content;
        public int page;
        public int size;
        public long totalElements;
        public int totalPages;
    }

    private final UsuarioEmpresaRepositoryPort uePort;
    private final UsuarioRepositoryPort usuarioPort;
    private final EmpresaRepositoryPort empresaPort;

    public SuperAdminRelacionService(UsuarioEmpresaRepositoryPort uePort,
            UsuarioRepositoryPort usuarioPort,
            EmpresaRepositoryPort empresaPort) {
        this.uePort = uePort;
        this.usuarioPort = usuarioPort;
        this.empresaPort = empresaPort;
    }

    public List<RelacionDTO> listByUsuario(Long usuarioId) {
        List<RelacionDTO> items = uePort.findByUsuario(usuarioId).stream()
                .map(RelacionDTO::of)
                .collect(Collectors.toList());
        enrich(items);
        return items;
    }

    public List<RelacionDTO> listByEmpresa(Long empresaId) {
        List<RelacionDTO> items = uePort.findByEmpresa(empresaId).stream()
                .map(RelacionDTO::of)
                .collect(Collectors.toList());
        enrich(items);
        return items;
    }

    public PageResponse<RelacionDTO> listAllPaged(int page, int size) {
        List<RelacionDTO> all = uePort.findAll().stream().map(RelacionDTO::of).collect(Collectors.toList());
        int from = Math.max(0, page * size);
        int to = Math.min(all.size(), from + size);
        List<RelacionDTO> slice = from >= all.size() ? List.of() : all.subList(from, to);
        enrich(slice);
        PageResponse<RelacionDTO> resp = new PageResponse<>();
        resp.content = slice;
        resp.page = page;
        resp.size = size;
        resp.totalElements = all.size();
        resp.totalPages = (int) Math.ceil(all.size() / (double) size);
        return resp;
    }

    public RelacionDTO add(RelacionDTO req) {
        if (req.usuarioId == null)
            throw new ApiException(HttpStatus.BAD_REQUEST, "usuarioId requerido");
        if (req.empresaId == null)
            throw new ApiException(HttpStatus.BAD_REQUEST, "empresaId requerido");
        usuarioPort.findById(req.usuarioId)
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Usuario inexistente"));
        empresaPort.findById(req.empresaId)
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Empresa inexistente"));
        UsuarioEmpresa d = new UsuarioEmpresa();
        d.setUsuarioId(req.usuarioId);
        d.setEmpresaId(req.empresaId);
        d.setRolEmpresa(req.rolEmpresa);
        d.setActivo(req.activo);
        RelacionDTO resp = RelacionDTO.of(uePort.save(d));
        enrich(resp);
        return resp;
    }

    public void remove(Long usuarioId, Long empresaId) {
        if (usuarioId == null || empresaId == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "usuarioId y empresaId requeridos");
        }
        uePort.deleteByUsuarioAndEmpresa(usuarioId, empresaId);
    }

    public void activar(Long usuarioId, Long empresaId, boolean activo) {
        if (usuarioId == null || empresaId == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "usuarioId y empresaId requeridos");
        }
        UsuarioEmpresa rel = uePort.findByUsuarioAndEmpresa(usuarioId, empresaId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Relación no encontrada"));
        rel.setActivo(activo);
        uePort.save(rel);
    }

    private void enrich(List<RelacionDTO> items) {
        for (RelacionDTO r : items) {
            enrich(r);
        }
    }

    private void enrich(RelacionDTO r) {
        if (r == null)
            return;

        if (r.usuarioId != null) {
            usuarioPort.findById(r.usuarioId).ifPresent(u -> {
                r.usuarioEmail = u.getEmail();
                String nombre = u.getNombre() != null ? u.getNombre() : "";
                String apellido = u.getApellido() != null ? u.getApellido() : "";
                String fullName = (nombre + " " + apellido).trim();
                r.usuarioNombre = fullName.isEmpty() ? null : fullName;
            });
        }

        if (r.empresaId != null) {
            empresaPort.findById(r.empresaId).ifPresent(e -> {
                r.empresaNombre = e.getNombre();
            });
        }
    }
}
