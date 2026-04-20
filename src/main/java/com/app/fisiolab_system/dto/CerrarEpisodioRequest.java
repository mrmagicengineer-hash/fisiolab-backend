package com.app.fisiolab_system.dto;

import com.app.fisiolab_system.model.EstadoCierreEpisodio;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CerrarEpisodioRequest(
        @NotNull EstadoCierreEpisodio estadoCierre,
        @Size(max = 500) String observacionCierre
) {
}
