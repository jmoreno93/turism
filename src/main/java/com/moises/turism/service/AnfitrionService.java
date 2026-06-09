package com.moises.turism.service;

import com.moises.turism.common.exception.ApiException;
import com.moises.turism.domain.Anfitrion;
import com.moises.turism.domain.Usuario;
import com.moises.turism.dto.anfitrion.AnfitrionResponse;
import com.moises.turism.dto.anfitrion.ReenviarValidacionAnfitrionRequest;
import com.moises.turism.enums.EstadoCuenta;
import com.moises.turism.enums.EstadoValidacionAnfitrion;
import com.moises.turism.repository.AnfitrionRepository;
import com.moises.turism.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AnfitrionService {

    private final AnfitrionRepository anfitrionRepository;
    private final UsuarioRepository usuarioRepository;
    private final NotificacionService notificacionService;

    @Transactional
    public AnfitrionResponse aprobar(Long idAnfitrion) {
        Anfitrion anfitrion = obtenerAnfitrion(idAnfitrion);
        anfitrion.setEstadoValidacion(EstadoValidacionAnfitrion.APROBADO);
        anfitrion.setFechaValidacion(LocalDateTime.now());

        Usuario usuario = anfitrion.getUsuario();
        usuario.setEstadoCuenta(EstadoCuenta.ACTIVA);
        usuarioRepository.save(usuario);
        anfitrionRepository.save(anfitrion);

        notificacionService.notificarUsuario(
                usuario,
                "Perfil aprobado",
                "Tu perfil de anfitrión fue aprobado y ya puedes publicar experiencias."
        );

        return AnfitrionResponse.from(anfitrion);
    }

    @Transactional
    public AnfitrionResponse rechazar(Long idAnfitrion, String motivo) {
        Anfitrion anfitrion = obtenerAnfitrion(idAnfitrion);
        anfitrion.setEstadoValidacion(EstadoValidacionAnfitrion.RECHAZADO);
        anfitrion.setFechaValidacion(LocalDateTime.now());

        Usuario usuario = anfitrion.getUsuario();
        usuario.setEstadoCuenta(EstadoCuenta.RECHAZADA);
        usuarioRepository.save(usuario);
        anfitrionRepository.save(anfitrion);

        notificacionService.notificarUsuario(
                usuario,
                "Perfil rechazado",
                "Tu perfil de anfitrión fue observado. Motivo: " + motivo
        );

        return AnfitrionResponse.from(anfitrion);
    }

    @Transactional
    public AnfitrionResponse reenviarValidacion(Long idAnfitrion, ReenviarValidacionAnfitrionRequest request) {
        Anfitrion anfitrion = obtenerAnfitrion(idAnfitrion);
        if (anfitrion.getEstadoValidacion() != EstadoValidacionAnfitrion.RECHAZADO) {
            throw new ApiException(HttpStatus.CONFLICT, "Solo se puede reenviar un perfil previamente rechazado.");
        }

        anfitrion.setDocumentoIdentidad(request.documentoIdentidad().trim());
        anfitrion.setDescripcion(request.descripcion().trim());
        anfitrion.setEstadoValidacion(EstadoValidacionAnfitrion.PENDIENTE);
        anfitrion.setFechaValidacion(null);

        Usuario usuario = anfitrion.getUsuario();
        usuario.setEstadoCuenta(EstadoCuenta.EN_REVISION);
        usuarioRepository.save(usuario);
        anfitrionRepository.save(anfitrion);

        notificacionService.notificarAdministradores(
                "Revisión reenviada",
                "El anfitrión " + usuario.getEmail() + " corrigió su información y requiere una nueva validación."
        );

        return AnfitrionResponse.from(anfitrion);
    }

    @Transactional(readOnly = true)
    public List<AnfitrionResponse> listar(String estado) {
        List<Anfitrion> anfitriones;
        if (estado == null || estado.isBlank()) {
            anfitriones = anfitrionRepository.findAllByOrderByIdAnfitrionDesc();
        } else {
            EstadoValidacionAnfitrion estadoValidacion = parsearEstadoValidacion(estado);
            anfitriones = anfitrionRepository.findAllByEstadoValidacionOrderByIdAnfitrionDesc(estadoValidacion);
        }
        return anfitriones.stream()
                .map(AnfitrionResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public AnfitrionResponse obtener(Long idAnfitrion) {
        return AnfitrionResponse.from(obtenerAnfitrion(idAnfitrion));
    }

    private EstadoValidacionAnfitrion parsearEstadoValidacion(String estado) {
        try {
            return EstadoValidacionAnfitrion.valueOf(estado.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "estado debe ser PENDIENTE, APROBADO o RECHAZADO.");
        }
    }

    private Anfitrion obtenerAnfitrion(Long idAnfitrion) {
        return anfitrionRepository.findById(idAnfitrion)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Anfitrión no encontrado."));
    }
}
