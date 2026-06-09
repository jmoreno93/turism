package com.moises.turism.repository;

import com.moises.turism.domain.DisponibilidadExperiencia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DisponibilidadExperienciaRepository extends JpaRepository<DisponibilidadExperiencia, Long> {
    Optional<DisponibilidadExperiencia> findByExperienciaIdExperienciaAndFechaDisponible(Long idExperiencia, LocalDate fechaDisponible);
    List<DisponibilidadExperiencia> findAllByExperienciaIdExperienciaOrderByFechaDisponibleAsc(Long idExperiencia);
    void deleteAllByExperienciaIdExperiencia(Long idExperiencia);
}
