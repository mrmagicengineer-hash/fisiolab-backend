package com.app.fisiolab_system.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record FuerzaMuscularItemRequest(
        @NotBlank @Size(max = 120) String grupoMuscular,
        @NotNull @Min(0) @Max(5) Integer puntajeDaniels
) {
}
