package com.moises.turism.dto.reserva;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CrearReservaRequest(
        @NotNull(message = "idUsuario es requerido")
        Long idUsuario,

        @NotNull(message = "idExperiencia es requerido")
        Long idExperiencia,

        @NotNull(message = "fechaExperiencia es requerida")
        @Future(message = "fechaExperiencia debe ser desde mañana en adelante")
        LocalDate fechaExperiencia,

        @NotNull(message = "cantidadPersonas es requerida")
        @Min(value = 1, message = "cantidadPersonas debe ser mayor o igual a 1")
        Integer cantidadPersonas,

        String observaciones
) {
}
