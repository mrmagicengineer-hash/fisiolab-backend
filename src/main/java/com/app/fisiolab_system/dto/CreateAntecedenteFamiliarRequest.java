package com.app.fisiolab_system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateAntecedenteFamiliarRequest(
        @NotBlank @Size(max = 50) String parentesco,
        @NotBlank @Size(max = 500) String condicion,
        @Size(max = 20) String codigoCie10
) {
}
