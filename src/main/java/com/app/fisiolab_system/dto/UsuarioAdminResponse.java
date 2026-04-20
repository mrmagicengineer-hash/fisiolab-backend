// UsuarioAdminResponse.java
package com.app.fisiolab_system.dto;

import com.app.fisiolab_system.model.RolUsuario;

public record UsuarioAdminResponse(
        Long id,
        String cedula,
        String email,
        String name,
        String lastName,
        RolUsuario rol,
        boolean activo,
        String especialidad,
        String tipoProfesional,
        String codigoRegistro
) {
}