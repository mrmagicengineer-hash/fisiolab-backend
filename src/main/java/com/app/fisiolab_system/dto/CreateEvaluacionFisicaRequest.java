package com.app.fisiolab_system.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.app.fisiolab_system.model.TipoEvaluacionFisioterapeutica;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateEvaluacionFisicaRequest(
        @NotNull TipoEvaluacionFisioterapeutica tipoEvaluacion,
        LocalDateTime fechaEvaluacion,
        @Size(max = 60) String frecuenciaCardiaca,
        @Size(max = 60) String frecuenciaRespiratoria,
        @Size(max = 60) String presionArterial,
        @Size(max = 60) String saturacionOxigeno,
        @NotNull @DecimalMin(value = "30.0") @DecimalMax(value = "250.0") BigDecimal tallaCm,
        @NotNull @DecimalMin(value = "1.0") @DecimalMax(value = "500.0") BigDecimal pesoKg,
        @NotNull @Min(0) @Max(10) Integer eva,
        @NotBlank @Size(max = 255) String localizacionDolor,
        @NotBlank @Size(max = 120) String tipoDolor,
        @NotBlank @Size(max = 2000) String examenFisicoSegmentario,
        @NotBlank @Size(max = 2000) String diagnosticosPresuntivos,
        @NotEmpty List<@Valid GoniometriaItemRequest> goniometria,
        @NotEmpty List<@Valid FuerzaMuscularItemRequest> fuerzaMuscular,
        @NotEmpty List<@Valid EscalaFuncionalItemRequest> escalasFuncionales,
        @NotEmpty List<@Valid PruebaEspecialItemRequest> pruebasEspeciales
) {
}
