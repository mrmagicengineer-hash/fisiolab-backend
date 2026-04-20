package com.app.fisiolab_system.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.app.fisiolab_system.dto.EstadisticasDashboardResponse;
import com.app.fisiolab_system.dto.PacienteResumenPlanesResponse;
import com.app.fisiolab_system.dto.SesionTerapiaResponse;
import com.app.fisiolab_system.dto.TimelineItemResponse;
import com.app.fisiolab_system.service.PlanTratamientoService;
import com.app.fisiolab_system.service.SesionTerapiaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/planes-tratamiento")
@Tag(name = "Planes de Tratamiento - Global", description = "Vistas globales y de navegación para planes de tratamiento")
@SecurityRequirement(name = "bearerAuth")
public class PlanTratamientoGlobalController {

    private final PlanTratamientoService planService;
    private final SesionTerapiaService sesionService;

    public PlanTratamientoGlobalController(PlanTratamientoService planService, SesionTerapiaService sesionService) {
        this.planService = planService;
        this.sesionService = sesionService;
    }

    @GetMapping("/resumen-pacientes")
    @PreAuthorize("hasAnyRole('FISIOTERAPEUTA', 'MEDICO', 'ADMINISTRADOR')")
    @Operation(
        summary = "Listar pacientes con planes activos",
        description = "Devuelve la lista de pacientes que tienen al menos un plan de tratamiento ACTIVO. "
            + "Incluye el nombre del paciente, número de HCL, peor código de alarma entre todos sus planes "
            + "(VERDE < AMARILLO < NARANJA < ROJO) y el conteo de planes activos. "
            + "Ordenado alfabéticamente por nombre de paciente.")
    @ApiResponse(responseCode = "200", description = "Lista de pacientes con resumen de planes")
    public ResponseEntity<List<PacienteResumenPlanesResponse>> resumenPorPaciente() {
        return ResponseEntity.ok(planService.listarResumenPorPaciente());
    }

    @GetMapping("/{planId}/timeline")
    @PreAuthorize("hasAnyRole('FISIOTERAPEUTA', 'MEDICO', 'ADMINISTRADOR')")
    @Operation(
        summary = "Línea de tiempo del plan",
        description = "Lista cronológica de sesiones SOAP y seguimientos del plan. "
            + "Cada ítem incluye tipo (SESION_SOAP | SEGUIMIENTO_PLAN) para que el frontend elija el ícono.")
    @ApiResponse(responseCode = "200", description = "Timeline del plan")
    @ApiResponse(responseCode = "400", description = "Plan no encontrado")
    public ResponseEntity<List<TimelineItemResponse>> timeline(@PathVariable Long planId) {
        return ResponseEntity.ok(planService.getTimeline(planId));
    }

    @PostMapping("/{planId}/iniciar-sesion-directa")
    @PreAuthorize("hasRole('FISIOTERAPEUTA')")
    @Operation(
        summary = "Iniciar sesión directa",
        description = "Crea una sesión vinculada al plan sin pasar por la agenda. "
            + "Útil para atender pacientes que llegan de imprevisto. "
            + "Crea una cita fantasma REALIZADA y abre la NotaSOAP en borrador.")
    @ApiResponse(responseCode = "200", description = "Sesión creada y en progreso")
    @ApiResponse(responseCode = "400", description = "Plan no encontrado o no ACTIVO")
    @ApiResponse(responseCode = "403", description = "Solo FISIOTERAPEUTA puede iniciar sesiones")
    public ResponseEntity<SesionTerapiaResponse> iniciarSesionDirecta(
            @PathVariable Long planId,
            Authentication authentication,
            jakarta.servlet.http.HttpServletRequest httpRequest) {
        return ResponseEntity.ok(sesionService.iniciarSesionDirecta(
            planId, authentication.getName(), httpRequest.getRemoteAddr()));
    }

    @GetMapping("/estadisticas-dashboard")
    @PreAuthorize("hasAnyRole('FISIOTERAPEUTA', 'MEDICO', 'ADMINISTRADOR')")
    @Operation(
        summary = "Estadísticas globales del dashboard de planes",
        description = "Devuelve contadores para los filtros superiores: total de planes ACTIVOS, "
            + "en riesgo (alarma NARANJA o ROJO) y finalizando (≥80% de sesiones realizadas).")
    @ApiResponse(responseCode = "200", description = "Contadores del dashboard")
    public ResponseEntity<EstadisticasDashboardResponse> estadisticasDashboard() {
        return ResponseEntity.ok(planService.obtenerEstadisticasDashboard());
    }
}
