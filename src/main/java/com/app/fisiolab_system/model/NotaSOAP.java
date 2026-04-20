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
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "notas_soap")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotaSOAP {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sesion_terapia_id", nullable = false, unique = true)
    private SesionTerapia sesionTerapia;

    // S — Subjetivo: lo que el paciente reporta
    @Column(columnDefinition = "TEXT")
    private String subjetivo;

    // O — Objetivo: hallazgos medibles y observables
    @Column(columnDefinition = "TEXT")
    private String objetivo;

    // A — Análisis/Evaluación: interpretación clínica del fisioterapeuta
    @Column(columnDefinition = "TEXT")
    private String analisis;

    // P — Plan: intervención y metas para próxima sesión
    @Column(columnDefinition = "TEXT")
    private String plan;

    // true = borrador editable; false = firmado y read-only
    @Column(nullable = false)
    @Builder.Default
    private boolean modoBorrador = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "firmado_por_id")
    private Usuario firmadoPor;

    @Column
    private LocalDateTime firmadoEn;

    // SHA-256 de (subjetivo + objetivo + analisis + plan + firmadoEn) al momento de firma
    @Column(length = 64)
    private String hashIntegridad;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(nullable = false)
    private LocalDateTime fechaModificacion;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (fechaCreacion == null) fechaCreacion = now;
        fechaModificacion = now;
    }

    @PreUpdate
    public void preUpdate() {
        fechaModificacion = LocalDateTime.now();
    }
}
