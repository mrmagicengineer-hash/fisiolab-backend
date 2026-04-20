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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "admisiones_episodio")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdmisionEpisodio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "episodio_clinico_id", nullable = false, unique = true)
    private EpisodioClinico episodioClinico;

    @Column(nullable = false)
    private LocalDateTime fechaHoraAdmision;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoAtencionAdmision tipoAtencion;

    @Column(nullable = false, length = 500)
    private String motivoAtencion;

    @Column(nullable = false, length = 160)
    private String profesionalAtiende;
}
