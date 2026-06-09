package com.moises.turism.controller;

import com.moises.turism.dto.anfitrion.AnfitrionResponse;
import com.moises.turism.dto.anfitrion.ReenviarValidacionAnfitrionRequest;
import com.moises.turism.service.AnfitrionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/anfitriones")
public class AnfitrionController {

    private final AnfitrionService anfitrionService;

    @GetMapping("/{idAnfitrion}")
    public ResponseEntity<AnfitrionResponse> obtener(@PathVariable Long idAnfitrion) {
        return ResponseEntity.ok(anfitrionService.obtener(idAnfitrion));
    }

    @PatchMapping("/{idAnfitrion}/reenviar-validacion")
    public ResponseEntity<AnfitrionResponse> reenviarValidacion(
            @PathVariable Long idAnfitrion,
            @Valid @RequestBody ReenviarValidacionAnfitrionRequest request
    ) {
        return ResponseEntity.ok(anfitrionService.reenviarValidacion(idAnfitrion, request));
    }
}
