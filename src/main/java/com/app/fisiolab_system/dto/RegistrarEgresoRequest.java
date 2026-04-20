package com.app.fisiolab_system.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegistrarEgresoRequest(
        @NotNull LocalDateTime fechaHoraEgreso,
        @NotBlank @Size(max = 120) String condicionSalida,
        @NotBlank @Size(max = 500) String causaAlta,
        @NotBlank @Size(max = 160) String destinoPaciente,
        boolean referidoOtraInstitucion
) {
}
