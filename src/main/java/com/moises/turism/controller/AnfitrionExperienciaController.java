package com.moises.turism.controller;

import com.moises.turism.dto.experiencia.CrearExperienciaRequest;
import com.moises.turism.dto.experiencia.ExperienciaResponse;
import com.moises.turism.service.ExperienciaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/anfitriones/{idAnfitrion}/experiencias")
public class AnfitrionExperienciaController {

    private final ExperienciaService experienciaService;

    @GetMapping
    public ResponseEntity<List<ExperienciaResponse>> listar(@PathVariable Long idAnfitrion) {
        return ResponseEntity.ok(experienciaService.listarPorAnfitrion(idAnfitrion));
    }

    @PostMapping
    public ResponseEntity<ExperienciaResponse> crear(
            @PathVariable Long idAnfitrion,
            @Valid @RequestBody CrearExperienciaRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(experienciaService.crear(idAnfitrion, request));
    }

    @PutMapping("/{idExperiencia}/reenviar")
    public ResponseEntity<ExperienciaResponse> reenviar(
            @PathVariable Long idAnfitrion,
            @PathVariable Long idExperiencia,
            @Valid @RequestBody CrearExperienciaRequest request
    ) {
        return ResponseEntity.ok(experienciaService.reenviar(idAnfitrion, idExperiencia, request));
    }
}
