package com.moises.turism.dto.anfitrion;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ReenviarValidacionAnfitrionRequest(
        @NotBlank(message = "documentoIdentidad es requerido")
        @Size(max = 30, message = "documentoIdentidad no debe superar 30 caracteres")
        String documentoIdentidad,

        @NotBlank(message = "descripcion es requerida")
        String descripcion
) {
}
