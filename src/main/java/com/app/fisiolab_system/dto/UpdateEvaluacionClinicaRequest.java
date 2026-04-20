package com.app.fisiolab_system.dto;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record UpdateEvaluacionClinicaRequest(
        LocalDateTime fechaEvaluacion,
        @Size(max = 50) String fisioterapeutaId,
        @Valid SignosVitalesDto signosVitales,
        @Size(max = 500) String motivoConsulta,
        @Size(max = 500) String observacionGeneral,
        List<@Size(max = 300) String> hallazgosPrincipales,
        @Min(0) @Max(10) Integer escalaEva,
        @Size(max = 500) String impresionDiagnostica,
        @Size(max = 1000) String planInicial
) {
}
