package com.app.fisiolab_system.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.app.fisiolab_system.model.TipoEvaluacionFisioterapeutica;

public record EvaluacionFisicaResponse(
        Long id,
        Long episodioClinicoId,
        Integer numeroEvaluacion,
        TipoEvaluacionFisioterapeutica tipoEvaluacion,
        LocalDateTime fechaEvaluacion,
        String frecuenciaCardiaca,
        String frecuenciaRespiratoria,
        String presionArterial,
        String saturacionOxigeno,
        BigDecimal tallaCm,
        BigDecimal pesoKg,
        BigDecimal imc,
        Integer eva,
        String localizacionDolor,
        String tipoDolor,
        String examenFisicoSegmentario,
        String diagnosticosPresuntivos,
        List<GoniometriaItemRequest> goniometria,
        List<FuerzaMuscularItemRequest> fuerzaMuscular,
        List<EscalaFuncionalCalculadaResponse> escalasFuncionales,
        List<PruebaEspecialItemRequest> pruebasEspeciales,
        BigDecimal puntajeFuncionalPromedio,
        String interpretacionFuncional
) {
}
