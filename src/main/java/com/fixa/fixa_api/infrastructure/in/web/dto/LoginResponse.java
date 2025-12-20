package com.fixa.fixa_api.infrastructure.in.web.dto;

public class LoginResponse {
    private Long id;
    private String email;
    private String rol;
    private String accessToken;
    private String refreshToken;

    public LoginResponse(Long id, String email, String rol, String accessToken, String refreshToken) {
        this.id = id;
        this.email = email;
        this.rol = rol;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
