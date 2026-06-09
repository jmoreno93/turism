package com.moises.turism.dto.experiencia;

import com.moises.turism.domain.DisponibilidadExperiencia;

import java.time.LocalDate;

public record ExperienciaDisponibilidadResponse(
        Long idDisponibilidad,
        LocalDate fechaDisponible,
        Integer cuposDisponibles
) {
    public static ExperienciaDisponibilidadResponse from(DisponibilidadExperiencia disponibilidad) {
        return new ExperienciaDisponibilidadResponse(
                disponibilidad.getIdDisponibilidad(),
                disponibilidad.getFechaDisponible(),
                disponibilidad.getCuposDisponibles()
        );
    }
}
