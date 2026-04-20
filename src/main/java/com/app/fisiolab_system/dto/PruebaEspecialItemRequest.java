package com.app.fisiolab_system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PruebaEspecialItemRequest(
        @NotBlank @Size(max = 120) String nombre,
        @NotBlank @Pattern(regexp = "POSITIVO|NEGATIVO|DUDOSO", message = "resultado debe ser POSITIVO, NEGATIVO o DUDOSO") String resultado,
        @Size(max = 400) String observacion
) {
}
