package com.app.fisiolab_system.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimelineItemResponse {

    /** "SESION_SOAP" o "SEGUIMIENTO_PLAN" */
    private String tipo;
    private Long itemId;
    private LocalDateTime fecha;
    private Integer numeroSesion;
    private String resumen;

    // Campos para tipo SESION_SOAP
    private String estadoSesion;
    private Boolean notaFirmada;

    // Campos para tipo SEGUIMIENTO_PLAN
    private String resultadoGeneral;
    private Integer porcentajeAvance;
}
