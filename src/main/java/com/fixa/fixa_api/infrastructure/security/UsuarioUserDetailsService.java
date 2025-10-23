package com.fixa.fixa_api.infrastructure.security;

import com.fixa.fixa_api.infrastructure.out.persistence.entity.UsuarioEntity;
import com.fixa.fixa_api.infrastructure.out.persistence.repository.UsuarioJpaRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

@Service
public class UsuarioUserDetailsService implements UserDetailsService {

    private final UsuarioJpaRepository usuarioRepo;

    public UsuarioUserDetailsService(UsuarioJpaRepository usuarioRepo) {
        this.usuarioRepo = usuarioRepo;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UsuarioEntity u = usuarioRepo.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
        if (!u.isActivo()) {
            throw new UsernameNotFoundException("Usuario inactivo");
        }
        Collection<? extends GrantedAuthority> auths = buildAuthorities(u.getRol());
        return new User(u.getEmail(), u.getPasswordHash(), auths);
    }

    private List<GrantedAuthority> buildAuthorities(String rol) {
        // Espera valores: superadmin, empresa, empleado, cliente
        String normalized = rol == null ? "" : rol.trim().toUpperCase();
        String roleName = switch (normalized) {
            case "SUPERADMIN" -> "ROLE_SUPERADMIN";
            case "EMPRESA" -> "ROLE_EMPRESA";
            case "EMPLEADO" -> "ROLE_EMPLEADO";
            default -> "ROLE_CLIENTE";
        };
        return List.of(new SimpleGrantedAuthority(roleName));
    }
}
