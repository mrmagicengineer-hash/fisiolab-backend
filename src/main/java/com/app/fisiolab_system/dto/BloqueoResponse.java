package com.app.fisiolab_system.dto;

import java.time.LocalDateTime;

import com.app.fisiolab_system.model.MotivoBloqueo;

public record BloqueoResponse(
        Long id,
        Long profesionalId,
        String profesionalNombre,
        Long creadoPorId,
        String creadoPorNombre,
        LocalDateTime fechaHoraInicio,
        LocalDateTime fechaHoraFin,
        MotivoBloqueo motivo,
        String descripcion,
        LocalDateTime fechaCreacion
) {
}
