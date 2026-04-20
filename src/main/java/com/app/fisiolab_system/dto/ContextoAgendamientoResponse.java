package com.app.fisiolab_system.dto;

import java.math.BigDecimal;
import java.util.List;

import com.app.fisiolab_system.model.EstadoEpisodioClinico;
import com.app.fisiolab_system.model.EstadoPlan;

public record ContextoAgendamientoResponse(
        Long pacienteId,
        String pacienteNombre,
        List<EpisodioAgendamiento> episodiosAbiertos
) {
    public record EpisodioAgendamiento(
            Long episodioId,
            String numeroEpisodio,
            String motivoConsulta,
            EstadoEpisodioClinico estadoEpisodio,
            List<PlanAgendamiento> planes
    ) {}

    public record PlanAgendamiento(
            Long planId,
            String objetivoGeneral,
            Integer sesionesPlanificadas,
            Integer sesionesRealizadas,
            Integer sesionesRestantes,
            BigDecimal costoSesion,
            EstadoPlan estadoPlan
    ) {}
}
