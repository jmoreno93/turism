package com.moises.turism.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "email es requerido")
        @Email(message = "email no tiene un formato válido")
        String email,

        @NotBlank(message = "password es requerido")
        String password
) {
}
