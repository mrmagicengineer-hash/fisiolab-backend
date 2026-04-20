package com.app.fisiolab_system.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record EscalaFuncionalItemRequest(
        @NotBlank @Size(max = 80) String nombreEscala,
        @NotNull @PositiveOrZero BigDecimal puntajeObtenido,
        @NotNull @Positive BigDecimal puntajeMaximo
) {
}
