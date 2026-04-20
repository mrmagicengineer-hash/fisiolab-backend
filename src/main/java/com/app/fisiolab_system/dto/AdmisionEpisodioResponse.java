package com.app.fisiolab_system.dto;

import java.time.LocalDateTime;

import com.app.fisiolab_system.model.TipoAtencionAdmision;

public record AdmisionEpisodioResponse(
        Long id,
        Long episodioClinicoId,
        LocalDateTime fechaHoraAdmision,
        TipoAtencionAdmision tipoAtencion,
        String motivoAtencion,
        String profesionalAtiende
) {
}
