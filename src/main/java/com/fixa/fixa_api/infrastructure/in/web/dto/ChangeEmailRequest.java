package com.fixa.fixa_api.infrastructure.in.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class ChangeEmailRequest {

    @Email
    @NotBlank
    private String nuevoEmail;

    @NotBlank
    private String password;

    public String getNuevoEmail() {
        return nuevoEmail;
    }

    public void setNuevoEmail(String nuevoEmail) {
        this.nuevoEmail = nuevoEmail;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
