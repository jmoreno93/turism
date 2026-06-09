package com.moises.turism.dto.anfitrion;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RechazoRequest(
        @NotBlank(message = "motivo es requerido")
        @Size(max = 500, message = "motivo no debe superar 500 caracteres")
        String motivo
) {
}
