package com.app.fisiolab_system.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.app.fisiolab_system.model.Auditoria;
import com.app.fisiolab_system.service.UsuarioAdminService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/admin/auditoria")
@Tag(name = "Auditoria", description = "Registro de acciones administrativas para auditoria")
@SecurityRequirement(name = "bearerAuth")
public class AuditoriaAdminController {
    private final UsuarioAdminService usuarioAdminService;

    public AuditoriaAdminController(UsuarioAdminService usuarioAdminService) {
        this.usuarioAdminService = usuarioAdminService;
    }

    @GetMapping("/eventos")
    @Operation(summary = "Listar eventos de auditoria", description = "Obtiene el historial de acciones administrativas para auditoria")
    @ApiResponse(responseCode = "200", description = "Listado obtenido correctamente")
    public ResponseEntity<List<Auditoria>> listarEventos() {
        return ResponseEntity.ok(usuarioAdminService.listarEventosAuditoria());
    }
}
