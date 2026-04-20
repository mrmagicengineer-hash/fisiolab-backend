package com.app.fisiolab_system.dto;

import java.util.List;

public record HistoriaClinicaCompletaResponse(
        HistoriaClinicaResumenResponse resumen,
        List<AntecedentePersonalResponse> antecedentesPersonales,
        List<AntecedenteFamiliarResponse> antecedentesFamiliares,
        List<ProblemaEpisodioResponse> problemasActivos,
        List<String> episodiosPrevios
) {
}
