// CreateUsuarioRequest.java
package com.app.fisiolab_system.dto;

import com.app.fisiolab_system.model.RolUsuario;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateUsuarioRequest(
        @NotBlank @Size(max = 20) String cedula,
        @NotBlank @Email @Size(max = 120) String email,
        @NotBlank @Size(max = 120) String name,
        @NotBlank @Size(max = 120) String lastName,
        @NotBlank String password,
        @NotNull RolUsuario rol,
        @Size(max = 120) String especialidad,
        @Size(max = 120) String tipoProfesional,
        @Size(max = 120) String codigoRegistro
) {
}