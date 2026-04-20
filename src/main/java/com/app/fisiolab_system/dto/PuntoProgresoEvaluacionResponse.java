package com.app.fisiolab_system.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.app.fisiolab_system.model.TipoEvaluacionFisioterapeutica;

public record PuntoProgresoEvaluacionResponse(
        Long evaluacionId,
        Integer numeroEvaluacion,
        TipoEvaluacionFisioterapeutica tipoEvaluacion,
        LocalDateTime fechaEvaluacion,
        Integer eva,
        BigDecimal puntajeFuncionalPromedio
) {
}
