package com.app.fisiolab_system.dto;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateEvaluacionClinicaRequest(
        LocalDateTime fechaEvaluacion,
        @Size(max = 50) String fisioterapeutaId,
        @NotNull @Valid SignosVitalesDto signosVitales,
        @NotBlank @Size(max = 500) String motivoConsulta,
        @NotBlank @Size(max = 500) String observacionGeneral,
        @NotEmpty List<@NotBlank @Size(max = 300) String> hallazgosPrincipales,
        @NotNull @Min(0) @Max(10) Integer escalaEva,
        @NotBlank @Size(max = 500) String impresionDiagnostica,
        @NotBlank @Size(max = 1000) String planInicial
) {
}
