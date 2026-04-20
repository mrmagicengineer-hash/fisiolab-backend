package com.app.fisiolab_system.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.app.fisiolab_system.model.EstadoCierreEpisodio;
import com.app.fisiolab_system.model.EstadoEpisodioClinico;

public record EpisodioClinicoContenidoResponse(
        // Información del episodio
        Long episodioId,
        Long historiaClinicaId,
        String numeroHcl,
        Integer numeroSecuencial,
        String numeroEpisodio,
        String motivoConsulta,
        LocalDateTime fechaApertura,
        LocalDateTime fechaCierre,
        EstadoEpisodioClinico estado,
        EstadoCierreEpisodio estadoCierre,
        String observacionCierre,

        // Contenido del episodio
        List<ProblemaEpisodioResponse> problemas,
        List<EvaluacionFisicaResponse> evaluaciones,
        List<PlanTratamientoConSeguimientosResponse> planesTratamiento
) {}
