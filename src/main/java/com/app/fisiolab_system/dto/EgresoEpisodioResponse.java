package com.app.fisiolab_system.dto;

import java.time.LocalDateTime;

public record EgresoEpisodioResponse(
        Long id,
        Long episodioClinicoId,
        LocalDateTime fechaHoraEgreso,
        String condicionSalida,
        String causaAlta,
        String destinoPaciente,
        boolean referidoOtraInstitucion
) {
}
