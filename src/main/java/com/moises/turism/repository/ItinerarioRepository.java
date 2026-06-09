package com.moises.turism.repository;

import com.moises.turism.domain.Itinerario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItinerarioRepository extends JpaRepository<Itinerario, Long> {
}
