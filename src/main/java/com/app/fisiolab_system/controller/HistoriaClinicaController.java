package com.app.fisiolab_system.controller;

import java.util.List;
import java.util.Map;

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

import com.app.fisiolab_system.dto.AntecedenteFamiliarResponse;
import com.app.fisiolab_system.dto.AntecedentePersonalResponse;
import com.app.fisiolab_system.dto.CreateAntecedenteFamiliarRequest;
import com.app.fisiolab_system.dto.CreateAntecedentePersonalRequest;
import com.app.fisiolab_system.dto.HistoriaClinicaCompletaResponse;
import com.app.fisiolab_system.dto.HistoriaClinicaResumenResponse;
import com.app.fisiolab_system.service.HistoriaClinicaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/historias-clinicas")
@Tag(name = "Historias Clinicas", description = "Modulo 3: historia clinica y antecedentes")
@SecurityRequirement(name = "bearerAuth")
public class HistoriaClinicaController {

    private final HistoriaClinicaService historiaClinicaService;

    public HistoriaClinicaController(HistoriaClinicaService historiaClinicaService) {
        this.historiaClinicaService = historiaClinicaService;
    }

    @PostMapping("/{numeroHcl}/antecedentes/personales")
    @PreAuthorize("hasAnyRole('FISIOTERAPEUTA','MEDICO')")
    @Operation(summary = "Registrar antecedente personal", description = "RF-14. Registra antecedentes patologicos, quirurgicos, traumaticos, alergicos, farmacologicos y gineco-obstetricos")
    @ApiResponse(responseCode = "200", description = "Antecedente personal registrado")
    public ResponseEntity<AntecedentePersonalResponse> registrarAntecedentePersonal(
            @PathVariable String numeroHcl,
            @Validated @RequestBody CreateAntecedentePersonalRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {
        return ResponseEntity.ok(historiaClinicaService.registrarAntecedentePersonal(
                numeroHcl,
                request,
                authentication.getName(),
                httpRequest.getRemoteAddr()));
    }

    @PostMapping("/{numeroHcl}/antecedentes/familiares")
    @PreAuthorize("hasAnyRole('FISIOTERAPEUTA','MEDICO')")
    @Operation(summary = "Registrar antecedente familiar", description = "RF-15. Registra antecedente familiar indicando parentesco y condicion")
    @ApiResponse(responseCode = "200", description = "Antecedente familiar registrado")
    public ResponseEntity<AntecedenteFamiliarResponse> registrarAntecedenteFamiliar(
            @PathVariable String numeroHcl,
            @Validated @RequestBody CreateAntecedenteFamiliarRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {
        return ResponseEntity.ok(historiaClinicaService.registrarAntecedenteFamiliar(
                numeroHcl,
                request,
                authentication.getName(),
                httpRequest.getRemoteAddr()));
    }

    @GetMapping("/{numeroHcl}/antecedentes/personales")
    @PreAuthorize("hasAnyRole('FISIOTERAPEUTA','MEDICO')")
    @Operation(summary = "Listar antecedentes personales", description = "Obtiene los antecedentes personales de una historia clinica por numero HCL")
    @ApiResponse(responseCode = "200", description = "Listado de antecedentes personales")
    public ResponseEntity<List<AntecedentePersonalResponse>> listarAntecedentesPersonales(
            @PathVariable String numeroHcl) {
        return ResponseEntity.ok(historiaClinicaService.listarAntecedentesPersonales(numeroHcl));
    }

    @GetMapping("/{numeroHcl}/antecedentes/familiares")
    @PreAuthorize("hasAnyRole('FISIOTERAPEUTA','MEDICO')")
    @Operation(summary = "Listar antecedentes familiares", description = "Obtiene los antecedentes familiares de una historia clinica por numero HCL")
    @ApiResponse(responseCode = "200", description = "Listado de antecedentes familiares")
    public ResponseEntity<List<AntecedenteFamiliarResponse>> listarAntecedentesFamiliares(
            @PathVariable String numeroHcl) {
        return ResponseEntity.ok(historiaClinicaService.listarAntecedentesFamiliares(numeroHcl));
    }

    @GetMapping("/{numeroHcl}/completa")
    @PreAuthorize("hasAnyRole('FISIOTERAPEUTA','MEDICO')")
    @Operation(summary = "Visualizar historia clinica completa", description = "RF-16. Devuelve antecedentes, problemas activos, episodios previos y estado del archivo")
    @ApiResponse(responseCode = "200", description = "Historia clinica completa")
    public ResponseEntity<HistoriaClinicaCompletaResponse> visualizarCompleta(@PathVariable String numeroHcl) {
        return ResponseEntity.ok(historiaClinicaService.obtenerHistoriaCompleta(numeroHcl));
    }

    @GetMapping("/por-paciente/{pacienteId}")
    @Operation(summary = "Obtener resumen de HC por paciente", description = "Obtiene el identificador y datos base de la historia clinica de un paciente")
    @ApiResponse(responseCode = "200", description = "Resumen de HC")
    public ResponseEntity<HistoriaClinicaResumenResponse> obtenerPorPaciente(@PathVariable Long pacienteId) {
        return ResponseEntity.ok(historiaClinicaService.obtenerResumenPorPaciente(pacienteId));
    }

    @GetMapping("/depuracion/candidatas")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Listar HCs candidatas a depuracion", description = "RF-17. Lista historias en archivo pasivo con inactividad de 15+ anos para revision")
    @ApiResponse(responseCode = "200", description = "Listado de candidatas")
    public ResponseEntity<List<HistoriaClinicaResumenResponse>> listarCandidatasDepuracion() {
        return ResponseEntity.ok(historiaClinicaService.listarCandidatasDepuracion());
    }

    @DeleteMapping("/{numeroHcl}/depuracion")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Ejecutar depuracion de HC", description = "RF-17. Elimina antecedentes e historia clinica tras aprobacion administrativa")
    @ApiResponse(responseCode = "200", description = "Depuracion ejecutada")
    public ResponseEntity<Map<String, String>> depurar(
            @PathVariable String numeroHcl,
            Authentication authentication,
            HttpServletRequest httpRequest) {
        historiaClinicaService.depurarHistoriaClinica(numeroHcl, authentication.getName(), httpRequest.getRemoteAddr());
        return ResponseEntity.ok(Map.of("resultado", "Historia clinica depurada correctamente"));
    }
}
