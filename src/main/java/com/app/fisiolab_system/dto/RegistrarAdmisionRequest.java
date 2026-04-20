package com.app.fisiolab_system.dto;

import java.time.LocalDateTime;

import com.app.fisiolab_system.model.TipoAtencionAdmision;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegistrarAdmisionRequest(
        @NotNull LocalDateTime fechaHoraAdmision,
        @NotNull TipoAtencionAdmision tipoAtencion,
        @NotBlank @Size(max = 500) String motivoAtencion,
        @NotBlank @Size(max = 160) String profesionalAtiende
) {
}
