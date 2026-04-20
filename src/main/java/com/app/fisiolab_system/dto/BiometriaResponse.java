package com.app.fisiolab_system.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BiometriaResponse(
        Long id,
        Long episodioClinicoId,
        LocalDateTime fechaRegistro,
        BigDecimal pesoKg,
        BigDecimal tallaCm,
        BigDecimal imcCalculado,
        String clasificacionImc,
        BigDecimal perimetroCinturaCm,
        BigDecimal perimetroCaderaCm,
        Integer presionArterialSistolica,
        Integer presionArterialDiastolica,
        String clasificacionPresionArterial,
        Integer frecuenciaCardiaca,
        Integer frecuenciaRespiratoria,
        BigDecimal temperatura,
        BigDecimal saturacionOxigeno,
        String observaciones,
        String registradoPor
) {
}
