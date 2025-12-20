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
    private final String refreshSecret;
    private final long jwtExpirationMs;
    private final long refreshExpirationMs;

    public JwtTokenProvider(
            @Value("${JWT_SECRET:change-me-super-secret-change-me-super-secret}") String jwtSecret,
            @Value("${JWT_REFRESH_SECRET:change-me-refresh-secret-change-me-refresh-secret}") String refreshSecret,
            @Value("${JWT_EXPIRATION_MS:3600000}") long jwtExpirationMs,
            @Value("${JWT_REFRESH_EXPIRATION_MS:2592000000}") long refreshExpirationMs) {
        this.jwtSecret = jwtSecret;
        this.refreshSecret = refreshSecret;
        this.jwtExpirationMs = jwtExpirationMs;
        this.refreshExpirationMs = refreshExpirationMs;
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

    public String generateRefreshToken(Usuario usuario) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + refreshExpirationMs);

        return Jwts.builder()
                .setSubject(usuario.getId().toString())
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(Keys.hmacShaKeyFor(refreshSecret.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims getClaims(String token) {
        return parseClaims(token, jwtSecret);
    }

    public Claims getRefreshClaims(String token) {
        return parseClaims(token, refreshSecret);
    }

    private Claims parseClaims(String token, String secret) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception ex) {
            return null;
        }
    }

    public long getRefreshExpirationMs() {
        return refreshExpirationMs;
    }
}
