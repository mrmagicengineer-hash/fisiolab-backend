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
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "sesiones_terapia",
    uniqueConstraints = @UniqueConstraint(columnNames = "cita_id")
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SesionTerapia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Origen: cita que dio lugar a esta sesión (relación 1:1 forzada por UK)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cita_id", nullable = false)
    private Cita cita;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_tratamiento_id")
    private PlanTratamiento planTratamiento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "episodio_clinico_id")
    private EpisodioClinico episodioClinico;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profesional_id", nullable = false)
    private Usuario profesional;

    @Column(precision = 10, scale = 2)
    private BigDecimal costoSesion;

    // Número de sesión dentro del plan (ej. "Sesión 3 de 10")
    @Column
    private Integer numeroSesionEnPlan;

    @Column(nullable = false)
    private LocalDateTime fechaHoraInicio;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EstadoSesionTerapia estado = EstadoSesionTerapia.EN_PROGRESO;

    // Firma digital
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "firmado_por_id")
    private Usuario firmadoPor;

    @Column
    private LocalDateTime firmadoEn;

    // SHA-256 del contenido SOAP al momento de firmar
    @Column(length = 64)
    private String hashIntegridad;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @PrePersist
    public void prePersist() {
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
        if (estado == null) {
            estado = EstadoSesionTerapia.EN_PROGRESO;
        }
    }
}
