package com.app.fisiolab_system.dto;

import java.util.List;

public record PacienteResumenCompletoResponse(
        PacienteResponse paciente,
        FichaFamiliarResponse fichaFamiliar,
        HistoriaClinicaResumenResponse historiaClinica,
        List<AntecedentePersonalResponse> antecedentesPersonales,
        List<AntecedenteFamiliarResponse> antecedentesFamiliares,
        List<ProblemaEpisodioResponse> problemasActivos,
        List<EpisodioClinicoResponse> episodios,
        int totalEpisodios,
        int totalEvaluaciones
) {
}
