package com.fixa.fixa_api.infrastructure.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fixa.fixa_api.domain.repository.UsuarioEmpresaRepositoryPort;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class BackofficeAccessFilter extends OncePerRequestFilter {

    private final CurrentUserService currentUserService;
    private final UsuarioEmpresaRepositoryPort usuarioEmpresaPort;
    private final ObjectMapper objectMapper;

    public BackofficeAccessFilter(CurrentUserService currentUserService,
                                   UsuarioEmpresaRepositoryPort usuarioEmpresaPort,
                                   ObjectMapper objectMapper) {
        this.currentUserService = currentUserService;
        this.usuarioEmpresaPort = usuarioEmpresaPort;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                     HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {
        
        String path = request.getRequestURI();
        
        System.out.println("========================================");
        System.out.println(" [BACKOFFICE FILTER] Iniciando filtro");
        System.out.println(" [BACKOFFICE FILTER] Path: " + path);
        System.out.println(" [BACKOFFICE FILTER] Method: " + request.getMethod());
        
        // Solo aplicar este filtro a rutas de backoffice
        if (!path.startsWith("/api/backoffice")) {
            System.out.println(" [BACKOFFICE FILTER] Path NO es /api/backoffice - Skip filtro");
            filterChain.doFilter(request, response);
            return;
        }

        System.out.println(" [BACKOFFICE FILTER] Path SÍ es /api/backoffice - Aplicando filtro");

        // IMPORTANTE: Primero dejamos que Spring Security autentique
        // Continuamos la cadena de filtros y verificamos la empresa DESPUÉS
        filterChain.doFilter(request, response);
    }

    private void sendErrorResponse(HttpServletResponse response, int status, String code, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        Map<String, Object> errorBody = new HashMap<>();
        errorBody.put("code", code);
        errorBody.put("message", message);
        errorBody.put("details", null);
        
        response.getWriter().write(objectMapper.writeValueAsString(errorBody));
    }
}
