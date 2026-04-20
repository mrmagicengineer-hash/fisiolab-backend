package com.app.fisiolab_system.dto;

import java.time.LocalDateTime;

import com.app.fisiolab_system.model.EstadoCierreEpisodio;
import com.app.fisiolab_system.model.EstadoEpisodioClinico;

public record EpisodioClinicoResponse(
        Long id,
        Long historiaClinicaId,
        String numeroHcl,
        Integer numeroSecuencial,
        String numeroEpisodio,
        String motivoConsulta,
        LocalDateTime fechaApertura,
        LocalDateTime fechaCierre,
        EstadoEpisodioClinico estado,
        EstadoCierreEpisodio estadoCierre,
        String observacionCierre
) {
}
