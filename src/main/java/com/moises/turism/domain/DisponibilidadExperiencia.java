package com.moises.turism.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "disponibilidad_experiencia",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_disponibilidad_experiencia_fecha",
                columnNames = {"id_experiencia", "fecha_disponible"}
        )
)
public class DisponibilidadExperiencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_disponibilidad")
    private Long idDisponibilidad;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_experiencia", nullable = false)
    private Experiencia experiencia;

    @Column(name = "fecha_disponible", nullable = false)
    private LocalDate fechaDisponible;

    @Column(name = "cupos_disponibles", nullable = false)
    private Integer cuposDisponibles;
}
