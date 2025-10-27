package com.fixa.fixa_api.application.service;

import com.fixa.fixa_api.domain.model.Usuario;
import com.fixa.fixa_api.domain.repository.UsuarioRepositoryPort;
import com.fixa.fixa_api.infrastructure.in.web.error.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SuperAdminUserService {

    private final UsuarioRepositoryPort usuarioPort;

    public SuperAdminUserService(UsuarioRepositoryPort usuarioPort) {
        this.usuarioPort = usuarioPort;
    }

    public List<Usuario> list() {
        return usuarioPort.findAll();
    }

    public Usuario create(String nombre, String apellido, String email, String telefono, String rol, boolean activo) {
        if (nombre == null || nombre.isBlank()) throw new ApiException(HttpStatus.BAD_REQUEST, "nombre requerido");
        if (apellido == null || apellido.isBlank()) throw new ApiException(HttpStatus.BAD_REQUEST, "apellido requerido");
        if (email == null || email.isBlank()) throw new ApiException(HttpStatus.BAD_REQUEST, "email requerido");
        if (rol == null || rol.isBlank()) throw new ApiException(HttpStatus.BAD_REQUEST, "rol requerido");
        Usuario u = new Usuario();
        u.setNombre(nombre);
        u.setApellido(apellido);
        u.setEmail(email);
        u.setTelefono(telefono);
        u.setRol(rol);
        u.setActivo(activo);
        return usuarioPort.save(u);
    }

    public Optional<Usuario> update(Long id, String nombre, String apellido, String telefono, String rol, Boolean activo) {
        return usuarioPort.findById(id).map(u -> {
            if (nombre != null) u.setNombre(nombre);
            if (apellido != null) u.setApellido(apellido);
            if (telefono != null) u.setTelefono(telefono);
            if (rol != null) u.setRol(rol);
            if (activo != null) u.setActivo(activo);
            return usuarioPort.save(u);
        });
    }

    public boolean activar(Long id, boolean activo) {
        Optional<Usuario> opt = usuarioPort.findById(id);
        if (opt.isEmpty()) return false;
        Usuario u = opt.get();
        u.setActivo(activo);
        usuarioPort.save(u);
        return true;
    }
}
