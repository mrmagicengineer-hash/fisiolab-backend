package com.app.fisiolab_system.model;

import java.time.LocalDate;
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
@Table(name = "seguimientos_plan")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeguimientoPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_tratamiento_id", nullable = false)
    private PlanTratamiento planTratamiento;

    @Column(nullable = false)
    private Integer numeroSesion;

    @Column(nullable = false)
    private LocalDate fechaSeguimiento;

    // Porcentaje de avance clínico reportado por el fisioterapeuta (0–100)
    @Column(nullable = false)
    private Integer porcentajeAvance;

    @Column(columnDefinition = "TEXT")
    private String resultadosObtenidos;

    @Column(columnDefinition = "TEXT")
    private String ajustes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ResultadoGeneral resultadoGeneral;

    @Column(nullable = false)
    private LocalDateTime fechaRegistro;

    @PrePersist
    protected void onCreate() {
        fechaRegistro = LocalDateTime.now();
    }
}
