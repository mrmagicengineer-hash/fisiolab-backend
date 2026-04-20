package com.app.fisiolab_system.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContextoPlanesResponse {

    private Long pacienteId;
    private String pacienteNombre;
    private String hcl;
    private List<EpisodioItem> episodios;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EpisodioItem {
        private Long episodioId;
        private String numeroEpisodio;
        private String motivoConsulta;
        private String estado;
        private LocalDateTime fechaApertura;
        private List<PlanItem> planes;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PlanItem {
        private Long planId;
        private Long problemaId;
        private String problemaDescripcion;
        private String codigoCie10;
        private String objetivoGeneral;
        private int sesionesPlanificadas;
        private int sesionesRealizadas;
        private int porcentajeAvance;
        private String codigoAlarma;
        private String estado;
        private LocalDate fechaInicio;
        private LocalDate fechaFinEstimada;
    }
}
