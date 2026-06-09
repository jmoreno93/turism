package com.moises.turism.controller;

import com.moises.turism.dto.anfitrion.AnfitrionResponse;
import com.moises.turism.dto.anfitrion.RechazoRequest;
import com.moises.turism.dto.auth.RegistroUsuarioRequest;
import com.moises.turism.dto.auth.UsuarioResponse;
import com.moises.turism.service.AnfitrionService;
import com.moises.turism.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/anfitriones")
public class AdminAnfitrionController {

    private final AnfitrionService anfitrionService;
    private final AuthService authService;

    @GetMapping
    public ResponseEntity<List<AnfitrionResponse>> listar(@RequestParam(required = false) String estado) {
        return ResponseEntity.ok(anfitrionService.listar(estado));
    }

    @PostMapping("/registrar")
    public ResponseEntity<UsuarioResponse> registrarDesdeAdmin(@Valid @RequestBody RegistroUsuarioRequest request) {
        return ResponseEntity.ok(authService.registrarAnfitrionDesdeAdmin(request));
    }

    @PatchMapping("/{idAnfitrion}/aprobar")
    public ResponseEntity<AnfitrionResponse> aprobar(@PathVariable Long idAnfitrion) {
        return ResponseEntity.ok(anfitrionService.aprobar(idAnfitrion));
    }

    @PatchMapping("/{idAnfitrion}/rechazar")
    public ResponseEntity<AnfitrionResponse> rechazar(
            @PathVariable Long idAnfitrion,
            @Valid @RequestBody RechazoRequest request
    ) {
        return ResponseEntity.ok(anfitrionService.rechazar(idAnfitrion, request.motivo()));
    }
}
