package com.fixa.fixa_api.application.service;

import com.fixa.fixa_api.domain.model.Categoria;
import com.fixa.fixa_api.domain.repository.CategoriaRepositoryPort;
import com.fixa.fixa_api.infrastructure.in.web.error.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SuperAdminCategoriaService {

    private final CategoriaRepositoryPort categoriaPort;

    public SuperAdminCategoriaService(CategoriaRepositoryPort categoriaPort) {
        this.categoriaPort = categoriaPort;
    }

    public List<Categoria> list() {
        return categoriaPort.findAll();
    }

    public Categoria create(String tipo, String nombre, String descripcion, String icono, String fotoDefault,
            boolean activo) {
        if (tipo == null || tipo.isBlank())
            throw new ApiException(HttpStatus.BAD_REQUEST, "tipo requerido");
        if (nombre == null || nombre.isBlank())
            throw new ApiException(HttpStatus.BAD_REQUEST, "nombre requerido");
        Categoria c = new Categoria();
        c.setTipo(tipo);
        c.setNombre(nombre);
        c.setDescripcion(descripcion);
        c.setIcono(icono);
        c.setFotoDefault(fotoDefault);
        c.setActivo(activo);
        return categoriaPort.save(c);
    }

    public Optional<Categoria> update(Long id, String tipo, String nombre, String descripcion, String icono,
            String fotoDefault,
            Boolean activo) {
        return categoriaPort.findById(id).map(c -> {
            if (tipo != null)
                c.setTipo(tipo);
            if (nombre != null)
                c.setNombre(nombre);
            if (descripcion != null)
                c.setDescripcion(descripcion);
            if (icono != null)
                c.setIcono(icono);
            if (fotoDefault != null)
                c.setFotoDefault(fotoDefault);
            if (activo != null)
                c.setActivo(activo);
            return categoriaPort.save(c);
        });
    }

    public boolean activar(Long id, boolean activo) {
        Optional<Categoria> opt = categoriaPort.findById(id);
        if (opt.isEmpty())
            return false;
        Categoria c = opt.get();
        c.setActivo(activo);
        categoriaPort.save(c);
        return true;
    }
}
