package com.moises.turism.service;

import com.moises.turism.common.exception.ApiException;
import com.moises.turism.domain.*;
import com.moises.turism.dto.experiencia.CrearExperienciaRequest;
import com.moises.turism.dto.experiencia.DisponibilidadRequest;
import com.moises.turism.dto.experiencia.ExperienciaDisponibilidadResponse;
import com.moises.turism.dto.experiencia.ExperienciaResponse;
import com.moises.turism.enums.EstadoCuenta;
import com.moises.turism.enums.EstadoPublicacionExperiencia;
import com.moises.turism.enums.EstadoValidacionAnfitrion;
import com.moises.turism.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ExperienciaService {

    private final AnfitrionRepository anfitrionRepository;
    private final CategoriaExperienciaRepository categoriaExperienciaRepository;
    private final ExperienciaRepository experienciaRepository;
    private final ExperienciaFotoRepository experienciaFotoRepository;
    private final DisponibilidadExperienciaRepository disponibilidadExperienciaRepository;
    private final NotificacionService notificacionService;

    @Transactional
    public ExperienciaResponse crear(Long idAnfitrion, CrearExperienciaRequest request) {
        Anfitrion anfitrion = obtenerAnfitrionHabilitado(idAnfitrion);
        CategoriaExperiencia categoria = categoriaExperienciaRepository.findById(request.idCategoria())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Categoría de experiencia no encontrada."));
        validarFechasDuplicadas(request.disponibilidades());

        Experiencia experiencia = construirExperienciaNueva(anfitrion, categoria, request);
        experienciaRepository.save(experiencia);
        registrarFotos(experiencia, request.fotosUrls());
        registrarDisponibilidades(experiencia, request.disponibilidades());

        notificacionService.notificarAdministradores(
                "Experiencia pendiente de revisión",
                "La experiencia '" + experiencia.getTitulo() + "' fue enviada para validación."
        );

        return mapearRespuesta(experiencia);
    }

    @Transactional
    public ExperienciaResponse aprobar(Long idExperiencia) {
        Experiencia experiencia = obtenerExperiencia(idExperiencia);
        validarEstadoPendiente(experiencia);
        experiencia.setEstadoPublicacion(EstadoPublicacionExperiencia.PUBLICADA);
        experienciaRepository.save(experiencia);

        notificacionService.notificarUsuario(
                experiencia.getAnfitrion().getUsuario(),
                "Experiencia publicada",
                "La experiencia '" + experiencia.getTitulo() + "' fue aprobada y publicada."
        );

        return mapearRespuesta(experiencia);
    }

    @Transactional
    public ExperienciaResponse rechazar(Long idExperiencia, String motivo) {
        Experiencia experiencia = obtenerExperiencia(idExperiencia);
        validarEstadoPendiente(experiencia);
        experiencia.setEstadoPublicacion(EstadoPublicacionExperiencia.RECHAZADA);
        experienciaRepository.save(experiencia);

        notificacionService.notificarUsuario(
                experiencia.getAnfitrion().getUsuario(),
                "Experiencia observada",
                "La experiencia '" + experiencia.getTitulo() + "' fue rechazada. Motivo: " + motivo
        );

        return mapearRespuesta(experiencia);
    }

    @Transactional
    public ExperienciaResponse reenviar(Long idAnfitrion, Long idExperiencia, CrearExperienciaRequest request) {
        Anfitrion anfitrion = obtenerAnfitrionHabilitado(idAnfitrion);
        Experiencia experiencia = obtenerExperiencia(idExperiencia);
        if (!experiencia.getAnfitrion().getIdAnfitrion().equals(anfitrion.getIdAnfitrion())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "La experiencia no pertenece al anfitrión indicado.");
        }
        if (experiencia.getEstadoPublicacion() != EstadoPublicacionExperiencia.RECHAZADA) {
            throw new ApiException(HttpStatus.CONFLICT, "Solo se puede reenviar una experiencia rechazada.");
        }

        CategoriaExperiencia categoria = categoriaExperienciaRepository.findById(request.idCategoria())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Categoría de experiencia no encontrada."));
        validarFechasDuplicadas(request.disponibilidades());

        actualizarExperiencia(experiencia, categoria, request);
        experiencia.setEstadoPublicacion(EstadoPublicacionExperiencia.PENDIENTE);
        experienciaRepository.save(experiencia);

        experienciaFotoRepository.deleteAllByExperienciaIdExperiencia(idExperiencia);
        disponibilidadExperienciaRepository.deleteAllByExperienciaIdExperiencia(idExperiencia);
        registrarFotos(experiencia, request.fotosUrls());
        registrarDisponibilidades(experiencia, request.disponibilidades());

        notificacionService.notificarAdministradores(
                "Experiencia reenviada",
                "La experiencia '" + experiencia.getTitulo() + "' fue corregida y requiere nueva revisión."
        );

        return mapearRespuesta(experiencia);
    }

    @Transactional(readOnly = true)
    public List<ExperienciaResponse> listarPublicadas() {
        return experienciaRepository.findAllByEstadoPublicacionOrderByFechaCreacionDesc(EstadoPublicacionExperiencia.PUBLICADA)
                .stream()
                .map(this::mapearRespuesta)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ExperienciaResponse> listarPorAnfitrion(Long idAnfitrion) {
        if (!anfitrionRepository.existsById(idAnfitrion)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Anfitrión no encontrado.");
        }
        return experienciaRepository.findAllByAnfitrionIdAnfitrionOrderByFechaCreacionDesc(idAnfitrion)
                .stream()
                .map(this::mapearRespuesta)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ExperienciaResponse> listarAdmin(String estado) {
        List<Experiencia> experiencias;
        if (estado == null || estado.isBlank()) {
            experiencias = experienciaRepository.findAllByOrderByFechaCreacionDesc();
        } else {
            EstadoPublicacionExperiencia estadoPublicacion = parsearEstadoPublicacion(estado);
            experiencias = experienciaRepository.findAllByEstadoPublicacionOrderByFechaCreacionDesc(estadoPublicacion);
        }
        return experiencias.stream()
                .map(this::mapearRespuesta)
                .toList();
    }

    @Transactional(readOnly = true)
    public ExperienciaResponse obtenerDetalle(Long idExperiencia) {
        return mapearRespuesta(obtenerExperiencia(idExperiencia));
    }

    private EstadoPublicacionExperiencia parsearEstadoPublicacion(String estado) {
        try {
            return EstadoPublicacionExperiencia.valueOf(estado.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "estado debe ser PENDIENTE, PUBLICADA o RECHAZADA.");
        }
    }

    private Experiencia construirExperienciaNueva(Anfitrion anfitrion, CategoriaExperiencia categoria, CrearExperienciaRequest request) {
        Experiencia experiencia = new Experiencia();
        experiencia.setAnfitrion(anfitrion);
        experiencia.setCategoria(categoria);
        experiencia.setEstadoPublicacion(EstadoPublicacionExperiencia.PENDIENTE);
        actualizarExperiencia(experiencia, categoria, request);
        return experiencia;
    }

    private void actualizarExperiencia(Experiencia experiencia, CategoriaExperiencia categoria, CrearExperienciaRequest request) {
        experiencia.setCategoria(categoria);
        experiencia.setTitulo(request.titulo().trim());
        experiencia.setDescripcion(request.descripcion().trim());
        experiencia.setUbicacion(request.ubicacion().trim());
        experiencia.setPrecio(request.precio());
        experiencia.setCapacidadMaxima(request.capacidadMaxima());
        experiencia.setDuracionHoras(request.duracionHoras());
    }

    private void registrarFotos(Experiencia experiencia, List<String> fotosUrls) {
        for (String url : fotosUrls) {
            ExperienciaFoto foto = new ExperienciaFoto();
            foto.setExperiencia(experiencia);
            foto.setUrlFoto(url.trim());
            experienciaFotoRepository.save(foto);
        }
    }

    private void registrarDisponibilidades(Experiencia experiencia, List<DisponibilidadRequest> disponibilidades) {
        for (DisponibilidadRequest request : disponibilidades) {
            DisponibilidadExperiencia disponibilidad = new DisponibilidadExperiencia();
            disponibilidad.setExperiencia(experiencia);
            disponibilidad.setFechaDisponible(request.fechaDisponible());
            disponibilidad.setCuposDisponibles(request.cuposDisponibles());
            disponibilidadExperienciaRepository.save(disponibilidad);
        }
    }

    private void validarFechasDuplicadas(List<DisponibilidadRequest> disponibilidades) {
        Set<Object> fechas = new HashSet<>();
        for (DisponibilidadRequest disponibilidad : disponibilidades) {
            if (!fechas.add(disponibilidad.fechaDisponible())) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "No se permiten disponibilidades repetidas para la misma fecha.");
            }
        }
    }

    private Anfitrion obtenerAnfitrionHabilitado(Long idAnfitrion) {
        Anfitrion anfitrion = anfitrionRepository.findById(idAnfitrion)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Anfitrión no encontrado."));

        if (anfitrion.getEstadoValidacion() != EstadoValidacionAnfitrion.APROBADO
                || anfitrion.getUsuario().getEstadoCuenta() != EstadoCuenta.ACTIVA) {
            throw new ApiException(HttpStatus.CONFLICT, "El anfitrión debe estar aprobado y activo para publicar experiencias.");
        }
        return anfitrion;
    }

    private Experiencia obtenerExperiencia(Long idExperiencia) {
        return experienciaRepository.findById(idExperiencia)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Experiencia no encontrada."));
    }

    private void validarEstadoPendiente(Experiencia experiencia) {
        if (experiencia.getEstadoPublicacion() != EstadoPublicacionExperiencia.PENDIENTE) {
            throw new ApiException(HttpStatus.CONFLICT, "La experiencia no está pendiente de revisión.");
        }
    }

    private ExperienciaResponse mapearRespuesta(Experiencia experiencia) {
        List<String> fotosUrls = experienciaFotoRepository.findAllByExperienciaIdExperienciaOrderByIdFotoAsc(experiencia.getIdExperiencia())
                .stream()
                .map(ExperienciaFoto::getUrlFoto)
                .toList();
        List<ExperienciaDisponibilidadResponse> disponibilidades = disponibilidadExperienciaRepository.findAllByExperienciaIdExperienciaOrderByFechaDisponibleAsc(experiencia.getIdExperiencia())
                .stream()
                .map(ExperienciaDisponibilidadResponse::from)
                .toList();
        return ExperienciaResponse.from(experiencia, fotosUrls, disponibilidades);
    }
}
