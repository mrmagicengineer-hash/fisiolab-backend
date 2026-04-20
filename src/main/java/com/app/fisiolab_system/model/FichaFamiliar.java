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
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ficha_familiar")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FichaFamiliar {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", nullable = false, unique = true)
    private Paciente paciente;

    @Column(nullable = false, length = 160)
    private String jefeHogar;

    @Column(nullable = false)
    private Integer numeroMiembros;

    @Column(nullable = false, length = 80)
    private String tipoVivienda;

    @Column(nullable = false, length = 255)
    private String condicionesSanitarias;

    @Column(nullable = false)
    private LocalDateTime fechaActualizacion;

    @PrePersist
    @PreUpdate
    public void touch() {
        fechaActualizacion = LocalDateTime.now();
    }
}
