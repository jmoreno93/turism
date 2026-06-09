package com.moises.turism.repository;

import com.moises.turism.domain.EstadoReserva;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EstadoReservaRepository extends JpaRepository<EstadoReserva, Integer> {
    Optional<EstadoReserva> findByNombreEstadoIgnoreCase(String nombreEstado);
}
