package com.app.fisiolab_system.dto;

import java.time.LocalDateTime;

public record FichaFamiliarResponse(
        Long id,
        Long pacienteId,
        String jefeHogar,
        Integer numeroMiembros,
        String tipoVivienda,
        String condicionesSanitarias,
        LocalDateTime fechaActualizacion
) {
}
