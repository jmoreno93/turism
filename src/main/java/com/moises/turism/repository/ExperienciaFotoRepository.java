package com.moises.turism.repository;

import com.moises.turism.domain.ExperienciaFoto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExperienciaFotoRepository extends JpaRepository<ExperienciaFoto, Long> {
    List<ExperienciaFoto> findAllByExperienciaIdExperienciaOrderByIdFotoAsc(Long idExperiencia);
    void deleteAllByExperienciaIdExperiencia(Long idExperiencia);
}
