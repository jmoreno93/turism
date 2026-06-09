package com.moises.turism.repository;

import com.moises.turism.domain.Rol;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RolRepository extends JpaRepository<Rol, Integer> {
    Optional<Rol> findByNombreRolIgnoreCase(String nombreRol);
}
