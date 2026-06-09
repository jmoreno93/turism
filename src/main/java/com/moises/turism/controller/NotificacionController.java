package com.moises.turism.controller;

import com.moises.turism.dto.notificacion.NotificacionResponse;
import com.moises.turism.service.NotificacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notificaciones")
public class NotificacionController {

    private final NotificacionService notificacionService;

    @GetMapping("/usuario/{idUsuario}")
    public ResponseEntity<List<NotificacionResponse>> listarPorUsuario(@PathVariable Long idUsuario) {
        return ResponseEntity.ok(notificacionService.listarPorUsuario(idUsuario));
    }
}
