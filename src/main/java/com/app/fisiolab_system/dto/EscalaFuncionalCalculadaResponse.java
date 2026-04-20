package com.app.fisiolab_system.dto;

import java.math.BigDecimal;

public record EscalaFuncionalCalculadaResponse(
        String nombreEscala,
        BigDecimal puntajeObtenido,
        BigDecimal puntajeMaximo,
        BigDecimal porcentaje,
        String interpretacion
) {
}
