package com.moises.turism.repository;

import com.moises.turism.domain.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {
    List<Reserva> findAllByUsuarioIdUsuarioOrderByFechaReservaDesc(Long idUsuario);
    List<Reserva> findAllByExperienciaAnfitrionIdAnfitrionOrderByFechaReservaDesc(Long idAnfitrion);
}
