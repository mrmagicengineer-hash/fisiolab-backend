package com.app.fisiolab_system.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.app.fisiolab_system.model.RolUsuario;
import com.app.fisiolab_system.model.Usuario;
import com.app.fisiolab_system.service.JwtService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/test")
@Tag(name = "Pruebas", description = "Endpoints de prueba del sistema")
public class TestController {
    private final JwtService jwtService;

    // Constructor injection for JwtService
    public TestController(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    /**
     * Endpoint de prueba para generar un token JWT
     */

    @GetMapping("/token")
    @Operation(summary = "Generar token de prueba", description = "Genera un JWT de prueba con un usuario hardcodeado")
    @ApiResponse(responseCode = "200", description = "Token generado")
    public String testToken(){
        // Create a user object for testing
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setEmail("androd2000x@gmail.com");
        usuario.setRol(RolUsuario.ADMINISTRADOR);

        // Generate JWT token
        String token = jwtService.generateToken(usuario);
        return token;
    }
}
