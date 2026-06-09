package com.moises.turism.dto.anfitrion;

import com.moises.turism.domain.Anfitrion;

import java.time.LocalDateTime;

public record AnfitrionResponse(
        Long idAnfitrion,
        Long idUsuario,
        String nombres,
        String apellidos,
        String email,
        String documentoIdentidad,
        String descripcion,
        String estadoValidacion,
        String estadoCuenta,
        LocalDateTime fechaValidacion
) {
    public static AnfitrionResponse from(Anfitrion anfitrion) {
        return new AnfitrionResponse(
                anfitrion.getIdAnfitrion(),
                anfitrion.getUsuario().getIdUsuario(),
                anfitrion.getUsuario().getNombres(),
                anfitrion.getUsuario().getApellidos(),
                anfitrion.getUsuario().getEmail(),
                anfitrion.getDocumentoIdentidad(),
                anfitrion.getDescripcion(),
                anfitrion.getEstadoValidacion().name(),
                anfitrion.getUsuario().getEstadoCuenta().name(),
                anfitrion.getFechaValidacion()
        );
    }
}
