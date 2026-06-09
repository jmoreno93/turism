package com.moises.turism.repository;

import com.moises.turism.domain.Interes;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InteresRepository extends JpaRepository<Interes, Integer> {
    Optional<Interes> findByNombreInteresIgnoreCase(String nombreInteres);
    List<Interes> findAllByOrderByNombreInteresAsc();
}
