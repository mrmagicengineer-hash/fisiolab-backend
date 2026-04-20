package com.app.fisiolab_system.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.app.fisiolab_system.dto.BloqueoResponse;
import com.app.fisiolab_system.dto.CrearBloqueoRequest;
import com.app.fisiolab_system.service.BloqueoAgendaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/agenda/bloqueos")
@Tag(name = "Bloqueos de Agenda", description = "RF-53. Gestión de tiempos no disponibles del personal (vacaciones, permisos, feriados)")
@SecurityRequirement(name = "bearerAuth")
public class BloqueoAgendaController {

    private final BloqueoAgendaService bloqueoAgendaService;

    public BloqueoAgendaController(BloqueoAgendaService bloqueoAgendaService) {
        this.bloqueoAgendaService = bloqueoAgendaService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(
            summary = "Crear bloqueo de agenda",
            description = "RF-53. Registra un período no disponible para un profesional. Solo ADMINISTRADOR.")
    @ApiResponse(responseCode = "201", description = "Bloqueo creado correctamente")
    @ApiResponse(responseCode = "400", description = "Datos invalidos")
    @ApiResponse(responseCode = "403", description = "Acceso denegado — solo ADMINISTRADOR")
    public ResponseEntity<BloqueoResponse> crearBloqueo(
            @Validated @RequestBody CrearBloqueoRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {
        BloqueoResponse response = bloqueoAgendaService.crearBloqueo(
                request, authentication.getName(), httpRequest.getRemoteAddr());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','FISIOTERAPEUTA')")
    @Operation(
            summary = "Listar bloqueos de agenda",
            description = "FISIOTERAPEUTA ve solo sus bloqueos. ADMINISTRADOR ve todos.")
    @ApiResponse(responseCode = "200", description = "Listado obtenido correctamente")
    public ResponseEntity<List<BloqueoResponse>> listarBloqueos(Authentication authentication) {
        return ResponseEntity.ok(bloqueoAgendaService.listarBloqueos(authentication.getName()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(
            summary = "Eliminar bloqueo de agenda",
            description = "RF-53. Elimina un bloqueo de agenda. Solo ADMINISTRADOR.")
    @ApiResponse(responseCode = "204", description = "Bloqueo eliminado")
    @ApiResponse(responseCode = "403", description = "Acceso denegado — solo ADMINISTRADOR")
    @ApiResponse(responseCode = "404", description = "Bloqueo no encontrado")
    public ResponseEntity<Void> eliminarBloqueo(
            @PathVariable Long id,
            Authentication authentication,
            HttpServletRequest httpRequest) {
        bloqueoAgendaService.eliminarBloqueo(id, authentication.getName(), httpRequest.getRemoteAddr());
        return ResponseEntity.noContent().build();
    }
}
