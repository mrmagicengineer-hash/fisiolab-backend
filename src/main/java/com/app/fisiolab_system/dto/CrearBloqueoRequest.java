package com.app.fisiolab_system.dto;

import java.time.LocalDateTime;

import com.app.fisiolab_system.model.MotivoBloqueo;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CrearBloqueoRequest(

        @NotNull(message = "El profesional es obligatorio")
        Long profesionalId,

        @NotNull(message = "La fecha y hora de inicio es obligatoria")
        LocalDateTime fechaHoraInicio,

        @NotNull(message = "La fecha y hora de fin es obligatoria")
        LocalDateTime fechaHoraFin,

        @NotNull(message = "El motivo del bloqueo es obligatorio")
        MotivoBloqueo motivo,

        @Size(max = 500, message = "La descripcion no puede superar 500 caracteres")
        String descripcion
) {
}
