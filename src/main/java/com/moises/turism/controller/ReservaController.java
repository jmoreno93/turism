package com.moises.turism.controller;

import com.moises.turism.common.exception.ApiException;
import com.moises.turism.dto.reserva.CrearReservaRequest;
import com.moises.turism.dto.reserva.ReservaResponse;
import com.moises.turism.service.ReservaService;
import com.moises.turism.service.SesionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/reservas")
public class ReservaController {

    private final ReservaService reservaService;
    private final SesionService sesionService;

    @PostMapping
    public ResponseEntity<ReservaResponse> crear(
            @RequestHeader(value = "X-Session-User", required = false) String idUsuarioSesion,
            @RequestHeader(value = "X-Session-Token", required = false) String sessionToken,
            @Valid @RequestBody CrearReservaRequest request
    ) {
        validarSesionReserva(idUsuarioSesion, sessionToken, request.idUsuario());
        return ResponseEntity.status(HttpStatus.CREATED).body(reservaService.crear(request));
    }

    @GetMapping("/usuario/{idUsuario}")
    public ResponseEntity<List<ReservaResponse>> listarPorUsuario(
            @RequestHeader(value = "X-Session-User", required = false) String idUsuarioSesion,
            @RequestHeader(value = "X-Session-Token", required = false) String sessionToken,
            @PathVariable Long idUsuario
    ) {
        validarSesionReserva(idUsuarioSesion, sessionToken, idUsuario);
        return ResponseEntity.ok(reservaService.listarPorUsuario(idUsuario));
    }

    private void validarSesionReserva(String idUsuarioSesionHeader, String sessionToken, Long idUsuarioRequest) {
        Long idUsuarioSesion = parseIdUsuarioSesion(idUsuarioSesionHeader);
        if (idUsuarioSesion == null
                || idUsuarioRequest == null
                || !idUsuarioSesion.equals(idUsuarioRequest)
                || !sesionService.esSesionValida(idUsuarioSesion, sessionToken)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Debes iniciar sesión como viajero para registrar o consultar reservas.");
        }
    }

    private Long parseIdUsuarioSesion(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Long.valueOf(value.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
