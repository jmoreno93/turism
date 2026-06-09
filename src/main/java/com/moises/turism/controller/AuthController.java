package com.moises.turism.controller;

import com.moises.turism.dto.auth.LoginRequest;
import com.moises.turism.dto.auth.RegistroUsuarioRequest;
import com.moises.turism.dto.auth.UsuarioResponse;
import com.moises.turism.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/registro")
    public ResponseEntity<UsuarioResponse> registrar(@Valid @RequestBody RegistroUsuarioRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registrar(request));
    }

    @PostMapping("/login")
    public ResponseEntity<UsuarioResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PatchMapping("/usuarios/{idUsuario}/verificar-correo")
    public ResponseEntity<UsuarioResponse> verificarCorreo(@PathVariable Long idUsuario) {
        return ResponseEntity.ok(authService.verificarCorreo(idUsuario));
    }
}
