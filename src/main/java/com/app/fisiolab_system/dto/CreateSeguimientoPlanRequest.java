package com.app.fisiolab_system.dto;

import java.time.LocalDate;

import com.app.fisiolab_system.model.ResultadoGeneral;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateSeguimientoPlanRequest(

        @NotNull
        LocalDate fechaSeguimiento,

        @NotNull @Min(0) @Max(100)
        Integer porcentajeAvance,

        @NotBlank
        String resultadosObtenidos,

        String ajustes,

        @NotNull
        ResultadoGeneral resultadoGeneral
) {}
