package com.app.fisiolab_system.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.app.fisiolab_system.dto.CreateEvaluacionClinicaRequest;
import com.app.fisiolab_system.dto.EvaluacionClinicaResponse;
import com.app.fisiolab_system.dto.UpdateEvaluacionClinicaRequest;
import com.app.fisiolab_system.service.EvaluacionClinicaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/episodios-clinicos/{episodioId}/evaluaciones-clinicas")
@Tag(name = "Evaluaciones Clinicas", description = "CRUD de evaluaciones clinicas rapidas por episodio")
@SecurityRequirement(name = "bearerAuth")
public class EvaluacionClinicaController {

    private final EvaluacionClinicaService evaluacionClinicaService;

    public EvaluacionClinicaController(EvaluacionClinicaService evaluacionClinicaService) {
        this.evaluacionClinicaService = evaluacionClinicaService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','FISIOTERAPEUTA')")
    @Operation(summary = "Crear evaluacion clinica", description = "Registra una evaluacion clinica rapida para el episodio")
    @ApiResponse(responseCode = "200", description = "Evaluacion creada")
    public ResponseEntity<EvaluacionClinicaResponse> crear(
            @PathVariable Long episodioId,
            @Validated @RequestBody CreateEvaluacionClinicaRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {
        return ResponseEntity.ok(evaluacionClinicaService.crear(
                episodioId,
                request,
                authentication.getName(),
                httpRequest.getRemoteAddr()));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','FISIOTERAPEUTA','MEDICO')")
    @Operation(summary = "Listar evaluaciones clinicas", description = "Devuelve todas las evaluaciones clinicas del episodio en orden cronologico")
    @ApiResponse(responseCode = "200", description = "Evaluaciones obtenidas")
    public ResponseEntity<List<EvaluacionClinicaResponse>> listar(@PathVariable Long episodioId) {
        return ResponseEntity.ok(evaluacionClinicaService.listarPorEpisodio(episodioId));
    }

    @GetMapping("/{evaluacionId}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','FISIOTERAPEUTA','MEDICO')")
    @Operation(summary = "Obtener evaluacion clinica", description = "Retorna el detalle de una evaluacion clinica especifica")
    @ApiResponse(responseCode = "200", description = "Evaluacion obtenida")
    public ResponseEntity<EvaluacionClinicaResponse> obtener(
            @PathVariable Long episodioId,
            @PathVariable Long evaluacionId) {
        return ResponseEntity.ok(evaluacionClinicaService.obtenerPorId(episodioId, evaluacionId));
    }

    @PutMapping("/{evaluacionId}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','FISIOTERAPEUTA')")
    @Operation(summary = "Actualizar evaluacion clinica", description = "Actualiza parcialmente los campos de una evaluacion clinica")
    @ApiResponse(responseCode = "200", description = "Evaluacion actualizada")
    public ResponseEntity<EvaluacionClinicaResponse> actualizar(
            @PathVariable Long episodioId,
            @PathVariable Long evaluacionId,
            @Validated @RequestBody UpdateEvaluacionClinicaRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {
        return ResponseEntity.ok(evaluacionClinicaService.actualizar(
                episodioId,
                evaluacionId,
                request,
                authentication.getName(),
                httpRequest.getRemoteAddr()));
    }

    @DeleteMapping("/{evaluacionId}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Eliminar evaluacion clinica", description = "Elimina una evaluacion clinica del episodio")
    @ApiResponse(responseCode = "204", description = "Evaluacion eliminada")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long episodioId,
            @PathVariable Long evaluacionId,
            Authentication authentication,
            HttpServletRequest httpRequest) {
        evaluacionClinicaService.eliminar(
                episodioId,
                evaluacionId,
                authentication.getName(),
                httpRequest.getRemoteAddr());
        return ResponseEntity.noContent().build();
    }
}
