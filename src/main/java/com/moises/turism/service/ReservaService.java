package com.moises.turism.service;

import com.moises.turism.common.ConstantesCatalogo;
import com.moises.turism.common.exception.ApiException;
import com.moises.turism.domain.*;
import com.moises.turism.dto.reserva.CrearReservaRequest;
import com.moises.turism.dto.reserva.ReservaResponse;
import com.moises.turism.enums.EstadoCuenta;
import com.moises.turism.enums.EstadoPublicacionExperiencia;
import com.moises.turism.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservaService {

    private final UsuarioRepository usuarioRepository;
    private final ExperienciaRepository experienciaRepository;
    private final EstadoReservaRepository estadoReservaRepository;
    private final ReservaRepository reservaRepository;
    private final DisponibilidadExperienciaRepository disponibilidadExperienciaRepository;
    private final NotificacionService notificacionService;

    @Transactional
    public ReservaResponse crear(CrearReservaRequest request) {
        Usuario viajero = usuarioRepository.findById(request.idUsuario())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Viajero no encontrado."));
        validarViajeroActivo(viajero);

        Experiencia experiencia = experienciaRepository.findById(request.idExperiencia())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Experiencia no encontrada."));
        validarExperienciaPublicada(experiencia);
        validarFechaReserva(request.fechaExperiencia());
        validarCantidadPersonas(request.cantidadPersonas());
        validarCuposDisponibles(experiencia.getIdExperiencia(), request.fechaExperiencia(), request.cantidadPersonas());

        EstadoReserva estadoPendiente = obtenerEstadoReserva(ConstantesCatalogo.ESTADO_RESERVA_PENDIENTE);
        Reserva reserva = new Reserva();
        reserva.setUsuario(viajero);
        reserva.setExperiencia(experiencia);
        reserva.setEstado(estadoPendiente);
        reserva.setFechaExperiencia(request.fechaExperiencia());
        reserva.setCantidadPersonas(request.cantidadPersonas());
        reserva.setObservaciones(request.observaciones());
        reservaRepository.save(reserva);

        try {
            notificacionService.notificarUsuario(
                    experiencia.getAnfitrion().getUsuario(),
                    "Nueva solicitud de reserva",
                    "El viajero " + viajero.getEmail() + " solicitó reservar '" + experiencia.getTitulo() + "'."
            );
        } catch (RuntimeException ignored) {
            // La reserva no debe fallar por un problema secundario de notificación en el prototipo.
        }

        return ReservaResponse.from(reserva);
    }

    @Transactional
    public ReservaResponse aceptar(Long idAnfitrion, Long idReserva) {
        Reserva reserva = obtenerReservaDelAnfitrion(idAnfitrion, idReserva);
        validarReservaPendiente(reserva);

        DisponibilidadExperiencia disponibilidad = validarCuposDisponibles(
                reserva.getExperiencia().getIdExperiencia(),
                reserva.getFechaExperiencia(),
                reserva.getCantidadPersonas()
        );
        disponibilidad.setCuposDisponibles(disponibilidad.getCuposDisponibles() - reserva.getCantidadPersonas());
        disponibilidadExperienciaRepository.save(disponibilidad);

        reserva.setEstado(obtenerEstadoReserva(ConstantesCatalogo.ESTADO_RESERVA_CONFIRMADA));
        reservaRepository.save(reserva);

        notificacionService.notificarUsuario(
                reserva.getUsuario(),
                "Reserva confirmada",
                "Tu reserva para '" + reserva.getExperiencia().getTitulo() + "' fue confirmada."
        );

        return ReservaResponse.from(reserva);
    }

    @Transactional
    public ReservaResponse rechazar(Long idAnfitrion, Long idReserva) {
        Reserva reserva = obtenerReservaDelAnfitrion(idAnfitrion, idReserva);
        validarReservaPendiente(reserva);
        reserva.setEstado(obtenerEstadoReserva(ConstantesCatalogo.ESTADO_RESERVA_RECHAZADA));
        reservaRepository.save(reserva);

        notificacionService.notificarUsuario(
                reserva.getUsuario(),
                "Reserva rechazada",
                "Tu reserva para '" + reserva.getExperiencia().getTitulo() + "' fue rechazada."
        );

        return ReservaResponse.from(reserva);
    }

    @Transactional(readOnly = true)
    public List<ReservaResponse> listarPorUsuario(Long idUsuario) {
        return reservaRepository.findAllByUsuarioIdUsuarioOrderByFechaReservaDesc(idUsuario)
                .stream()
                .map(ReservaResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReservaResponse> listarPorAnfitrion(Long idAnfitrion) {
        return reservaRepository.findAllByExperienciaAnfitrionIdAnfitrionOrderByFechaReservaDesc(idAnfitrion)
                .stream()
                .map(ReservaResponse::from)
                .toList();
    }

    private Reserva obtenerReservaDelAnfitrion(Long idAnfitrion, Long idReserva) {
        Reserva reserva = reservaRepository.findById(idReserva)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Reserva no encontrada."));
        if (!reserva.getExperiencia().getAnfitrion().getIdAnfitrion().equals(idAnfitrion)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "La reserva no pertenece al anfitrión indicado.");
        }
        return reserva;
    }

    private void validarViajeroActivo(Usuario viajero) {
        if (!ConstantesCatalogo.ROL_VIAJERO.equalsIgnoreCase(viajero.getRol().getNombreRol())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Solo un usuario viajero puede solicitar una reserva.");
        }
        if (viajero.getEstadoCuenta() != EstadoCuenta.ACTIVA || !Boolean.TRUE.equals(viajero.getCorreoVerificado())) {
            throw new ApiException(HttpStatus.CONFLICT, "La cuenta del viajero debe estar activa y con correo verificado.");
        }
    }

    private void validarExperienciaPublicada(Experiencia experiencia) {
        if (experiencia.getEstadoPublicacion() != EstadoPublicacionExperiencia.PUBLICADA) {
            throw new ApiException(HttpStatus.CONFLICT, "La experiencia debe estar publicada para poder reservarse.");
        }
    }

    private void validarFechaReserva(LocalDate fechaExperiencia) {
        if (fechaExperiencia == null || !fechaExperiencia.isAfter(LocalDate.now())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "La fecha de experiencia debe ser desde mañana en adelante.");
        }
    }

    private void validarCantidadPersonas(Integer cantidadPersonas) {
        if (cantidadPersonas == null || cantidadPersonas < 1) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "La cantidad de personas debe ser mayor o igual a 1.");
        }
    }

    private DisponibilidadExperiencia validarCuposDisponibles(Long idExperiencia, java.time.LocalDate fecha, Integer cantidadPersonas) {
        DisponibilidadExperiencia disponibilidad = disponibilidadExperienciaRepository
                .findByExperienciaIdExperienciaAndFechaDisponible(idExperiencia, fecha)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "La experiencia no tiene disponibilidad en la fecha seleccionada."));

        if (disponibilidad.getCuposDisponibles() < cantidadPersonas) {
            throw new ApiException(HttpStatus.CONFLICT, "No hay cupos suficientes para la cantidad de personas solicitada.");
        }
        return disponibilidad;
    }

    private void validarReservaPendiente(Reserva reserva) {
        if (!ConstantesCatalogo.ESTADO_RESERVA_PENDIENTE.equalsIgnoreCase(reserva.getEstado().getNombreEstado())) {
            throw new ApiException(HttpStatus.CONFLICT, "La reserva ya fue atendida.");
        }
    }

    private EstadoReserva obtenerEstadoReserva(String nombreEstado) {
        return estadoReservaRepository.findByNombreEstadoIgnoreCase(nombreEstado)
                .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "No existe el estado de reserva configurado: " + nombreEstado));
    }
}
