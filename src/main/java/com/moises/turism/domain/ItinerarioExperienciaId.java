package com.moises.turism.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Embeddable
public class ItinerarioExperienciaId implements Serializable {

    @Column(name = "id_itinerario")
    private Long idItinerario;

    @Column(name = "id_experiencia")
    private Long idExperiencia;
}
