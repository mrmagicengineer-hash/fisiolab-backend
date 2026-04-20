package com.app.fisiolab_system.dto;

import com.app.fisiolab_system.model.CodigoAlarma;
import com.app.fisiolab_system.model.EstadoPlan;

// RF-33: Indicador visual de avance de sesiones del plan de tratamiento
public record IndicadorAvancePlanResponse(
        Long planId,
        Integer sesionesRealizadas,
        Integer sesionesPlanificadas,
        Integer porcentajeAvance,
        EstadoPlan estado,
        CodigoAlarma codigoAlarma
) {}
