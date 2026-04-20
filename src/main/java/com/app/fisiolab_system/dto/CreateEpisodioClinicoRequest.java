package com.app.fisiolab_system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateEpisodioClinicoRequest(
        @NotBlank String pacienteId,
        @NotBlank @Size(max = 500) String motivo,
        String estado
) {
}
