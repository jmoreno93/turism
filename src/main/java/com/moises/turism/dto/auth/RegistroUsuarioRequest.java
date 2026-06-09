package com.moises.turism.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record RegistroUsuarioRequest(
        @NotBlank(message = "nombres es requerido")
        @Size(max = 100, message = "nombres no debe superar 100 caracteres")
        String nombres,

        @NotBlank(message = "apellidos es requerido")
        @Size(max = 100, message = "apellidos no debe superar 100 caracteres")
        String apellidos,

        @NotBlank(message = "email es requerido")
        @Email(message = "email no tiene un formato válido")
        @Size(max = 150, message = "email no debe superar 150 caracteres")
        String email,

        @NotBlank(message = "password es requerido")
        @Size(min = 8, max = 72, message = "password debe tener entre 8 y 72 caracteres")
        String password,

        @NotBlank(message = "telefono es requerido")
        @Size(max = 20, message = "telefono no debe superar 20 caracteres")
        String telefono,

        @NotBlank(message = "tipoCuenta es requerido: VIAJERO o ANFITRION")
        String tipoCuenta,

        String documentoIdentidad,
        String descripcionAnfitrion,
        Set<Integer> interesesIds
) {
}
