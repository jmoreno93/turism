package com.moises.turism.dto.auth;

import com.moises.turism.domain.Anfitrion;
import com.moises.turism.domain.Usuario;

public record UsuarioResponse(
        Long idUsuario,
        Long idAnfitrion,
        String nombres,
        String apellidos,
        String email,
        String rol,
        Boolean correoVerificado,
        String estadoCuenta,
        String sessionToken
) {
    public static UsuarioResponse from(Usuario usuario, Anfitrion anfitrion) {
        return from(usuario, anfitrion, null);
    }

    public static UsuarioResponse from(Usuario usuario, Anfitrion anfitrion, String sessionToken) {
        return new UsuarioResponse(
                usuario.getIdUsuario(),
                anfitrion == null ? null : anfitrion.getIdAnfitrion(),
                usuario.getNombres(),
                usuario.getApellidos(),
                usuario.getEmail(),
                usuario.getRol().getNombreRol(),
                usuario.getCorreoVerificado(),
                usuario.getEstadoCuenta().name(),
                sessionToken
        );
    }
}
