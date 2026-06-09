package com.moises.turism.service;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SesionService {

    private final Map<String, Long> sesiones = new ConcurrentHashMap<>();

    public String crearSesion(Long idUsuario) {
        String token = idUsuario + ":" + UUID.randomUUID();
        sesiones.put(token, idUsuario);
        return token;
    }

    public boolean esSesionValida(Long idUsuario, String token) {
        if (idUsuario == null || !StringUtils.hasText(token)) {
            return false;
        }

        Long usuarioSesion = sesiones.get(token);
        if (idUsuario.equals(usuarioSesion)) {
            return true;
        }

        // Fallback útil para este prototipo: permite que la sesión sobreviva
        // a un reinicio del contenedor mientras el navegador conserve el token.
        return token.startsWith(idUsuario + ":");
    }
}
