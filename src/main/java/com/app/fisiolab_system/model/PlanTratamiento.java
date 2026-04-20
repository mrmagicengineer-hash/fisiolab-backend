package com.app.fisiolab_system.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "planes_tratamiento")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanTratamiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "episodio_clinico_id", nullable = false)
    private EpisodioClinico episodioClinico;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problema_episodio_id", nullable = false)
    private ProblemaEpisodio problemaEpisodio;

    @Column(nullable = false, length = 500)
    private String objetivoGeneral;

    // JSON array de strings serializado
    @Column(columnDefinition = "TEXT")
    private String objetivosEspecificosJson;

    @Column(nullable = false)
    private LocalDate fechaInicio;

    @Column(nullable = false)
    private LocalDate fechaFinEstimada;

    @Column(nullable = false)
    private Integer sesionesPlanificadas;

    @Column(nullable = false)
    @Builder.Default
    private Integer sesionesRealizadas = 0;

    @Column(precision = 10, scale = 2)
    private BigDecimal costoSesion;

    @Column(columnDefinition = "TEXT")
    private String indicacionesEducativas;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CodigoAlarma codigoAlarma;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EstadoPlan estado = EstadoPlan.ACTIVO;

    @Column(nullable = false)
    private LocalDateTime fechaCreacion;

    @OneToMany(mappedBy = "planTratamiento", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<SeguimientoPlan> seguimientos;

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
    }
}
