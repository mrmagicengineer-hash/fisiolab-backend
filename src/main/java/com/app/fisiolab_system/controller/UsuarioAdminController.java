package com.app.fisiolab_system.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.app.fisiolab_system.dto.CreateUsuarioRequest;
import com.app.fisiolab_system.dto.UsuarioAdminResponse;
import com.app.fisiolab_system.service.UsuarioAdminService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/admin/usuarios")
@Tag(name = "Administracion", description = "Gestion de usuarios por administradores")
@SecurityRequirement(name = "bearerAuth")
public class UsuarioAdminController {
    private final UsuarioAdminService usuarioAdminService;

    public UsuarioAdminController(UsuarioAdminService usuarioAdminService) {
        this.usuarioAdminService = usuarioAdminService;
    }

    @GetMapping
    @Operation(summary = "Listar usuarios", description = "Obtiene todos los usuarios para el modulo administrador")
    @ApiResponse(responseCode = "200", description = "Listado obtenido correctamente")
    @ApiResponse(responseCode = "403", description = "Acceso denegado")
    public ResponseEntity<List<UsuarioAdminResponse>> listar() {
        return ResponseEntity.ok(usuarioAdminService.listar());
    }

    @GetMapping("/activos")
    @Operation(summary = "Listar usuarios activos", description = "Obtiene usuarios con cuenta activa")
    @ApiResponse(responseCode = "200", description = "Listado obtenido correctamente")
    @ApiResponse(responseCode = "403", description = "Acceso denegado")
    public ResponseEntity<List<UsuarioAdminResponse>> listarActivos() {
        return ResponseEntity.ok(usuarioAdminService.listarActivos());
    }


    @GetMapping("/desactivados")
    @Operation(summary = "Listar usuarios desactivados", description = "Obtiene usuarios con cuenta desactivada")
    @ApiResponse(responseCode = "200", description = "Listado obtenido correctamente")
    @ApiResponse(responseCode = "403", description = "Acceso denegado")
    public ResponseEntity<List<UsuarioAdminResponse>> listarDesactivados() {
        return ResponseEntity.ok(usuarioAdminService.listarDesactivados());
    }

    @GetMapping("/bloqueados")
    @Operation(summary = "Listar usuarios bloqueados", description = "Obtiene usuarios con cuenta bloqueada")
    @ApiResponse(responseCode = "200", description = "Listado obtenido correctamente")
    @ApiResponse(responseCode = "403", description = "Acceso denegado")
    public ResponseEntity<List<UsuarioAdminResponse>> listarBloqueados() {
        return ResponseEntity.ok(usuarioAdminService.listarBloqueados());
    }

    // Step 1: Create a professional account 
    @PostMapping
    @Operation(summary = "Crear usuario profesional", description = "Crea una cuenta profesional desde el modulo administrador")
    @ApiResponse(responseCode = "200", description = "Usuario creado correctamente")
    @ApiResponse(responseCode = "403", description = "Acceso denegado")
    public ResponseEntity<UsuarioAdminResponse> crear(
            @Validated @RequestBody CreateUsuarioRequest request,
            HttpServletRequest httpRequest
    ){
        String ip = httpRequest.getRemoteAddr();
        return ResponseEntity.ok(usuarioAdminService.createProfessional(request, ip));
    }

    @PatchMapping("/{id}/desactivar-bloqueo")
    @Operation(summary = "Desactivar bloqueo temporal", description = "Quita el tiempo de bloqueo de login y reinicia intentos fallidos")
    @ApiResponse(responseCode = "200", description = "Bloqueo temporal removido")
    @ApiResponse(responseCode = "400", description = "Usuario no encontrado")
    @ApiResponse(responseCode = "403", description = "Acceso denegado")
    public ResponseEntity<UsuarioAdminResponse> desactivarBloqueo(
            @PathVariable Long id,
            HttpServletRequest httpRequest
    ) {
        String ip = httpRequest.getRemoteAddr();
        return ResponseEntity.ok(usuarioAdminService.desactivarTiempoBloqueo(id, ip));
    }
}
