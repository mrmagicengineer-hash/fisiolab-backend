package com.app.fisiolab_system.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreatePlanTratamientoRequest(

        @NotBlank @Size(max = 500)
        String objetivos,

        @NotNull @Min(1)
        Integer sesionesPlanificadas,

        @DecimalMin(value = "0.0", inclusive = false)
        BigDecimal costoSesion
) {}
