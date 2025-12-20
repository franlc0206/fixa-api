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
    private final com.fixa.fixa_api.domain.repository.RefreshTokenRepositoryPort refreshTokenPort;
    private final com.fixa.fixa_api.infrastructure.security.JwtTokenProvider jwtTokenProvider;

    public AuthService(UsuarioRepositoryPort usuarioPort,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            EmpleadoRepositoryPort empleadoPort,
            UsuarioEmpresaRepositoryPort usuarioEmpresaPort,
            com.fixa.fixa_api.domain.repository.RefreshTokenRepositoryPort refreshTokenPort,
            com.fixa.fixa_api.infrastructure.security.JwtTokenProvider jwtTokenProvider) {
        this.usuarioPort = usuarioPort;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.empleadoPort = empleadoPort;
        this.usuarioEmpresaPort = usuarioEmpresaPort;
        this.refreshTokenPort = refreshTokenPort;
        this.jwtTokenProvider = jwtTokenProvider;
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
        Authentication auth = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(email, password));
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
                new UsernamePasswordAuthenticationToken(usuario.getEmail(), currentPassword));
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

    public com.fixa.fixa_api.domain.model.RefreshToken createRefreshToken(Usuario usuario) {
        // Borrar tokens antiguos del usuario (opcional, para mantener 1 sesión activa o
        // manejar rotación)
        refreshTokenPort.deleteByUsuarioId(usuario.getId());

        String token = jwtTokenProvider.generateRefreshToken(usuario);
        java.time.Instant expiry = java.time.Instant.now().plusMillis(jwtTokenProvider.getRefreshExpirationMs());

        com.fixa.fixa_api.domain.model.RefreshToken rt = com.fixa.fixa_api.domain.model.RefreshToken.builder()
                .usuarioId(usuario.getId())
                .token(token)
                .expiryDate(expiry)
                .build();

        return refreshTokenPort.save(rt);
    }

    public com.fixa.fixa_api.infrastructure.in.web.dto.LoginResponse refresh(String refreshToken) {
        return refreshTokenPort.findByToken(refreshToken)
                .map(rt -> {
                    // Validar expiración
                    if (rt.getExpiryDate().isBefore(java.time.Instant.now())) {
                        refreshTokenPort.deleteByUsuarioId(rt.getUsuarioId());
                        throw new ApiException(HttpStatus.UNAUTHORIZED, "Refresh Token expirado");
                    }

                    // Obtener usuario
                    Usuario usuario = usuarioPort.findById(rt.getUsuarioId())
                            .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Usuario no encontrado"));

                    // Generar nuevos tokens
                    String newAccessToken = jwtTokenProvider.generateToken(usuario);
                    com.fixa.fixa_api.domain.model.RefreshToken newRefreshToken = createRefreshToken(usuario);

                    return new com.fixa.fixa_api.infrastructure.in.web.dto.LoginResponse(
                            usuario.getId(),
                            usuario.getEmail(),
                            usuario.getRol(),
                            newAccessToken,
                            newRefreshToken.getToken());
                })
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Refresh Token inválido"));
    }

    private void vincularUsuarioConEmpresasPorEmail(Usuario usuario) {
        if (usuario == null)
            return;
        String email = usuario.getEmail();
        if (email == null || email.isBlank())
            return;

        List<Empleado> empleados = empleadoPort.findActivosSinUsuarioPorEmail(email);
        if (empleados.isEmpty())
            return;

        for (Empleado empleado : empleados) {
            empleado.setUsuarioId(usuario.getId());
            empleadoPort.save(empleado);

            Long empresaId = empleado.getEmpresaId();
            if (empresaId == null)
                continue;

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
