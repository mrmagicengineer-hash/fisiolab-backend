package com.app.fisiolab_system.dto;

import com.app.fisiolab_system.model.EstadoProblemaEpisodio;
import lombok.Data;

@Data
public class ProblemaEpisodioResponse {
    private Long id;
    private Integer numeroSecuencial;
    private String descripcion;
    private String codigoCie10;
    private EstadoProblemaEpisodio estado;
}
