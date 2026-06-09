package com.moises.turism.repository;

import com.moises.turism.domain.Experiencia;
import com.moises.turism.enums.EstadoPublicacionExperiencia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExperienciaRepository extends JpaRepository<Experiencia, Long> {
    List<Experiencia> findAllByEstadoPublicacionOrderByFechaCreacionDesc(EstadoPublicacionExperiencia estadoPublicacion);
    List<Experiencia> findAllByAnfitrionIdAnfitrionOrderByFechaCreacionDesc(Long idAnfitrion);
    List<Experiencia> findAllByOrderByFechaCreacionDesc();
}
