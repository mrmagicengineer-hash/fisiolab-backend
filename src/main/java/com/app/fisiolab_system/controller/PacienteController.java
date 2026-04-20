package com.app.fisiolab_system.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.app.fisiolab_system.dto.ContextoAgendamientoResponse;
import com.app.fisiolab_system.dto.ContextoPlanesResponse;
import com.app.fisiolab_system.dto.CreatePacienteRequest;
import com.app.fisiolab_system.dto.FichaFamiliarRequest;
import com.app.fisiolab_system.dto.FichaFamiliarResponse;
import com.app.fisiolab_system.dto.PacienteResumenCompletoResponse;
import com.app.fisiolab_system.dto.PacienteResponse;
import com.app.fisiolab_system.dto.UpdatePacienteRequest;
import com.app.fisiolab_system.service.EpisodioClinicoService;
import com.app.fisiolab_system.service.PacienteService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/pacientes")
@Tag(name = "Tarjetero Indice", description = "Punto de entrada unico para registro y busqueda de pacientes")
@SecurityRequirement(name = "bearerAuth")
public class PacienteController {

    private final PacienteService pacienteService;
    private final EpisodioClinicoService episodioClinicoService;

    public PacienteController(PacienteService pacienteService, EpisodioClinicoService episodioClinicoService) {
        this.pacienteService = pacienteService;
        this.episodioClinicoService = episodioClinicoService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','FISIOTERAPEUTA')")
    @Operation(summary = "Registrar paciente", description = "RF-07. Registra un paciente nuevo y genera automaticamente su numero HCL")
    @ApiResponse(responseCode = "200", description = "Paciente registrado correctamente")
    @ApiResponse(responseCode = "403", description = "Acceso denegado")
    public ResponseEntity<PacienteResponse> registrar(
            @Validated @RequestBody CreatePacienteRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {
        return ResponseEntity.ok(
                pacienteService.registrarPaciente(request, authentication.getName(), httpRequest.getRemoteAddr()));
    }

    @GetMapping("/busqueda")
    @Operation(summary = "Buscar paciente", description = "RF-08. Busca por cedula, numero HCL o nombres completos. Requiere minimo 3 caracteres")
    @ApiResponse(responseCode = "200", description = "Resultados de busqueda")
    public ResponseEntity<List<PacienteResponse>> buscar(@RequestParam("q") String query) {
        return ResponseEntity.ok(pacienteService.buscarPacientes(query));
    }

    @GetMapping
    @Operation(summary = "Listar pacientes registrados", description = "Devuelve el listado completo de pacientes para poblar la tabla principal del dashboard")
    @ApiResponse(responseCode = "200", description = "Listado obtenido correctamente")
    public ResponseEntity<List<PacienteResponse>> listarPacientes() {
        return ResponseEntity.ok(pacienteService.listarPacientesRegistrados());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener detalle de paciente", description = "Obtiene la ficha del paciente por su identificador interno")
    @ApiResponse(responseCode = "200", description = "Paciente encontrado")
    public ResponseEntity<PacienteResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(pacienteService.obtenerPorId(id));
    }

    @GetMapping("/{id}/contexto-agendamiento")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','FISIOTERAPEUTA')")
    @Operation(
            summary = "Contexto de agendamiento del paciente",
            description = "RF-50. Devuelve episodios abiertos/admitidos con sus planes activos y sesiones restantes. Alimenta el combobox de la agenda.")
    @ApiResponse(responseCode = "200", description = "Contexto obtenido")
    public ResponseEntity<ContextoAgendamientoResponse> getContextoAgendamiento(@PathVariable Long id) {
        return ResponseEntity.ok(pacienteService.getContextoAgendamiento(id));
    }

    @GetMapping("/{id}/resumen")
    @Operation(summary = "Obtener resumen completo del paciente", description = "Devuelve todos los datos del paciente: datos demograficos, ficha familiar, historia clinica, antecedentes personales/familiares, problemas activos, episodios clinicos y conteo de evaluaciones")
    @ApiResponse(responseCode = "200", description = "Resumen completo obtenido")
    public ResponseEntity<PacienteResumenCompletoResponse> obtenerResumen(@PathVariable Long id) {
        return ResponseEntity.ok(pacienteService.obtenerResumenCompleto(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','FISIOTERAPEUTA')")
    @Operation(summary = "Editar datos del paciente", description = "RF-10. Edita datos de la ficha del paciente y registra auditoria")
    @ApiResponse(responseCode = "200", description = "Paciente actualizado")
    @ApiResponse(responseCode = "403", description = "Acceso denegado")
    public ResponseEntity<PacienteResponse> actualizar(
            @PathVariable Long id,
            @Validated @RequestBody UpdatePacienteRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {
        return ResponseEntity.ok(
                pacienteService.actualizarPaciente(id, request, authentication.getName(), httpRequest.getRemoteAddr()));
    }

    @PostMapping("/{id}/ficha-familiar")
    @PreAuthorize("hasRole('FISIOTERAPEUTA')")
    @Operation(summary = "Registrar o actualizar ficha familiar", description = "RF-12. Registra jefe de hogar, miembros, tipo de vivienda y condiciones sanitarias")
    @ApiResponse(responseCode = "200", description = "Ficha familiar guardada")
    @ApiResponse(responseCode = "403", description = "Acceso denegado")
    public ResponseEntity<FichaFamiliarResponse> guardarFichaFamiliar(
            @PathVariable Long id,
            @Validated @RequestBody FichaFamiliarRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {
        return ResponseEntity.ok(
                pacienteService.guardarFichaFamiliar(id, request, authentication.getName(), httpRequest.getRemoteAddr()));
    }

    @GetMapping("/{id}/ficha-familiar")
    @Operation(summary = "Obtener ficha familiar", description = "Consulta la ficha familiar registrada para un paciente")
    @ApiResponse(responseCode = "200", description = "Ficha familiar encontrada")
    public ResponseEntity<FichaFamiliarResponse> obtenerFichaFamiliar(@PathVariable Long id) {
        return ResponseEntity.ok(pacienteService.obtenerFichaFamiliar(id));
    }

    @GetMapping("/{id}/contexto-planes")
    @PreAuthorize("hasAnyRole('FISIOTERAPEUTA', 'MEDICO', 'ADMINISTRADOR')")
    @Operation(
        summary = "Contexto de planes del paciente",
        description = "Devuelve episodios ABIERTO/ADMITIDO del paciente con sus planes de tratamiento "
            + "e indicador de avance (sesiones realizadas vs planificadas). "
            + "Usado para poblar el panel de detalle al seleccionar un paciente.")
    @ApiResponse(responseCode = "200", description = "Contexto de planes obtenido")
    @ApiResponse(responseCode = "400", description = "Paciente o historia clínica no encontrados")
    public ResponseEntity<ContextoPlanesResponse> contextoPlanesDelPaciente(@PathVariable Long id) {
        return ResponseEntity.ok(episodioClinicoService.contextoPlanes(id));
    }

    @PostMapping("/archivo/actualizar")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Actualizar archivos pasivos", description = "RF-11. Ejecuta el cambio automatico de ACTIVO a PASIVO para historias sin atencion en 5 anos")
    @ApiResponse(responseCode = "200", description = "Proceso ejecutado")
    public ResponseEntity<Map<String, Integer>> actualizarArchivoPasivo() {
        int total = pacienteService.actualizarArchivosPasivosAutomaticamente();
        return ResponseEntity.ok(Map.of("pacientesActualizados", total));
    }
}
