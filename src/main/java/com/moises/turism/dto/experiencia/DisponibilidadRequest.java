package com.moises.turism.dto.experiencia;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record DisponibilidadRequest(
        @NotNull(message = "fechaDisponible es requerida")
        @FutureOrPresent(message = "fechaDisponible no puede estar en el pasado")
        LocalDate fechaDisponible,

        @NotNull(message = "cuposDisponibles es requerido")
        @Min(value = 1, message = "cuposDisponibles debe ser mayor o igual a 1")
        Integer cuposDisponibles
) {
}
