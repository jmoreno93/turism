package com.moises.turism.dto.experiencia;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public record CrearExperienciaRequest(
        @NotNull(message = "idCategoria es requerido")
        Integer idCategoria,

        @NotBlank(message = "titulo es requerido")
        @Size(max = 150, message = "titulo no debe superar 150 caracteres")
        String titulo,

        @NotBlank(message = "descripcion es requerida")
        String descripcion,

        @NotBlank(message = "ubicacion es requerida")
        @Size(max = 150, message = "ubicacion no debe superar 150 caracteres")
        String ubicacion,

        @NotNull(message = "precio es requerido")
        @DecimalMin(value = "0.01", message = "precio debe ser mayor a 0")
        BigDecimal precio,

        @NotNull(message = "capacidadMaxima es requerida")
        @Min(value = 1, message = "capacidadMaxima debe ser mayor o igual a 1")
        Integer capacidadMaxima,

        @NotNull(message = "duracionHoras es requerida")
        @Min(value = 1, message = "duracionHoras debe ser mayor o igual a 1")
        Integer duracionHoras,

        @NotEmpty(message = "fotosUrls debe contener al menos una foto")
        List<@NotBlank(message = "La URL de foto no puede estar vacía") @Size(max = 255, message = "La URL de foto no debe superar 255 caracteres") String> fotosUrls,

        @NotEmpty(message = "disponibilidades debe contener al menos una fecha")
        List<@Valid DisponibilidadRequest> disponibilidades
) {
}
