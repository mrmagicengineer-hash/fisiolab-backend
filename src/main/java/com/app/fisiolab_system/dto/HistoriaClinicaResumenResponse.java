package com.app.fisiolab_system.dto;

import java.time.LocalDateTime;

import com.app.fisiolab_system.model.EstadoArchivoPaciente;
import com.app.fisiolab_system.model.EstadoHistoriaClinica;

public record HistoriaClinicaResumenResponse(
        Long historiaClinicaId,
        Long pacienteId,
        String numeroHcl,
        String paciente,
        String cedula,
        String unidadSalud,
        EstadoHistoriaClinica estadoHistoriaClinica,
        EstadoArchivoPaciente estadoArchivoPaciente,
        LocalDateTime fechaApertura
) {
}
