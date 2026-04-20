package com.app.fisiolab_system.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EstadisticasDashboardResponse {
    private long total;
    private long enRiesgo;
    private long finalizando;
}
