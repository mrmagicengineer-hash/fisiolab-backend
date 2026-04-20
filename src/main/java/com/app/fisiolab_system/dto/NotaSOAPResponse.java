package com.app.fisiolab_system.dto;

import java.time.LocalDateTime;

public record NotaSOAPResponse(
        Long id,
        Long sesionTerapiaId,
        String subjetivo,
        String objetivo,
        String analisis,
        String plan,
        boolean modoBorrador,
        Long firmadoPorId,
        String firmadoPorNombre,
        LocalDateTime firmadoEn,
        String hashIntegridad,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaModificacion
) {
}
