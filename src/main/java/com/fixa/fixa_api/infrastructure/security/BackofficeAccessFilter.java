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
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class BackofficeAccessFilter extends OncePerRequestFilter {

    private final CurrentUserService currentUserService;
    private final UsuarioEmpresaRepositoryPort usuarioEmpresaPort;
    private final ObjectMapper objectMapper;
    private final com.fixa.fixa_api.application.service.SuscripcionService suscripcionService;

    // Regex para extraer empresaId de rutas como /api/empresas/{id}/...
    private static final Pattern EMPRESA_PATH_PATTERN = Pattern.compile("^/api/empresas/(\\d+)(/.*)?$");

    public BackofficeAccessFilter(CurrentUserService currentUserService,
            UsuarioEmpresaRepositoryPort usuarioEmpresaPort,
            ObjectMapper objectMapper,
            com.fixa.fixa_api.application.service.SuscripcionService suscripcionService) {
        this.currentUserService = currentUserService;
        this.usuarioEmpresaPort = usuarioEmpresaPort;
        this.objectMapper = objectMapper;
        this.suscripcionService = suscripcionService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();

        // 1. Si no es una ruta protegida relevante, pasamos
        // Rutas que queremos proteger explícitamente: /api/backoffice/** y
        // /api/empresas/{id}/**
        boolean isBackoffice = path.startsWith("/api/backoffice");
        boolean isEmpresaResource = path.startsWith("/api/empresas/");

        if (!isBackoffice && !isEmpresaResource) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2. Dejar que Spring Security autentique primero (el filtro JWT va antes)
        // Pero como este filtro se ejecuta en la cadena, necesitamos saber si el
        // usuario está autenticado.
        // Si el filtro JWT ya pasó, el SecurityContext debería tener la autenticación.
        // OJO: En la configuración de seguridad, este filtro se añade ANTES de
        // UsernamePasswordAuthenticationFilter
        // pero DESPUÉS de JwtAuthenticationFilter.

        // Verificamos si hay usuario autenticado
        Optional<Long> userIdOpt = currentUserService.getCurrentUserId();
        if (userIdOpt.isEmpty()) {
            // Si no hay usuario, dejamos pasar para que Spring Security maneje el 401/403
            // estándar
            // o si es una ruta pública (aunque estas rutas suelen ser protegidas)
            filterChain.doFilter(request, response);
            return;
        }

        Long userId = userIdOpt.get();

        // 3. Validación específica para /api/empresas/{id}
        if (isEmpresaResource) {
            Matcher matcher = EMPRESA_PATH_PATTERN.matcher(path);
            if (matcher.find()) {
                String empresaIdStr = matcher.group(1);
                try {
                    Long empresaId = Long.parseLong(empresaIdStr);
                    if (!usuarioEmpresaPort.existsByUsuarioAndEmpresa(userId, empresaId)) {
                        sendErrorResponse(response, HttpServletResponse.SC_FORBIDDEN, "FORBIDDEN",
                                "No tienes acceso a la empresa solicitada (" + empresaId + ")");
                        return;
                    }
                    // Validar suscripción activa
                    try {
                        suscripcionService.validarSuscripcionActiva(empresaId);
                    } catch (com.fixa.fixa_api.infrastructure.in.web.error.ApiException e) {
                        sendErrorResponse(response, HttpServletResponse.SC_FORBIDDEN, "SUBSCRIPTION_EXPIRED",
                                e.getMessage());
                        return;
                    }
                } catch (NumberFormatException e) {
                    // ID no válido, dejamos pasar y que el controller falle o 404
                }
            }
        }

        // 4. Validación para /api/backoffice
        // Aquí la validación es más laxa: solo verificar que tenga AL MENOS UNA empresa
        // activa
        // ya que el controller determinará cuál usar.
        if (isBackoffice) {
            var empresas = usuarioEmpresaPort.findByUsuario(userId);
            boolean tieneEmpresaActiva = empresas.stream().anyMatch(ue -> ue.isActivo());

            if (!tieneEmpresaActiva) {
                // Excepción: Permitir crear empresa si es el flujo de onboarding (si existiera)
                // Por ahora bloqueamos.
                sendErrorResponse(response, HttpServletResponse.SC_FORBIDDEN, "NO_ACTIVE_COMPANY",
                        "No tienes ninguna empresa activa asociada a tu cuenta.");
                return;
            }
            // Opcional: Validar que al menos una empresa tenga suscripción activa?
            // Por ahora no bloqueamos el acceso general al backoffice, solo a recursos
            // específicos
            // O podríamos redirigir a la pantalla de selección de empresa.
        }

        filterChain.doFilter(request, response);
    }

    private void sendErrorResponse(HttpServletResponse response, int status, String code, String message)
            throws IOException {
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
