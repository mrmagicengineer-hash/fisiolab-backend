package com.app.fisiolab_system.dto;

import jakarta.validation.constraints.Size;

public record ActualizarNotaSOAPRequest(
        @Size(max = 5000) String subjetivo,
        @Size(max = 5000) String objetivo,
        @Size(max = 5000) String analisis,
        @Size(max = 5000) String plan
) {
}
