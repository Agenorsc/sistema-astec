package br.com.astec.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank @Email String email,
        @NotBlank String senha) {

    public LoginRequest {
        if (email != null) {
            email = email.trim();
        }
    }
}
