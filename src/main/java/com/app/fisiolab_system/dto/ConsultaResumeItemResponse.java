package com.app.fisiolab_system.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsultaResumeItemResponse {
    private String id;
    private String hcl;
    private String nombresCompletos;
    private Integer totalConsultas;
    private String ultimaConsultaFecha;
    private String ultimaConsultaMotivo;
    private Boolean tienePlanTratamiento;
    private String estadoSeguimiento;
}
