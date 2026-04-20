package com.app.fisiolab_system.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.app.fisiolab_system.dto.CreateProblemaEpisodioRequest;
import com.app.fisiolab_system.dto.ProblemaEpisodioResponse;
import com.app.fisiolab_system.service.ProblemaEpisodioService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/episodios-clinicos/{episodioId}/problemas")
@Tag(name = "Problemas de Episodio", description = "RF-29: Registro de problemas en el episodio clínico")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class ProblemaEpisodioController {
    private final ProblemaEpisodioService problemaEpisodioService;

    @PostMapping
    @PreAuthorize("hasRole('FISIOTERAPEUTA')")
    @Operation(summary = "Registrar problema", description = "RF-29. Agrega un problema a la lista del episodio clínico")
    @ApiResponse(responseCode = "200", description = "Problema registrado")
    public ResponseEntity<ProblemaEpisodioResponse> registrarProblema(
            @PathVariable Long episodioId,
            @Validated @RequestBody CreateProblemaEpisodioRequest request) {
        return ResponseEntity.ok(problemaEpisodioService.registrarProblema(episodioId, request));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('FISIOTERAPEUTA','ADMINISTRADOR','MEDICO')")
    @Operation(summary = "Listar problemas", description = "Lista todos los problemas registrados en el episodio clínico")
    @ApiResponse(responseCode = "200", description = "Lista de problemas obtenida")
    public ResponseEntity<List<ProblemaEpisodioResponse>> listarProblemas(@PathVariable Long episodioId) {
        return ResponseEntity.ok(problemaEpisodioService.listarPorEpisodio(episodioId));
    }
}
