package com.fixa.fixa_api.infrastructure.config;

import com.fixa.fixa_api.infrastructure.security.BackofficeAccessFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final BackofficeAccessFilter backofficeAccessFilter;

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
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                // NOTA: Removido el BackofficeAccessFilter - La verificación de empresa
                // se hace directamente en los controllers cuando es necesario
                // .addFilterAfter(backofficeAccessFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth
                        // Endpoints completamente públicos (sin auth requerida)
                        .requestMatchers(
                                "/health",
                                "/api/auth/**",
                                "/api/public/**"
                        ).permitAll()
                        // SuperAdmin exclusivo
                        .requestMatchers("/api/superadmin/**").hasRole("SUPERADMIN")
                        // BackOffice (filtrado por BackofficeAccessFilter)
                        .requestMatchers("/api/backoffice/**").hasAnyRole("SUPERADMIN", "EMPRESA", "EMPLEADO")
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
                        ).hasAnyRole("SUPERADMIN", "EMPRESA")
                        // Turnos
                        .requestMatchers(
                                "/api/turnos/**"
                        ).hasAnyRole("SUPERADMIN", "EMPRESA", "EMPLEADO")
                        // Todo lo demás requiere autenticación
                        .anyRequest().authenticated()
                )
                .httpBasic(Customizer.withDefaults());
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:5173"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept", "Origin"));
        config.setExposedHeaders(List.of("Location"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
