package com.app.fisiolab_system.dto;

import java.math.BigDecimal;
import java.util.List;

import com.app.fisiolab_system.model.EstadoPlan;

public record PlanResumenResponse(
        Long id,
        String objetivoGeneral,
        List<String> objetivosEspecificos,
        String indicacionesEducativas,
        Integer sesionesPlanificadas,
        Integer sesionesRealizadas,
        BigDecimal costoSesion,
        EstadoPlan estado
) {
}
