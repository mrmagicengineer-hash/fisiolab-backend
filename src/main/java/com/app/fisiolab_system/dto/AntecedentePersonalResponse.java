package com.app.fisiolab_system.dto;

import java.time.LocalDateTime;

import com.app.fisiolab_system.model.EstadoAntecedente;
import com.app.fisiolab_system.model.TipoAntecedentePersonal;

public record AntecedentePersonalResponse(
        Long id,
        TipoAntecedentePersonal tipo,
        String descripcion,
        String codigoCie10,
        EstadoAntecedente estado,
        LocalDateTime fechaRegistro
) {
}
