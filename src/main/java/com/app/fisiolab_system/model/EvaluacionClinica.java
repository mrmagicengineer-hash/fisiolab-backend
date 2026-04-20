package com.app.fisiolab_system.model;

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
@Table(name = "evaluaciones_clinicas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvaluacionClinica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "episodio_clinico_id", nullable = false)
    private EpisodioClinico episodioClinico;

    @Column(nullable = false)
    private LocalDateTime fechaEvaluacion;

    @Column(length = 50)
    private String fisioterapeutaId;

    @Column(length = 20)
    private String presionArterial;

    private Integer frecuenciaCardiaca;

    @Column(length = 500)
    private String motivoConsulta;

    @Column(length = 500)
    private String observacionGeneral;

    @Column(columnDefinition = "TEXT")
    private String hallazgosPrincipalesJson;

    @Column(nullable = false)
    private Integer escalaEva;

    @Column(length = 500)
    private String impresionDiagnostica;

    @Column(length = 1000)
    private String planInicial;

    @Column(nullable = false, updatable = false)
    private LocalDateTime creadoEn;

    @PrePersist
    public void prePersist() {
        if (fechaEvaluacion == null) {
            fechaEvaluacion = LocalDateTime.now();
        }
        if (creadoEn == null) {
            creadoEn = LocalDateTime.now();
        }
    }
}
