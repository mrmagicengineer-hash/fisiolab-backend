package com.app.fisiolab_system.dto;

import com.app.fisiolab_system.model.EstadoProblemaEpisodio;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateProblemaEpisodioRequest {
    @NotBlank
    private String descripcion;
    @NotBlank
    private String codigoCie10;
    @NotNull
    private EstadoProblemaEpisodio estado;
}
