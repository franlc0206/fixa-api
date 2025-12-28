package com.fixa.fixa_api.infrastructure.config;

import com.fixa.fixa_api.infrastructure.out.persistence.entity.UsuarioEntity;
import com.fixa.fixa_api.infrastructure.out.persistence.repository.UsuarioJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataSeeder {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    @Value("${SUPERADMIN_EMAIL:admin@fixa.local}")
    private String superadminEmail;

    @Value("${SUPERADMIN_PASSWORD:admin123}")
    private String superadminPassword;

    @Bean
    public CommandLineRunner seedSuperadmin(UsuarioJpaRepository repo, PasswordEncoder encoder) {
        return args -> {
            String email = superadminEmail;
            if (repo.findByEmailIgnoreCase(email).isEmpty()) {
                UsuarioEntity u = new UsuarioEntity();
                u.setNombre("Super");
                u.setApellido("Admin");
                u.setEmail(email);
                u.setTelefono("000000000");
                u.setPasswordHash(encoder.encode(superadminPassword));
                u.setRol("SUPERADMIN");
                u.setActivo(true);
                repo.save(u);
                log.info("Seeded SUPERADMIN user {} (password set from env)", email);
            } else {
                log.info("SUPERADMIN seed skipped; user {} already exists", email);
            }
        };
    }
}
