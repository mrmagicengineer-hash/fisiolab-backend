package com.app.fisiolab_system.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PacienteResumenPlanesResponse {
    private Long pacienteId;
    private String pacienteNombre;
    private String hcl;
    private String peorAlarma;
    private int conteoPlanesActivos;
}
