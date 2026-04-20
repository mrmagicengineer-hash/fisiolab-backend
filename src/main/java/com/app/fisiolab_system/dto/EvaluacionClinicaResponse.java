package com.app.fisiolab_system.dto;

import java.time.LocalDateTime;
import java.util.List;

public record EvaluacionClinicaResponse(
        Long id,
        Long episodioClinicoId,
        LocalDateTime fechaEvaluacion,
        String fisioterapeutaId,
        SignosVitalesDto signosVitales,
        String motivoConsulta,
        String observacionGeneral,
        List<String> hallazgosPrincipales,
        Integer escalaEva,
        String impresionDiagnostica,
        String planInicial,
        LocalDateTime creadoEn
) {
}
