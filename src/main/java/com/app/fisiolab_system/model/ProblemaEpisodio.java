package com.app.fisiolab_system.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "problemas_episodio")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProblemaEpisodio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "episodio_clinico_id", nullable = false)
    private EpisodioClinico episodioClinico;

    @Column(nullable = false)
    private Integer numeroSecuencial;

    @Column(nullable = false, length = 500)
    private String descripcion;

    @Column(nullable = false, length = 20)
    private String codigoCie10;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoProblemaEpisodio estado;
}
