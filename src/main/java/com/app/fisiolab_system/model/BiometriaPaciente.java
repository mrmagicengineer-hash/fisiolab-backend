package com.app.fisiolab_system.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "biometria_paciente")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BiometriaPaciente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "episodio_clinico_id", nullable = false)
    private EpisodioClinico episodioClinico;

    @Column(nullable = false)
    private LocalDateTime fechaRegistro;

    @Column(nullable = false, precision = 6, scale = 2)
    private BigDecimal pesoKg;

    @Column(nullable = false, precision = 6, scale = 2)
    private BigDecimal tallaCm;

    @Column(nullable = false, precision = 6, scale = 2)
    private BigDecimal imcCalculado;

    @Column(precision = 6, scale = 2)
    private BigDecimal perimetroCinturaCm;

    @Column(precision = 6, scale = 2)
    private BigDecimal perimetroCaderaCm;

    private Integer presionArterialSistolica;

    private Integer presionArterialDiastolica;

    private Integer frecuenciaCardiaca;

    private Integer frecuenciaRespiratoria;

    @Column(precision = 4, scale = 1)
    private BigDecimal temperatura;

    @Column(precision = 5, scale = 2)
    private BigDecimal saturacionOxigeno;

    @Column(length = 500)
    private String observaciones;

    @Column(nullable = false, length = 120)
    private String registradoPor;

    @PrePersist
    public void prePersist() {
        if (fechaRegistro == null) {
            fechaRegistro = LocalDateTime.now();
        }
    }
}
