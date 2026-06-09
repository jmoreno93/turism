package com.moises.turism.domain;

import com.moises.turism.enums.EstadoValidacionAnfitrion;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "anfitriones")
public class Anfitrion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_anfitrion")
    private Long idAnfitrion;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", nullable = false, unique = true)
    private Usuario usuario;

    @Column(name = "documento_identidad", nullable = false, length = 30)
    private String documentoIdentidad;

    @Column(name = "descripcion", nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_validacion", nullable = false, length = 20)
    private EstadoValidacionAnfitrion estadoValidacion;

    @Column(name = "fecha_validacion")
    private LocalDateTime fechaValidacion;
}
