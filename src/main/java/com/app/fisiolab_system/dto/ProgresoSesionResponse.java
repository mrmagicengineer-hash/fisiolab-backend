package com.app.fisiolab_system.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.app.fisiolab_system.model.EstadoSesionTerapia;
import com.app.fisiolab_system.model.TipoEvaluacionFisioterapeutica;

public record ProgresoSesionResponse(
        Long sesionId,
        Integer numeroSesionActual,
        Integer totalSesionesPlan,
        Integer sesionesRealizadas,
        Integer sesionesRestantes,
        EvaluacionReferencia evaluacionInicial,
        EvaluacionReferencia evaluacionReciente,
        Integer deltaEva,
        List<SesionResumenItem> historialSesiones
) {
    public record EvaluacionReferencia(
            Long evaluacionId,
            LocalDateTime fecha,
            Integer eva,
            BigDecimal puntajeFuncionalPromedio,
            String interpretacionFuncional,
            TipoEvaluacionFisioterapeutica tipo
    ) {}

    public record SesionResumenItem(
            Long sesionId,
            Integer numeroSesion,
            LocalDateTime fecha,
            EstadoSesionTerapia estado
    ) {}
}
