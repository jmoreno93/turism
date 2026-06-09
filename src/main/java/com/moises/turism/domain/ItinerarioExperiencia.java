package com.moises.turism.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "itinerario_experiencia")
public class ItinerarioExperiencia {

    @EmbeddedId
    private ItinerarioExperienciaId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("idItinerario")
    @JoinColumn(name = "id_itinerario", nullable = false)
    private Itinerario itinerario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("idExperiencia")
    @JoinColumn(name = "id_experiencia", nullable = false)
    private Experiencia experiencia;

    @Column(name = "orden_actividad", nullable = false)
    private Integer ordenActividad;
}
