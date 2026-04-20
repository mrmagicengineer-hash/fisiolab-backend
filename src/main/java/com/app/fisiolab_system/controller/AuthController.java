package com.app.fisiolab_system.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.app.fisiolab_system.dto.ChangePasswordRequest;
import com.app.fisiolab_system.dto.LoginRequest;
import com.app.fisiolab_system.dto.LoginResponse;
import com.app.fisiolab_system.dto.RefreshTokenRequest;
import com.app.fisiolab_system.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.servlet.http.HttpServletRequest;

// 1. Mark this class as a REST controller
//    It handles HTTP requests and returns JSON responses
@RestController

// 2. Define the base URL path for all endpoints in this controller
@RequestMapping("/auth")
@Tag(name = "Autenticacion", description = "Endpoints de autenticacion y gestion de contrasena")
public class AuthController {

    // 3. Inject the authentication service (business logic layer)
    private final AuthService authService;

    // 4. Constructor-based dependency injection
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // 5. Define a POST endpoint for user login: /auth/login
    @PostMapping("/login")
    @Operation(summary = "Iniciar sesion", description = "Autentica un usuario y retorna un token JWT")
    @ApiResponse(responseCode = "200", description = "Inicio de sesion exitoso")
    @ApiResponse(responseCode = "401", description = "Credenciales invalidas")
    public ResponseEntity<LoginResponse> login(

            // 6. Validate the incoming request body using annotations (e.g., @NotNull, @Email)
            //    Map the JSON request to a LoginRequest object
            @Validated @RequestBody LoginRequest request,
            HttpServletRequest httpRequest
    ){
        // Get client IP address for auditing purposes
        String clientIp = httpRequest.getRemoteAddr();
        // 7. Call the authentication service to process login
        //    It usually validates credentials and generates a JWT token
        LoginResponse response = authService.login(request, clientIp);

        // 8. Return HTTP 200 OK with the response (e.g., token, user info)
        return ResponseEntity.ok(response);
    }

    // Define a POST endpoint to refresh the access token: /auth/refresh
    @PostMapping("/refresh")
    @Operation(summary = "Renovar token",
               description = "Intercambia un refresh token valido por un nuevo JWT. Rota el refresh token en el proceso.")
    @ApiResponse(responseCode = "200", description = "Token renovado exitosamente")
    @ApiResponse(responseCode = "401", description = "Refresh token invalido, expirado o revocado")
    public ResponseEntity<LoginResponse> refresh(
            @Validated @RequestBody RefreshTokenRequest request,
            HttpServletRequest httpRequest
    ) {
        String clientIp = httpRequest.getRemoteAddr();
        LoginResponse response = authService.refreshToken(request.refreshToken(), clientIp);
        return ResponseEntity.ok(response);
    }

    // Define a POST endpoint to logout and revoke refresh tokens: /auth/logout
    @PostMapping("/logout")
    @Operation(summary = "Cerrar sesion",
               description = "Revoca todos los refresh tokens del usuario autenticado.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponse(responseCode = "200", description = "Sesion cerrada")
    @ApiResponse(responseCode = "401", description = "No autenticado")
    public ResponseEntity<String> logout(
            Authentication authentication,
            HttpServletRequest httpRequest
    ) {
        String email = authentication.getName();
        String clientIp = httpRequest.getRemoteAddr();
        authService.logout(email, clientIp);
        return ResponseEntity.ok("Sesion cerrada exitosamente");
    }

    // 6. Define a POST endpoint for changing password: /auth/change-password
    @PostMapping("/change-password")
    @Operation(summary = "Cambiar contrasena", description = "Permite al usuario autenticado actualizar su contrasena")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponse(responseCode = "200", description = "Contrasena actualizada")
    @ApiResponse(responseCode = "401", description = "No autenticado")
    public ResponseEntity<String> changePassword(
        @Validated @RequestBody ChangePasswordRequest request,
        Authentication authentication,
        HttpServletRequest httpRequest
    ){
        String email =authentication.getName();
        String ip = httpRequest.getRemoteAddr();

        authService.changePassword(email, request, ip);
        return ResponseEntity.ok("Contraseña cambiada exitosamente");
    }
}