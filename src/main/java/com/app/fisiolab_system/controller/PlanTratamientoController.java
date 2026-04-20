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

import com.app.fisiolab_system.dto.CreatePlanTratamientoRequest;
import com.app.fisiolab_system.dto.CreateSeguimientoPlanRequest;
import com.app.fisiolab_system.dto.IndicadorAvancePlanResponse;
import com.app.fisiolab_system.dto.PlanTratamientoResponse;
import com.app.fisiolab_system.dto.SeguimientoPlanResponse;
import com.app.fisiolab_system.service.PlanTratamientoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/episodios-clinicos/{episodioId}/problemas/{problemaId}/plan")
@Tag(name = "Planes de Tratamiento", description = "RF-31, RF-32, RF-33: Gestión de planes de tratamiento y seguimiento")
@SecurityRequirement(name = "bearerAuth")
public class PlanTratamientoController {

    private final PlanTratamientoService planService;

    public PlanTratamientoController(PlanTratamientoService planService) {
        this.planService = planService;
    }

    // -------------------------------------------------------------------------
    // RF-31: Creación del plan
    // -------------------------------------------------------------------------

    @PostMapping
    @PreAuthorize("hasRole('FISIOTERAPEUTA')")
    @Operation(
            summary = "RF-31: Crear plan de tratamiento",
            description = "Crea un plan de tratamiento para un problema activo del episodio. "
                    + "Incluye objetivo general, objetivos específicos, fechas, número de sesiones, "
                    + "indicaciones educativas y código de alarma (semáforo).")
    @ApiResponse(responseCode = "200", description = "Plan creado correctamente")
    @ApiResponse(responseCode = "400", description = "Ya existe un plan o datos inválidos")
    @ApiResponse(responseCode = "403", description = "Acceso denegado")
    public ResponseEntity<PlanTratamientoResponse> crearPlan(
            @PathVariable Long episodioId,
            @PathVariable Long problemaId,
            @Validated @RequestBody CreatePlanTratamientoRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {
        return ResponseEntity.ok(planService.crearPlan(
                episodioId, problemaId, request,
                authentication.getName(), httpRequest.getRemoteAddr()));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('FISIOTERAPEUTA', 'MEDICO', 'ADMINISTRADOR')")
    @Operation(
            summary = "Obtener plan de tratamiento",
            description = "Devuelve el plan de tratamiento asociado al problema, "
                    + "incluyendo sesiones realizadas y porcentaje de avance.")
    @ApiResponse(responseCode = "200", description = "Plan encontrado")
    @ApiResponse(responseCode = "400", description = "Plan no encontrado")
    public ResponseEntity<PlanTratamientoResponse> obtenerPlan(
            @PathVariable Long episodioId,
            @PathVariable Long problemaId) {
        return ResponseEntity.ok(planService.obtenerPlanPorProblema(episodioId, problemaId));
    }

    // -------------------------------------------------------------------------
    // RF-32: Seguimiento del plan
    // -------------------------------------------------------------------------

    @PostMapping("/seguimientos")
    @PreAuthorize("hasRole('FISIOTERAPEUTA')")
    @Operation(
            summary = "RF-32: Registrar seguimiento del plan",
            description = "Registra una evaluación periódica del plan indicando avance, "
                    + "resultados obtenidos, ajustes y resultado general "
                    + "(MEJORA, ESTABLE, DETERIORO, ALTA, ABANDONO). "
                    + "El plan se cierra automáticamente si el resultado es ALTA o ABANDONO.")
    @ApiResponse(responseCode = "200", description = "Seguimiento registrado correctamente")
    @ApiResponse(responseCode = "400", description = "Plan no encontrado o estado inválido")
    @ApiResponse(responseCode = "403", description = "Acceso denegado")
    public ResponseEntity<SeguimientoPlanResponse> registrarSeguimiento(
            @PathVariable Long episodioId,
            @PathVariable Long problemaId,
            @Validated @RequestBody CreateSeguimientoPlanRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {
        return ResponseEntity.ok(planService.registrarSeguimiento(
                episodioId, problemaId, request,
                authentication.getName(), httpRequest.getRemoteAddr()));
    }

    @GetMapping("/seguimientos")
    @PreAuthorize("hasAnyRole('FISIOTERAPEUTA', 'MEDICO', 'ADMINISTRADOR')")
    @Operation(
            summary = "Listar seguimientos del plan",
            description = "Devuelve el historial de evaluaciones de seguimiento ordenadas por número de sesión.")
    @ApiResponse(responseCode = "200", description = "Historial de seguimientos")
    public ResponseEntity<List<SeguimientoPlanResponse>> listarSeguimientos(
            @PathVariable Long episodioId,
            @PathVariable Long problemaId) {
        return ResponseEntity.ok(planService.listarSeguimientos(episodioId, problemaId));
    }

    // -------------------------------------------------------------------------
    // RF-33: Indicador de avance de sesiones
    // -------------------------------------------------------------------------

    @GetMapping("/indicador")
    @PreAuthorize("hasAnyRole('FISIOTERAPEUTA', 'MEDICO', 'ADMINISTRADOR')")
    @Operation(
            summary = "RF-33: Indicador de avance de sesiones",
            description = "Devuelve el indicador visual del plan: sesiones realizadas vs planificadas "
                    + "(ej: 8/12), porcentaje de avance calculado y estado actual del plan.")
    @ApiResponse(responseCode = "200", description = "Indicador de avance")
    public ResponseEntity<IndicadorAvancePlanResponse> obtenerIndicador(
            @PathVariable Long episodioId,
            @PathVariable Long problemaId) {
        return ResponseEntity.ok(planService.obtenerIndicador(episodioId, problemaId));
    }
}
