package com.app.fisiolab_system.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.app.fisiolab_system.model.ResultadoGeneral;

public record SeguimientoPlanResponse(
        Long id,
        Long planTratamientoId,
        Integer numeroSesion,
        LocalDate fechaSeguimiento,
        Integer porcentajeAvance,
        String resultadosObtenidos,
        String ajustes,
        ResultadoGeneral resultadoGeneral,
        LocalDateTime fechaRegistro
) {}
