package com.moises.turism.repository;

import com.moises.turism.domain.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {
    List<Notificacion> findAllByUsuarioIdUsuarioOrderByFechaEnvioDesc(Long idUsuario);
}
