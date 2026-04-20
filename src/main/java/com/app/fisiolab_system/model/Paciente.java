package com.app.fisiolab_system.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "pacientes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Paciente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String numeroHcl;

    @Column(nullable = false, unique = true, length = 20)
    private String cedula;

    @Column(unique = true, length = 120)
    private String email;

    @Column(nullable = false, length = 160)
    private String nombresCompletos;

    @Column(nullable = false)
    private LocalDate fechaNacimiento;

    @Column(nullable = false, length = 30)
    private String genero;

    @Column(length = 80)
    private String grupoCultural;

    @Column(length = 60)
    private String estadoCivil;

    @Column(length = 100)
    private String ocupacion;

    @Column(length = 100)
    private String regimenSeguridadSocial;

    @Column(length = 10)
    private String tipoSangre;

    @Column(nullable = false, length = 20)
    private String telefonoPrincipal;

    @Column(length = 20)
    private String telefonoSecundario;

    @Column(length = 255)
    private String direccion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoArchivoPaciente estadoArchivo;

    @Column(nullable = false)
    private LocalDateTime fechaRegistro;

    @Column(nullable = false)
    private LocalDateTime fechaUltimaAtencion;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (fechaRegistro == null) {
            fechaRegistro = now;
        }
        if (fechaUltimaAtencion == null) {
            fechaUltimaAtencion = now;
        }
        if (estadoArchivo == null) {
            estadoArchivo = EstadoArchivoPaciente.ACTIVO;
        }
    }
}
