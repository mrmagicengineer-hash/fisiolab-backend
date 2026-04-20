package com.app.fisiolab_system.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "episodios_clinicos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EpisodioClinico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "historia_clinica_id", nullable = false)
    private HistoriaClinica historiaClinica;

    @Column(nullable = false)
    private Integer numeroSecuencial;

    @Column(nullable = false, length = 30)
    private String numeroEpisodio;

    @Column(nullable = false, length = 500)
    private String motivoConsulta;

    @Column(nullable = false)
    private LocalDateTime fechaApertura;

    private LocalDateTime fechaCierre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoEpisodioClinico estado;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private EstadoCierreEpisodio estadoCierre;

    @Column(length = 500)
    private String observacionCierre;

    @PrePersist
    public void prePersist() {
        if (fechaApertura == null) {
            fechaApertura = LocalDateTime.now();
        }
        if (estado == null) {
            estado = EstadoEpisodioClinico.ABIERTO;
        }
    }
}
