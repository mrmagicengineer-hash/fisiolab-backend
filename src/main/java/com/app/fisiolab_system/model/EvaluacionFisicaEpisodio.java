package com.app.fisiolab_system.model;

import java.math.BigDecimal;
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
@Table(name = "evaluaciones_fisicas_episodio")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvaluacionFisicaEpisodio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "episodio_clinico_id", nullable = false)
    private EpisodioClinico episodioClinico;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoEvaluacionFisioterapeutica tipoEvaluacion;

    @Column(nullable = false)
    private LocalDateTime fechaEvaluacion;

    @Column(length = 60)
    private String frecuenciaCardiaca;

    @Column(length = 60)
    private String frecuenciaRespiratoria;

    @Column(length = 60)
    private String presionArterial;

    @Column(length = 60)
    private String saturacionOxigeno;

    @Column(precision = 6, scale = 2)
    private BigDecimal tallaCm;

    @Column(precision = 6, scale = 2)
    private BigDecimal pesoKg;

    @Column(precision = 6, scale = 2)
    private BigDecimal imc;

    @Column(nullable = false)
    private Integer eva;

    @Column(nullable = false, length = 255)
    private String localizacionDolor;

    @Column(nullable = false, length = 120)
    private String tipoDolor;

    @Column(nullable = false, length = 2000)
    private String examenFisicoSegmentario;

    @Column(nullable = false, length = 2000)
    private String diagnosticosPresuntivos;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String goniometriaJson;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String fuerzaMuscularJson;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String escalasFuncionalesJson;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String pruebasEspecialesJson;

    @Column(precision = 6, scale = 2)
    private BigDecimal puntajeFuncionalPromedio;

    @Column(length = 120)
    private String interpretacionFuncional;

    @Column(nullable = false)
    private Integer numeroEvaluacion;

    @PrePersist
    public void prePersist() {
        if (fechaEvaluacion == null) {
            fechaEvaluacion = LocalDateTime.now();
        }
    }
}
