package com.app.fisiolab_system.dto;

import java.math.BigDecimal;
import java.util.List;

public record ComparativaEvaluacionResponse(
        Long evaluacionInicialId,
        Long evaluacionComparadaId,
        Integer deltaEva,
        BigDecimal deltaPuntajeFuncionalPromedio,
        List<ComparativaEscalaResponse> deltasEscalas
) {
}
