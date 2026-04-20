package com.app.fisiolab_system.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegistrarBiometriaRequest(

        LocalDateTime fechaRegistro,

        @NotNull(message = "El peso es obligatorio")
        @DecimalMin(value = "1.0", message = "El peso debe ser mayor a 1 kg")
        @DecimalMax(value = "500.0", message = "El peso no puede superar 500 kg")
        BigDecimal pesoKg,

        @NotNull(message = "La talla es obligatoria")
        @DecimalMin(value = "30.0", message = "La talla debe ser mayor a 30 cm")
        @DecimalMax(value = "250.0", message = "La talla no puede superar 250 cm")
        BigDecimal tallaCm,

        @DecimalMin(value = "10.0", message = "El perimetro de cintura debe ser mayor a 10 cm")
        @DecimalMax(value = "300.0", message = "El perimetro de cintura no puede superar 300 cm")
        BigDecimal perimetroCinturaCm,

        @DecimalMin(value = "10.0", message = "El perimetro de cadera debe ser mayor a 10 cm")
        @DecimalMax(value = "300.0", message = "El perimetro de cadera no puede superar 300 cm")
        BigDecimal perimetroCaderaCm,

        @Min(value = 50, message = "La presion sistolica minima es 50 mmHg")
        @Max(value = 300, message = "La presion sistolica maxima es 300 mmHg")
        Integer presionArterialSistolica,

        @Min(value = 20, message = "La presion diastolica minima es 20 mmHg")
        @Max(value = 200, message = "La presion diastolica maxima es 200 mmHg")
        Integer presionArterialDiastolica,

        @Min(value = 20, message = "La frecuencia cardiaca minima es 20 lpm")
        @Max(value = 300, message = "La frecuencia cardiaca maxima es 300 lpm")
        Integer frecuenciaCardiaca,

        @Min(value = 4, message = "La frecuencia respiratoria minima es 4 rpm")
        @Max(value = 60, message = "La frecuencia respiratoria maxima es 60 rpm")
        Integer frecuenciaRespiratoria,

        @DecimalMin(value = "30.0", message = "La temperatura minima es 30 °C")
        @DecimalMax(value = "45.0", message = "La temperatura maxima es 45 °C")
        BigDecimal temperatura,

        @DecimalMin(value = "50.0", message = "La saturacion de oxigeno minima es 50%")
        @DecimalMax(value = "100.0", message = "La saturacion de oxigeno maxima es 100%")
        BigDecimal saturacionOxigeno,

        @Size(max = 500, message = "Las observaciones no pueden superar 500 caracteres")
        String observaciones
) {
}
