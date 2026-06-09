package com.moises.turism.dto.experiencia;

import com.moises.turism.domain.Experiencia;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record ExperienciaResponse(
        Long idExperiencia,
        Long idAnfitrion,
        String anfitrion,
        Integer idCategoria,
        String categoria,
        String titulo,
        String descripcion,
        String ubicacion,
        BigDecimal precio,
        Integer capacidadMaxima,
        Integer duracionHoras,
        String estadoPublicacion,
        LocalDateTime fechaCreacion,
        List<String> fotosUrls,
        List<ExperienciaDisponibilidadResponse> disponibilidades
) {
    public static ExperienciaResponse from(
            Experiencia experiencia,
            List<String> fotosUrls,
            List<ExperienciaDisponibilidadResponse> disponibilidades
    ) {
        return new ExperienciaResponse(
                experiencia.getIdExperiencia(),
                experiencia.getAnfitrion().getIdAnfitrion(),
                experiencia.getAnfitrion().getUsuario().getNombres() + " " + experiencia.getAnfitrion().getUsuario().getApellidos(),
                experiencia.getCategoria().getIdCategoria(),
                experiencia.getCategoria().getNombreCategoria(),
                experiencia.getTitulo(),
                experiencia.getDescripcion(),
                experiencia.getUbicacion(),
                experiencia.getPrecio(),
                experiencia.getCapacidadMaxima(),
                experiencia.getDuracionHoras(),
                experiencia.getEstadoPublicacion().name(),
                experiencia.getFechaCreacion(),
                fotosUrls,
                disponibilidades
        );
    }
}
