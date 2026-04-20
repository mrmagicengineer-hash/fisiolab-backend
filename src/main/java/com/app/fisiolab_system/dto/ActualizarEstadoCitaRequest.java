package com.app.fisiolab_system.dto;

import com.app.fisiolab_system.model.EstadoCita;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ActualizarEstadoCitaRequest(

        @NotNull(message = "El nuevo estado es obligatorio")
        EstadoCita nuevoEstado,

        @Size(max = 1000, message = "Las observaciones no pueden superar 1000 caracteres")
        String observaciones
) {
}
