package com.moises.turism.dto.notificacion;

import com.moises.turism.domain.Notificacion;

import java.time.LocalDateTime;

public record NotificacionResponse(
        Long idNotificacion,
        Long idUsuario,
        String titulo,
        String mensaje,
        Boolean leida,
        LocalDateTime fechaEnvio
) {
    public static NotificacionResponse from(Notificacion notificacion) {
        return new NotificacionResponse(
                notificacion.getIdNotificacion(),
                notificacion.getUsuario().getIdUsuario(),
                notificacion.getTitulo(),
                notificacion.getMensaje(),
                notificacion.getLeida(),
                notificacion.getFechaEnvio()
        );
    }
}
