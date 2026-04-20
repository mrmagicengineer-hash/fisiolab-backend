package com.app.fisiolab_system.dto;

import java.math.BigDecimal;

public record ComparativaEscalaResponse(
        String nombreEscala,
        BigDecimal porcentajeInicial,
        BigDecimal porcentajeComparado,
        BigDecimal deltaPorcentaje
) {
}
