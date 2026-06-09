package com.moises.turism.controller;

import com.moises.turism.dto.experiencia.ExperienciaResponse;
import com.moises.turism.service.ExperienciaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/experiencias")
public class ExperienciaController {

    private final ExperienciaService experienciaService;

    @GetMapping("/publicadas")
    public ResponseEntity<List<ExperienciaResponse>> listarPublicadas() {
        return ResponseEntity.ok(experienciaService.listarPublicadas());
    }

    @GetMapping("/{idExperiencia}")
    public ResponseEntity<ExperienciaResponse> obtenerDetalle(@PathVariable Long idExperiencia) {
        return ResponseEntity.ok(experienciaService.obtenerDetalle(idExperiencia));
    }
}
