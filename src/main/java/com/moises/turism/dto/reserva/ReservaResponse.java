package com.moises.turism.dto.reserva;

import com.moises.turism.domain.Reserva;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ReservaResponse(
        Long idReserva,
        Long idUsuario,
        String viajero,
        Long idExperiencia,
        String experiencia,
        Long idAnfitrion,
        String estado,
        LocalDateTime fechaReserva,
        LocalDate fechaExperiencia,
        Integer cantidadPersonas,
        String observaciones
) {
    public static ReservaResponse from(Reserva reserva) {
        return new ReservaResponse(
                reserva.getIdReserva(),
                reserva.getUsuario().getIdUsuario(),
                reserva.getUsuario().getNombres() + " " + reserva.getUsuario().getApellidos(),
                reserva.getExperiencia().getIdExperiencia(),
                reserva.getExperiencia().getTitulo(),
                reserva.getExperiencia().getAnfitrion().getIdAnfitrion(),
                reserva.getEstado().getNombreEstado(),
                reserva.getFechaReserva(),
                reserva.getFechaExperiencia(),
                reserva.getCantidadPersonas(),
                reserva.getObservaciones()
        );
    }
}
