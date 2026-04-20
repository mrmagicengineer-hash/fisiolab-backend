package com.app.fisiolab_system.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.app.fisiolab_system.model.EstadoArchivoPaciente;

public record PacienteResponse(
        Long id,
        String numeroHcl,
        String cedula,
        String email,
        String nombresCompletos,
        LocalDate fechaNacimiento,
        String genero,
        String grupoCultural,
        String estadoCivil,
        String ocupacion,
        String regimenSeguridadSocial,
        String tipoSangre,
        String telefonoPrincipal,
        String telefonoSecundario,
        String direccion,
        EstadoArchivoPaciente estadoArchivo,
        LocalDateTime fechaRegistro,
        LocalDateTime fechaUltimaAtencion
) {
}
