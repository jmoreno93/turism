package com.moises.turism.repository;

import com.moises.turism.domain.Anfitrion;
import com.moises.turism.enums.EstadoValidacionAnfitrion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AnfitrionRepository extends JpaRepository<Anfitrion, Long> {
    Optional<Anfitrion> findByUsuarioIdUsuario(Long idUsuario);
    List<Anfitrion> findAllByEstadoValidacionOrderByIdAnfitrionDesc(EstadoValidacionAnfitrion estadoValidacion);
    List<Anfitrion> findAllByOrderByIdAnfitrionDesc();
}
