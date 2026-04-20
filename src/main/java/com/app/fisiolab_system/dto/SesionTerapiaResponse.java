package com.app.fisiolab_system.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.app.fisiolab_system.model.EstadoSesionTerapia;

public record SesionTerapiaResponse(
        Long id,
        Long citaId,
        Long planTratamientoId,
        Long pacienteId,
        String pacienteNombre,
        Long episodioClinicoId,
        Long profesionalId,
        String profesionalNombre,
        BigDecimal costoSesion,
        Integer numeroSesionEnPlan,
        LocalDateTime fechaHoraInicio,
        EstadoSesionTerapia estado,
        Long firmadoPorId,
        LocalDateTime firmadoEn,
        String hashIntegridad,
        LocalDateTime fechaCreacion,
        NotaSOAPResponse notaSOAP,
        PlanResumenResponse planResumen
) {
}
