package com.fixa.fixa_api.application.service;

import com.fixa.fixa_api.domain.model.Usuario;
import com.fixa.fixa_api.domain.model.UsuarioEmpresa;
import com.fixa.fixa_api.domain.model.Empleado;
import com.fixa.fixa_api.domain.repository.UsuarioRepositoryPort;
import com.fixa.fixa_api.domain.repository.UsuarioEmpresaRepositoryPort;
import com.fixa.fixa_api.domain.repository.EmpleadoRepositoryPort;
import com.fixa.fixa_api.infrastructure.in.web.error.ApiException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AuthService {

    private final UsuarioRepositoryPort usuarioPort;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final EmpleadoRepositoryPort empleadoPort;
    private final UsuarioEmpresaRepositoryPort usuarioEmpresaPort;

    public AuthService(UsuarioRepositoryPort usuarioPort,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       EmpleadoRepositoryPort empleadoPort,
                       UsuarioEmpresaRepositoryPort usuarioEmpresaPort) {
        this.usuarioPort = usuarioPort;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.empleadoPort = empleadoPort;
        this.usuarioEmpresaPort = usuarioEmpresaPort;
    }

    public Usuario register(String nombre, String apellido, String email, String telefono, String rawPassword) {
        Optional<Usuario> existente = usuarioPort.findByEmail(email);
        if (existente.isPresent()) {
            throw new IllegalArgumentException("Email ya registrado");
        }
        String hash = passwordEncoder.encode(rawPassword);
        Usuario u = new Usuario();
        u.setNombre(nombre);
        u.setApellido(apellido);
        u.setEmail(email);
        u.setTelefono(telefono);
        u.setRol("CLIENTE");
        u.setActivo(true);
        Usuario saved = usuarioPort.saveWithPasswordHash(u, hash);
        vincularUsuarioConEmpresasPorEmail(saved);
        return saved;
    }

    public Usuario login(String email, String password) {
        Authentication auth = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
        if (!auth.isAuthenticated()) {
            throw new IllegalArgumentException("Credenciales inválidas");
        }
        Usuario u = usuarioPort.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado tras login"));
        vincularUsuarioConEmpresasPorEmail(u);
        return u;
    }

    public Usuario loginOrRegisterGoogle(String email, String nombre, String apellido) {
        Optional<Usuario> existente = usuarioPort.findByEmail(email);
        if (existente.isPresent()) {
            Usuario uExistente = existente.get();
            vincularUsuarioConEmpresasPorEmail(uExistente);
            return uExistente;
        }

        Usuario u = new Usuario();
        u.setNombre(nombre);
        u.setApellido(apellido);
        u.setEmail(email);
        u.setTelefono(null);
        u.setRol("CLIENTE");
        u.setActivo(true);
        Usuario saved = usuarioPort.save(u);
        vincularUsuarioConEmpresasPorEmail(saved);
        return saved;
    }

    public Usuario cambiarEmail(Long usuarioId, String currentPassword, String nuevoEmail) {
        if (nuevoEmail == null || nuevoEmail.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Nuevo email requerido");
        }

        Usuario usuario = usuarioPort.findById(usuarioId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        if (usuario.getEmail() != null && usuario.getEmail().equalsIgnoreCase(nuevoEmail)) {
            return usuario;
        }

        usuarioPort.findByEmail(nuevoEmail).ifPresent(u -> {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Email ya registrado");
        });

        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(usuario.getEmail(), currentPassword)
        );
        if (!auth.isAuthenticated()) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas");
        }

        List<Empleado> empleados = empleadoPort.findByUsuarioId(usuarioId);
        for (Empleado empleado : empleados) {
            empleado.setUsuarioId(null);
            empleadoPort.save(empleado);
        }

        List<UsuarioEmpresa> relaciones = usuarioEmpresaPort.findByUsuario(usuarioId);
        for (UsuarioEmpresa rel : relaciones) {
            usuarioEmpresaPort.deleteByUsuarioAndEmpresa(usuarioId, rel.getEmpresaId());
        }

        usuario.setEmail(nuevoEmail);
        return usuarioPort.save(usuario);
    }

    private void vincularUsuarioConEmpresasPorEmail(Usuario usuario) {
        if (usuario == null) return;
        String email = usuario.getEmail();
        if (email == null || email.isBlank()) return;

        List<Empleado> empleados = empleadoPort.findActivosSinUsuarioPorEmail(email);
        if (empleados.isEmpty()) return;

        for (Empleado empleado : empleados) {
            empleado.setUsuarioId(usuario.getId());
            empleadoPort.save(empleado);

            Long empresaId = empleado.getEmpresaId();
            if (empresaId == null) continue;

            boolean existe = usuarioEmpresaPort.existsByUsuarioAndEmpresa(usuario.getId(), empresaId);
            if (!existe) {
                UsuarioEmpresa rel = new UsuarioEmpresa();
                rel.setUsuarioId(usuario.getId());
                rel.setEmpresaId(empresaId);
                rel.setRolEmpresa("STAFF");
                rel.setActivo(true);
                usuarioEmpresaPort.save(rel);
            }
        }
    }
}
