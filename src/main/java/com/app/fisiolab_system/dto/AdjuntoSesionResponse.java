package com.app.fisiolab_system.dto;

import java.time.LocalDateTime;

public record AdjuntoSesionResponse(
        Long id,
        Long sesionTerapiaId,
        String nombreOriginal,
        String tipoMime,
        Long tamanoBytes,
        Long subidoPorId,
        String subidoPorNombre,
        LocalDateTime fechaSubida,
        String urlDescarga
) {
}
