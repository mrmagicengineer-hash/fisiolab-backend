package com.app.fisiolab_system.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

public record UpdatePacienteRequest(
        @NotBlank @Size(max = 20) String cedula,
        @NotBlank @Email @Size(max = 120) String email,
        @NotBlank @Size(max = 160) String nombresCompletos,
        @NotNull @Past LocalDate fechaNacimiento,
        @NotBlank @Size(max = 30) String genero,
        @Size(max = 80) String grupoCultural,
        @Size(max = 60) String estadoCivil,
        @Size(max = 100) String ocupacion,
        @Size(max = 100) String regimenSeguridadSocial,
        @Size(max = 10) String tipoSangre,
        @NotBlank @Size(max = 20) String telefonoPrincipal,
        @Size(max = 20) String telefonoSecundario,
        @Size(max = 255) String direccion
) {
}
