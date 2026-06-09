package com.moises.turism.controller;

import com.moises.turism.dto.anfitrion.RechazoRequest;
import com.moises.turism.dto.experiencia.ExperienciaResponse;
import com.moises.turism.service.ExperienciaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/experiencias")
public class AdminExperienciaController {

    private final ExperienciaService experienciaService;

    @GetMapping
    public ResponseEntity<List<ExperienciaResponse>> listar(@RequestParam(required = false) String estado) {
        return ResponseEntity.ok(experienciaService.listarAdmin(estado));
    }

    @PatchMapping("/{idExperiencia}/aprobar")
    public ResponseEntity<ExperienciaResponse> aprobar(@PathVariable Long idExperiencia) {
        return ResponseEntity.ok(experienciaService.aprobar(idExperiencia));
    }

    @PatchMapping("/{idExperiencia}/rechazar")
    public ResponseEntity<ExperienciaResponse> rechazar(
            @PathVariable Long idExperiencia,
            @Valid @RequestBody RechazoRequest request
    ) {
        return ResponseEntity.ok(experienciaService.rechazar(idExperiencia, request.motivo()));
    }
}
