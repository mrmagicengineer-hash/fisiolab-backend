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

import com.app.fisiolab_system.dto.AdmisionEpisodioResponse;
import com.app.fisiolab_system.dto.ComparativaEvaluacionResponse;
import com.app.fisiolab_system.dto.CerrarEpisodioRequest;
import com.app.fisiolab_system.dto.CreateEvaluacionFisicaRequest;
import com.app.fisiolab_system.dto.CreateEpisodioClinicoRequest;
import com.app.fisiolab_system.dto.EgresoEpisodioResponse;
import com.app.fisiolab_system.dto.EvaluacionFisicaResponse;
import com.app.fisiolab_system.dto.EpisodioClinicoResponse;
import com.app.fisiolab_system.dto.EpisodioClinicoContenidoResponse;
import com.app.fisiolab_system.dto.PlanTratamientoResponse;
import com.app.fisiolab_system.dto.PuntoProgresoEvaluacionResponse;
import com.app.fisiolab_system.dto.RegistrarAdmisionRequest;
import com.app.fisiolab_system.dto.RegistrarEgresoRequest;
import com.app.fisiolab_system.dto.ConsultaResumeItemResponse;
import com.app.fisiolab_system.dto.PaginatedResponse;
import com.app.fisiolab_system.service.EvaluacionFisicaService;
import com.app.fisiolab_system.service.EpisodioClinicoService;
import com.app.fisiolab_system.service.PlanTratamientoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/episodios-clinicos")
@Tag(name = "Episodios Clinicos", description = "Modulo 4: episodios clinicos y admision")
@SecurityRequirement(name = "bearerAuth")
public class EpisodioClinicoController {

    private final EpisodioClinicoService episodioClinicoService;
    private final EvaluacionFisicaService evaluacionFisicaService;
    private final PlanTratamientoService planTratamientoService;

    public EpisodioClinicoController(
            EpisodioClinicoService episodioClinicoService,
            EvaluacionFisicaService evaluacionFisicaService,
            PlanTratamientoService planTratamientoService) {
        this.episodioClinicoService = episodioClinicoService;
        this.evaluacionFisicaService = evaluacionFisicaService;
        this.planTratamientoService = planTratamientoService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','FISIOTERAPEUTA')")
    @Operation(summary = "Abrir episodio clinico", description = "RF-18. Abre un nuevo episodio clinico para un paciente con secuencial por HCL")
    @ApiResponse(responseCode = "200", description = "Episodio abierto")
    public ResponseEntity<EpisodioClinicoResponse> abrirEpisodio(
            @Validated @RequestBody CreateEpisodioClinicoRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {
        return ResponseEntity.ok(episodioClinicoService.abrirEpisodio(
                request,
                authentication.getName(),
                httpRequest.getRemoteAddr()));
    }

    @PostMapping("/{episodioId}/admision")
    @PreAuthorize("hasRole('ADMINISTRADOR', 'FISIOTERAPEUTA')")
    @Operation(summary = "Registrar admision", description = "RF-19. Registra fecha/hora, tipo de atencion, motivo y profesional")
    @ApiResponse(responseCode = "200", description = "Admision registrada")
    public ResponseEntity<AdmisionEpisodioResponse> registrarAdmision(
            @PathVariable Long episodioId,
            @Validated @RequestBody RegistrarAdmisionRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {
        return ResponseEntity.ok(episodioClinicoService.registrarAdmision(
                episodioId,
                request,
                authentication.getName(),
                httpRequest.getRemoteAddr()));
    }

    @PostMapping("/{episodioId}/egreso")
    @PreAuthorize("hasRole('FISIOTERAPEUTA')")
    @Operation(summary = "Registrar egreso", description = "RF-20. Registra condicion de salida, causa de alta, destino y referencia")
    @ApiResponse(responseCode = "200", description = "Egreso registrado")
    public ResponseEntity<EgresoEpisodioResponse> registrarEgreso(
            @PathVariable Long episodioId,
            @Validated @RequestBody RegistrarEgresoRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {
        return ResponseEntity.ok(episodioClinicoService.registrarEgreso(
                episodioId,
                request,
                authentication.getName(),
                httpRequest.getRemoteAddr()));
    }

    @GetMapping("/historial/{numeroHcl}")
    @PreAuthorize("hasAnyRole('FISIOTERAPEUTA','MEDICO')")
    @Operation(summary = "Historial de episodios", description = "RF-21. Lista episodios de un paciente en orden cronologico inverso")
    @ApiResponse(responseCode = "200", description = "Historial obtenido")
    public ResponseEntity<List<EpisodioClinicoResponse>> historial(@PathVariable String numeroHcl) {
        return ResponseEntity.ok(episodioClinicoService.historialPorNumeroHcl(numeroHcl));
    }

    @PostMapping("/{episodioId}/cierre")
    @PreAuthorize("hasRole('FISIOTERAPEUTA')")
    @Operation(summary = "Cerrar episodio", description = "RF-22. Cierra episodio y actualiza automaticamente fecha de ultima atencion")
    @ApiResponse(responseCode = "200", description = "Episodio cerrado")
    public ResponseEntity<EpisodioClinicoResponse> cerrar(
            @PathVariable Long episodioId,
            @Validated @RequestBody CerrarEpisodioRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {
        return ResponseEntity.ok(episodioClinicoService.cerrarEpisodio(
                episodioId,
                request,
                authentication.getName(),
                httpRequest.getRemoteAddr()));
    }

    @PostMapping("/{episodioId}/evaluaciones")
    @PreAuthorize("hasRole('FISIOTERAPEUTA')")
    @Operation(summary = "Registrar evaluacion fisica", description = "RF-23 a RF-26. Registra evaluacion inicial, intermedia o de alta con medidas clinicas, goniometria, fuerza muscular y escalas funcionales")
    @ApiResponse(responseCode = "200", description = "Evaluacion registrada")
    public ResponseEntity<EvaluacionFisicaResponse> registrarEvaluacion(
            @PathVariable Long episodioId,
            @Validated @RequestBody CreateEvaluacionFisicaRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {
        return ResponseEntity.ok(evaluacionFisicaService.registrarEvaluacion(
                episodioId,
                request,
                authentication.getName(),
                httpRequest.getRemoteAddr()));
    }

    @GetMapping("/{episodioId}/evaluaciones")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','FISIOTERAPEUTA','MEDICO')")
    @Operation(summary = "Listar evaluaciones del episodio", description = "Devuelve todas las evaluaciones fisicas registradas para el episodio en orden cronologico")
    @ApiResponse(responseCode = "200", description = "Evaluaciones obtenidas")
    public ResponseEntity<List<EvaluacionFisicaResponse>> listarEvaluaciones(@PathVariable Long episodioId) {
        return ResponseEntity.ok(evaluacionFisicaService.listarPorEpisodio(episodioId));
    }

    @GetMapping("/{episodioId}/evaluaciones/{evaluacionId}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','FISIOTERAPEUTA','MEDICO')")
    @Operation(summary = "Obtener evaluacion fisica", description = "Obtiene el detalle completo de una evaluacion fisica del episodio")
    @ApiResponse(responseCode = "200", description = "Evaluacion obtenida")
    public ResponseEntity<EvaluacionFisicaResponse> obtenerEvaluacion(
            @PathVariable Long episodioId,
            @PathVariable Long evaluacionId) {
        return ResponseEntity.ok(evaluacionFisicaService.obtenerPorId(episodioId, evaluacionId));
    }

    @GetMapping("/{episodioId}/evaluaciones/{evaluacionId}/comparativa")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','FISIOTERAPEUTA','MEDICO')")
    @Operation(summary = "Comparar evaluacion con inicial", description = "RF-27. Calcula la comparativa de EVA y escalas funcionales respecto a la evaluacion inicial del episodio")
    @ApiResponse(responseCode = "200", description = "Comparativa obtenida")
    public ResponseEntity<ComparativaEvaluacionResponse> compararEvaluacion(
            @PathVariable Long episodioId,
            @PathVariable Long evaluacionId) {
        return ResponseEntity.ok(evaluacionFisicaService.compararConInicial(episodioId, evaluacionId));
    }

    @GetMapping("/{episodioId}/evaluaciones/progreso")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','FISIOTERAPEUTA','MEDICO')")
    @Operation(summary = "Obtener puntos de progreso", description = "RF-28. Devuelve la serie temporal de EVA y puntaje funcional para graficar evolucion del episodio")
    @ApiResponse(responseCode = "200", description = "Serie de progreso obtenida")
    public ResponseEntity<List<PuntoProgresoEvaluacionResponse>> obtenerProgreso(@PathVariable Long episodioId) {
        return ResponseEntity.ok(evaluacionFisicaService.obtenerProgreso(episodioId));
    }

    @GetMapping("/{episodioId}/planes")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','FISIOTERAPEUTA','MEDICO')")
    @Operation(summary = "Listar planes de tratamiento del episodio", description = "RF-31/RF-33. Devuelve todos los planes de tratamiento del episodio con su avance de sesiones")
    @ApiResponse(responseCode = "200", description = "Planes obtenidos")
    public ResponseEntity<List<PlanTratamientoResponse>> listarPlanes(@PathVariable Long episodioId) {
        return ResponseEntity.ok(planTratamientoService.listarPorEpisodio(episodioId));
    }

    @GetMapping("/{episodioId}/contenido-completo")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','FISIOTERAPEUTA','MEDICO')")
    @Operation(summary = "Obtener contenido completo del episodio", description = "Retorna toda la información relacionada con el episodio: problemas, evaluaciones, planes de tratamiento y seguimientos")
    @ApiResponse(responseCode = "200", description = "Contenido completo obtenido")
    public ResponseEntity<EpisodioClinicoContenidoResponse> obtenerContenidoCompleto(
            @PathVariable Long episodioId) {
        return ResponseEntity.ok(episodioClinicoService.obtenerContenidoCompleto(episodioId));
    }

    @GetMapping("/consultas/resumen")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','FISIOTERAPEUTA','MEDICO')")
    @Operation(summary = "Resumen de consultas (Paginado)", description = "Retorna lista paginada de pacientes con su resumen de episodios")
    @ApiResponse(responseCode = "200", description = "Resumen obtenido")
    public ResponseEntity<PaginatedResponse<ConsultaResumeItemResponse>> obtenerResumenConsultas(
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "1") int page,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "8") int limit,
            @org.springframework.web.bind.annotation.RequestParam(required = false) String search) {
        return ResponseEntity.ok(episodioClinicoService.obtenerResumenConsultas(search, page, limit));
    }
}
