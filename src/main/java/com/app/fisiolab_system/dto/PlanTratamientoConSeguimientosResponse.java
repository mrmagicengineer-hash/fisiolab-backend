package com.app.fisiolab_system.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.app.fisiolab_system.model.CodigoAlarma;
import com.app.fisiolab_system.model.EstadoPlan;

public record PlanTratamientoConSeguimientosResponse(
        Long id,
        Long episodioClinicoId,
        Long problemaEpisodioId,
        String objetivoGeneral,
        List<String> objetivosEspecificos,
        LocalDate fechaInicio,
        LocalDate fechaFinEstimada,
        Integer sesionesPlanificadas,
        Integer sesionesRealizadas,
        Integer porcentajeAvanceTotal,
        String indicacionesEducativas,
        CodigoAlarma codigoAlarma,
        EstadoPlan estado,
        LocalDateTime fechaCreacion,
        BigDecimal costoSesion,
        List<SeguimientoPlanResponse> seguimientos
) {}
