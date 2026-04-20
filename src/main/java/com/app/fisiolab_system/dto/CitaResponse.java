package com.app.fisiolab_system.dto;

import java.time.LocalDateTime;

import com.app.fisiolab_system.model.EstadoCita;

public record CitaResponse(
        Long id,
        Long pacienteId,
        String pacienteNombres,
        String pacienteCedula,
        Long profesionalId,
        String profesionalNombre,
        Long creadoPorId,
        String creadoPorNombre,
        LocalDateTime fechaHoraInicio,
        LocalDateTime fechaHoraFin,
        EstadoCita estado,
        String motivoConsulta,
        String codigoCie10Sugerido,
        String observaciones,
        Long episodioClinicoId,
        Long planTratamientoId,
        Long sesionGeneradaId,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaModificacion
) {
}
