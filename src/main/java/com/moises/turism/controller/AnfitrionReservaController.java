package com.moises.turism.controller;

import com.moises.turism.dto.reserva.ReservaResponse;
import com.moises.turism.service.ReservaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/anfitriones/{idAnfitrion}/reservas")
public class AnfitrionReservaController {

    private final ReservaService reservaService;

    @GetMapping
    public ResponseEntity<List<ReservaResponse>> listar(@PathVariable Long idAnfitrion) {
        return ResponseEntity.ok(reservaService.listarPorAnfitrion(idAnfitrion));
    }

    @PatchMapping("/{idReserva}/aceptar")
    public ResponseEntity<ReservaResponse> aceptar(
            @PathVariable Long idAnfitrion,
            @PathVariable Long idReserva
    ) {
        return ResponseEntity.ok(reservaService.aceptar(idAnfitrion, idReserva));
    }

    @PatchMapping("/{idReserva}/rechazar")
    public ResponseEntity<ReservaResponse> rechazar(
            @PathVariable Long idAnfitrion,
            @PathVariable Long idReserva
    ) {
        return ResponseEntity.ok(reservaService.rechazar(idAnfitrion, idReserva));
    }
}
