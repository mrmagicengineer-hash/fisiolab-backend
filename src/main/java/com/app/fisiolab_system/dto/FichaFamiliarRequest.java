package com.app.fisiolab_system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record FichaFamiliarRequest(
        @NotBlank @Size(max = 160) String jefeHogar,
        @NotNull @Positive Integer numeroMiembros,
        @NotBlank @Size(max = 80) String tipoVivienda,
        @NotBlank @Size(max = 255) String condicionesSanitarias
) {
}
