package com.app.fisiolab_system.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SignosVitalesDto(
        @NotBlank @Size(max = 20) String pa,
        @NotNull @Min(30) @Max(250) Integer fc
) {
}
