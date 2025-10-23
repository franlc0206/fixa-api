package com.fixa.fixa_api.application.service;

import com.fixa.fixa_api.domain.model.Usuario;
import com.fixa.fixa_api.domain.repository.UsuarioRepositoryPort;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    private final UsuarioRepositoryPort usuarioPort;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public AuthService(UsuarioRepositoryPort usuarioPort,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager) {
        this.usuarioPort = usuarioPort;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
    }

    public Usuario register(String nombre, String apellido, String email, String telefono, String rawPassword, String rol) {
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
        u.setRol(rol);
        u.setActivo(true);
        return usuarioPort.saveWithPasswordHash(u, hash);
    }

    public Usuario login(String email, String password) {
        Authentication auth = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
        if (!auth.isAuthenticated()) {
            throw new IllegalArgumentException("Credenciales inválidas");
        }
        return usuarioPort.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado tras login"));
    }
}
