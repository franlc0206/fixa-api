package com.fixa.fixa_api.infrastructure.config;

import com.fixa.fixa_api.infrastructure.security.BackofficeAccessFilter;
import com.fixa.fixa_api.infrastructure.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final BackofficeAccessFilter backofficeAccessFilter;

    @Value("${ALLOWED_ORIGINS:http://localhost:5173}")
    private String allowedOrigins;

    public SecurityConfig(BackofficeAccessFilter backofficeAccessFilter) {
        this.backofficeAccessFilter = backofficeAccessFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        List<String> origins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
        // Use patterns to be resilient to proxy header nuances
        config.setAllowedOriginPatterns(origins.isEmpty() ? List.of("http://localhost:5174") : origins);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        // Allow all headers to avoid preflight rejections due to custom headers
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("Location"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public CorsFilter corsWebFilter() {
        return new CorsFilter(corsConfigurationSource());
    }

    @Bean
    @Order(1)
    public SecurityFilterChain publicFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/public/**", "/api/auth/**", "/health", "/actuator/**")
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .anyRequest().permitAll()
                );
        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain protectedFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Permitir preflight CORS globalmente
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // SuperAdmin exclusivo
                        .requestMatchers("/api/superadmin/**").hasRole("SUPERADMIN")
                        // BackOffice requiere autenticación
                        .requestMatchers("/api/backoffice/**").authenticated()
                        // Empresas: SuperAdmin para crear/modificar
                        .requestMatchers(HttpMethod.POST, "/api/empresas").hasRole("SUPERADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/empresas/**").hasRole("SUPERADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/empresas/**").hasRole("SUPERADMIN")
                        // Otros recursos empresariales
                        .requestMatchers(
                                "/api/empresas/**",
                                "/api/empleados/**",
                                "/api/servicios/**",
                                "/api/disponibilidad/**"
                        ).authenticated()
                        // Turnos
                        .requestMatchers("/api/turnos/**").authenticated()
                        // Todo lo demás requiere autenticación
                        .anyRequest().authenticated()
                );

        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
