package com.app.fisiolab_system.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CrearCitaRequest(

        @NotNull(message = "El paciente es obligatorio")
        Long pacienteId,

        @NotNull(message = "El profesional es obligatorio")
        Long profesionalId,

        @NotNull(message = "La fecha y hora de inicio es obligatoria")
        @Future(message = "La cita debe programarse en el futuro")
        LocalDateTime fechaHoraInicio,

        @NotNull(message = "La fecha y hora de fin es obligatoria")
        LocalDateTime fechaHoraFin,

        @NotBlank(message = "El motivo de consulta es obligatorio")
        @Size(max = 500, message = "El motivo no puede superar 500 caracteres")
        String motivoConsulta,

        @Size(max = 10, message = "El codigo CIE-10 no puede superar 10 caracteres")
        String codigoCie10Sugerido,

        @Size(max = 1000, message = "Las observaciones no pueden superar 1000 caracteres")
        String observaciones,

        // Enlace opcional al episodio clínico activo del paciente
        Long episodioClinicoId,

        // Enlace opcional al plan de tratamiento activo (debe pertenecer al episodioClinicoId)
        Long planTratamientoId
) {
}
