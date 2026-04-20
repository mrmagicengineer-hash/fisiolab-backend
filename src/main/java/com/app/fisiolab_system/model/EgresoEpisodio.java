package com.app.fisiolab_system.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "egresos_episodio")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EgresoEpisodio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "episodio_clinico_id", nullable = false, unique = true)
    private EpisodioClinico episodioClinico;

    @Column(nullable = false)
    private LocalDateTime fechaHoraEgreso;

    @Column(nullable = false, length = 120)
    private String condicionSalida;

    @Column(nullable = false, length = 500)
    private String causaAlta;

    @Column(nullable = false, length = 160)
    private String destinoPaciente;

    @Column(nullable = false)
    private boolean referidoOtraInstitucion;
}
