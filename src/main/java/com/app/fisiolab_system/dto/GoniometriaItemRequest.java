package com.app.fisiolab_system.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record GoniometriaItemRequest(
        @NotBlank @Size(max = 120) String articulacion,
        @NotBlank @Size(max = 80) String plano,
        @NotNull @PositiveOrZero BigDecimal rangoMovimientoGrados
) {
}
