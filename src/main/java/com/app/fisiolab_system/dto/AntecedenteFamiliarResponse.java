package com.app.fisiolab_system.dto;

import java.time.LocalDateTime;

public record AntecedenteFamiliarResponse(
        Long id,
        String parentesco,
        String condicion,
        String codigoCie10,
        LocalDateTime fechaRegistro
) {
}
