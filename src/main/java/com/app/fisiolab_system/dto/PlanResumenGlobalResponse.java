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
public class PlanResumenGlobalResponse {
    private String id;
    private String hcl;
    private String paciente;
    private Long episodioId;
    private Long problemaId;
    private String problemaDescripcion;
    private String codigoCie10;
    private String objetivoGeneral;
    private Integer sesionesPlanificadas;
    private Integer sesionesRealizadas;
    private Double porcentajeAvance;
    private String codigoAlarma;
    private String estado;
    private String ultimoResultado;
    private String fechaInicio;
    private String fechaFinEstimada;
}
