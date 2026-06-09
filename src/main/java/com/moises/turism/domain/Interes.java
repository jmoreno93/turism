package com.moises.turism.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "intereses")
public class Interes {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_interes")
    private Integer idInteres;

    @Column(name = "nombre_interes", nullable = false, unique = true, length = 50)
    private String nombreInteres;
}
