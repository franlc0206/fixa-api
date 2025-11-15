package com.fixa.fixa_api.infrastructure.security;

import com.fixa.fixa_api.domain.model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final String jwtSecret;
    private final long jwtExpirationMs;

    public JwtTokenProvider(
            @Value("${JWT_SECRET:change-me-super-secret-change-me-super-secret}") String jwtSecret,
            @Value("${JWT_EXPIRATION_MS:3600000}") long jwtExpirationMs
    ) {
        this.jwtSecret = jwtSecret;
        this.jwtExpirationMs = jwtExpirationMs;
    }

    public String generateToken(Usuario usuario) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .setSubject(usuario.getId() != null ? usuario.getId().toString() : null)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .claim("email", usuario.getEmail())
                .claim("rol", usuario.getRol())
                .signWith(Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims getClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)))
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception ex) {
            return null;
        }
    }
}
