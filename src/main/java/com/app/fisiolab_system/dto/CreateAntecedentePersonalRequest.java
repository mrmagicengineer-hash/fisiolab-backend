package com.app.fisiolab_system.dto;

import com.app.fisiolab_system.model.EstadoAntecedente;
import com.app.fisiolab_system.model.TipoAntecedentePersonal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateAntecedentePersonalRequest(
        @NotNull TipoAntecedentePersonal tipo,
        @NotBlank @Size(max = 500) String descripcion,
        @Size(max = 20) String codigoCie10,
        @NotNull EstadoAntecedente estado
) {
}
