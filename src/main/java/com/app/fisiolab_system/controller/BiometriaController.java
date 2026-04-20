package com.app.fisiolab_system.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.app.fisiolab_system.dto.BiometriaResponse;
import com.app.fisiolab_system.dto.RegistrarBiometriaRequest;
import com.app.fisiolab_system.service.BiometriaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/episodios-clinicos/{episodioId}/biometria")
@Tag(name = "Biometria", description = "Registro y consulta de signos vitales y medidas antropometricas por episodio clinico")
@SecurityRequirement(name = "bearerAuth")
public class BiometriaController {

    private final BiometriaService biometriaService;

    public BiometriaController(BiometriaService biometriaService) {
        this.biometriaService = biometriaService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','FISIOTERAPEUTA')")
    @Operation(
            summary = "Registrar biometria",
            description = "Registra signos vitales y medidas antropometricas (peso, talla, IMC, presion arterial, etc.) para un episodio clinico abierto. El IMC se calcula automaticamente.")
    @ApiResponse(responseCode = "200", description = "Biometria registrada")
    public ResponseEntity<BiometriaResponse> registrar(
            @PathVariable Long episodioId,
            @Validated @RequestBody RegistrarBiometriaRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {
        return ResponseEntity.ok(biometriaService.registrar(
                episodioId,
                request,
                authentication.getName(),
                httpRequest.getRemoteAddr()));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','FISIOTERAPEUTA','MEDICO')")
    @Operation(
            summary = "Listar biometrias del episodio",
            description = "Devuelve todos los registros de biometria del episodio en orden cronologico ascendente.")
    @ApiResponse(responseCode = "200", description = "Biometrias obtenidas")
    public ResponseEntity<List<BiometriaResponse>> listar(@PathVariable Long episodioId) {
        return ResponseEntity.ok(biometriaService.listarPorEpisodio(episodioId));
    }

    @GetMapping("/{biometriaId}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','FISIOTERAPEUTA','MEDICO')")
    @Operation(
            summary = "Obtener biometria por ID",
            description = "Devuelve el detalle completo de un registro de biometria del episodio.")
    @ApiResponse(responseCode = "200", description = "Biometria obtenida")
    public ResponseEntity<BiometriaResponse> obtener(
            @PathVariable Long episodioId,
            @PathVariable Long biometriaId) {
        return ResponseEntity.ok(biometriaService.obtenerPorId(episodioId, biometriaId));
    }
}
