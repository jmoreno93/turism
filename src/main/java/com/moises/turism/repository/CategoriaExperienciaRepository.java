package com.moises.turism.repository;

import com.moises.turism.domain.CategoriaExperiencia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoriaExperienciaRepository extends JpaRepository<CategoriaExperiencia, Integer> {
    Optional<CategoriaExperiencia> findByNombreCategoriaIgnoreCase(String nombreCategoria);
    List<CategoriaExperiencia> findAllByOrderByNombreCategoriaAsc();
}
