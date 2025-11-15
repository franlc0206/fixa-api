package com.fixa.fixa_api.infrastructure.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);
        Claims claims = jwtTokenProvider.getClaims(token);
        if (claims == null) {
            filterChain.doFilter(request, response);
            return;
        }

        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            String email = claims.get("email", String.class);
            String rol = claims.get("rol", String.class);
            Collection<? extends GrantedAuthority> authorities = buildAuthorities(rol);
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(email, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        filterChain.doFilter(request, response);
    }

    private Collection<? extends GrantedAuthority> buildAuthorities(String rol) {
        String normalized = rol == null ? "" : rol.trim().toUpperCase();
        String roleName;
        switch (normalized) {
            case "SUPERADMIN" -> roleName = "ROLE_SUPERADMIN";
            case "EMPRESA" -> roleName = "ROLE_EMPRESA";
            case "EMPLEADO" -> roleName = "ROLE_EMPLEADO";
            default -> roleName = "ROLE_CLIENTE";
        }
        return List.of(new SimpleGrantedAuthority(roleName));
    }
}
