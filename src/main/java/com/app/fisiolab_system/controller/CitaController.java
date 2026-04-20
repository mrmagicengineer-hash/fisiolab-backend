package com.app.fisiolab_system.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.app.fisiolab_system.dto.ActualizarEstadoCitaRequest;
import com.app.fisiolab_system.dto.CalendarEventResponse;
import com.app.fisiolab_system.dto.CitaResponse;
import com.app.fisiolab_system.dto.CrearCitaRequest;
import com.app.fisiolab_system.service.CitaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/citas")
@Tag(name = "Gestión de Agenda y Citas", description = "RF-50 al RF-57. Agendamiento, visualización y cambio de estado de citas")
@SecurityRequirement(name = "bearerAuth")
public class CitaController {

    private final CitaService citaService;

    public CitaController(CitaService citaService) {
        this.citaService = citaService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','FISIOTERAPEUTA')")
    @Operation(
            summary = "Crear cita",
            description = "RF-50. Agenda una nueva cita. Valida disponibilidad del profesional y estado activo del paciente. "
                    + "FISIOTERAPEUTA solo puede agendar en su propia agenda.")
    @ApiResponse(responseCode = "201", description = "Cita creada correctamente")
    @ApiResponse(responseCode = "400", description = "Solapamiento de horario o datos invalidos")
    @ApiResponse(responseCode = "403", description = "Acceso denegado")
    public ResponseEntity<CitaResponse> crearCita(
            @Validated @RequestBody CrearCitaRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {
        CitaResponse response = citaService.crearCita(request, authentication.getName(),
                httpRequest.getRemoteAddr());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','FISIOTERAPEUTA')")
    @Operation(
            summary = "Listar citas",
            description = "RF-51. Lista todas las citas. FISIOTERAPEUTA solo ve las suyas.")
    @ApiResponse(responseCode = "200", description = "Listado obtenido correctamente")
    public ResponseEntity<List<CitaResponse>> listarCitas(Authentication authentication) {
        return ResponseEntity.ok(citaService.listarCitas(authentication.getName()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','FISIOTERAPEUTA')")
    @Operation(
            summary = "Obtener detalle de cita",
            description = "Devuelve el detalle de una cita. FISIOTERAPEUTA solo puede ver las suyas.")
    @ApiResponse(responseCode = "200", description = "Cita encontrada")
    @ApiResponse(responseCode = "403", description = "Acceso denegado")
    @ApiResponse(responseCode = "404", description = "Cita no encontrada")
    public ResponseEntity<CitaResponse> obtenerCita(
            @PathVariable Long id,
            Authentication authentication) {
        return ResponseEntity.ok(citaService.obtenerPorId(id, authentication.getName()));
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','FISIOTERAPEUTA')")
    @Operation(
            summary = "Cambiar estado de cita",
            description = "RF-52. Transiciona el estado: PROGRAMADA → REALIZADA | CANCELADA | NO_ASISTIDA. "
                    + "Al marcar como REALIZADA se dispara la creacion automatica de la SesionSOAP (Modulo 7).")
    @ApiResponse(responseCode = "200", description = "Estado actualizado")
    @ApiResponse(responseCode = "400", description = "Transicion de estado invalida")
    @ApiResponse(responseCode = "403", description = "Acceso denegado")
    public ResponseEntity<CitaResponse> actualizarEstado(
            @PathVariable Long id,
            @Validated @RequestBody ActualizarEstadoCitaRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {
        return ResponseEntity.ok(
                citaService.actualizarEstado(id, request, authentication.getName(),
                        httpRequest.getRemoteAddr()));
    }

    @GetMapping("/disponibilidad")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','FISIOTERAPEUTA')")
    @Operation(
            summary = "Verificar disponibilidad de profesional",
            description = "RF-50. Retorna true si el profesional no tiene cita ni bloqueo en el rango indicado.")
    @ApiResponse(responseCode = "200", description = "Resultado de disponibilidad")
    public ResponseEntity<java.util.Map<String, Boolean>> verificarDisponibilidad(
            @RequestParam Long profesionalId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta) {
        boolean disponible = citaService.verificarDisponibilidad(profesionalId, desde, hasta);
        return ResponseEntity.ok(java.util.Map.of("disponible", disponible));
    }

    @GetMapping("/agenda/view")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','FISIOTERAPEUTA')")
    @Operation(
            summary = "Vista de agenda en formato FullCalendar",
            description = "RF-51. Devuelve citas y bloqueos en formato compatible con FullCalendar. "
                    + "Requiere parametros desde/hasta. FISIOTERAPEUTA ve solo su agenda. "
                    + "ADMINISTRADOR puede filtrar por profesionalId o ver todos.")
    @ApiResponse(responseCode = "200", description = "Eventos del calendario")
    public ResponseEntity<List<CalendarEventResponse>> getAgendaView(
            @RequestParam(required = false) Long profesionalId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta,
            Authentication authentication) {
        return ResponseEntity.ok(
                citaService.getAgendaView(profesionalId, desde, hasta, authentication.getName()));
    }
}
