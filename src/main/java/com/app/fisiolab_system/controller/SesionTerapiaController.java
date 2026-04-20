package com.app.fisiolab_system.controller;

import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.app.fisiolab_system.dto.ActualizarNotaSOAPRequest;
import com.app.fisiolab_system.dto.AdjuntoSesionResponse;
import com.app.fisiolab_system.dto.NotaSOAPResponse;
import com.app.fisiolab_system.dto.ProgresoSesionResponse;
import com.app.fisiolab_system.dto.SesionTerapiaResponse;
import com.app.fisiolab_system.service.AdjuntoSesionService;
import com.app.fisiolab_system.service.PdfSesionService;
import com.app.fisiolab_system.service.SesionTerapiaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@Tag(name = "Sesiones de Terapia", description = "Módulo 7: sesiones, notas SOAP y firma digital")
@SecurityRequirement(name = "bearerAuth")
public class SesionTerapiaController {

    private final SesionTerapiaService sesionService;
    private final AdjuntoSesionService adjuntoService;
    private final PdfSesionService pdfService;

    public SesionTerapiaController(SesionTerapiaService sesionService,
            AdjuntoSesionService adjuntoService,
            PdfSesionService pdfService) {
        this.sesionService = sesionService;
        this.adjuntoService = adjuntoService;
        this.pdfService = pdfService;
    }

    // ─── TRIGGER: cita → sesión ───────────────────────────────────────────────

    @PatchMapping("/citas/{citaId}/atender")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','FISIOTERAPEUTA')")
    @Operation(
        summary = "Atender cita (iniciar sesión)",
        description = "Convierte cita PROGRAMADA en REALIZADA y crea automáticamente "
            + "SesionTerapia + NotaSOAP en borrador. Retorna el ID de sesión para "
            + "que el front redirija al formulario SOAP.")
    @ApiResponse(responseCode = "200", description = "Sesión iniciada")
    @ApiResponse(responseCode = "400", description = "Cita no está en estado PROGRAMADA")
    public ResponseEntity<SesionTerapiaResponse> atenderCita(
            @PathVariable Long citaId,
            Authentication authentication,
            HttpServletRequest httpRequest) {
        return ResponseEntity.ok(sesionService.iniciarSesion(
                citaId,
                authentication.getName(),
                httpRequest.getRemoteAddr()));
    }

    // ─── SESIÓN ───────────────────────────────────────────────────────────────

    @GetMapping("/sesiones/{sesionId}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','FISIOTERAPEUTA','MEDICO')")
    @Operation(summary = "Obtener sesión de terapia", description = "Retorna sesión con su nota SOAP embebida")
    @ApiResponse(responseCode = "200", description = "Sesión obtenida")
    public ResponseEntity<SesionTerapiaResponse> obtenerSesion(@PathVariable Long sesionId) {
        return ResponseEntity.ok(sesionService.obtenerPorId(sesionId));
    }

    // ─── HISTORIAL DE EPISODIO (todas las notas SOAP) ─────────────────────────

    @GetMapping("/episodios/{episodioId}/historial")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','FISIOTERAPEUTA','MEDICO')")
    @Operation(
        summary = "Historial clínico del episodio",
        description = "Devuelve todas las sesiones con sus notas SOAP ordenadas "
            + "cronológicamente para el episodio especificado.")
    @ApiResponse(responseCode = "200", description = "Historial obtenido")
    public ResponseEntity<List<SesionTerapiaResponse>> historialPorEpisodio(
            @PathVariable Long episodioId) {
        return ResponseEntity.ok(sesionService.historialPorEpisodio(episodioId));
    }

    // ─── NOTA SOAP ────────────────────────────────────────────────────────────

    @PutMapping("/sesiones/{sesionId}/nota-soap")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','FISIOTERAPEUTA')")
    @Operation(
        summary = "Actualizar nota SOAP (borrador)",
        description = "Actualiza los campos S/O/A/P de la nota. Solo permitido "
            + "mientras modoBorrador=true. El fisioterapeuta debe ser el asignado a la cita.")
    @ApiResponse(responseCode = "200", description = "Nota actualizada")
    @ApiResponse(responseCode = "400", description = "Nota ya firmada — inmutable")
    public ResponseEntity<NotaSOAPResponse> actualizarNota(
            @PathVariable Long sesionId,
            @Validated @RequestBody ActualizarNotaSOAPRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {
        return ResponseEntity.ok(sesionService.actualizarNotaSOAP(
                sesionId,
                request,
                authentication.getName(),
                httpRequest.getRemoteAddr()));
    }

    @PatchMapping("/sesiones/{sesionId}/firmar")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','FISIOTERAPEUTA')")
    @Operation(
        summary = "Firmar y cerrar sesión",
        description = "Aplica firma digital SHA-256. Bloquea edición futura e incrementa "
            + "sesionesRealizadas en el plan. Requiere todos los campos S/O/A/P completos.")
    @ApiResponse(responseCode = "200", description = "Sesión firmada y bloqueada")
    @ApiResponse(responseCode = "400", description = "Campos SOAP incompletos o ya firmada")
    @ApiResponse(responseCode = "403", description = "No es el fisioterapeuta asignado")
    public ResponseEntity<SesionTerapiaResponse> firmarSesion(
            @PathVariable Long sesionId,
            Authentication authentication,
            HttpServletRequest httpRequest) {
        return ResponseEntity.ok(sesionService.finalizarYFirmarSesion(
                sesionId,
                authentication.getName(),
                httpRequest.getRemoteAddr()));
    }

    // ─── RESUMEN DE PROGRESO ──────────────────────────────────────────────────

    @GetMapping("/sesiones/{sesionId}/resumen-progreso")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','FISIOTERAPEUTA','MEDICO')")
    @Operation(
        summary = "Resumen de progreso de la sesión",
        description = "Devuelve comparativa EVA inicial vs reciente, avance del plan "
            + "(sesiones realizadas/planificadas) e historial de sesiones. "
            + "Alimenta la sección Análisis del SOAP.")
    @ApiResponse(responseCode = "200", description = "Resumen obtenido")
    public ResponseEntity<ProgresoSesionResponse> resumenProgreso(@PathVariable Long sesionId) {
        return ResponseEntity.ok(sesionService.getResumenProgreso(sesionId));
    }

    // ─── PDF ─────────────────────────────────────────────────────────────────

    @GetMapping("/sesiones/{sesionId}/pdf")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','FISIOTERAPEUTA','MEDICO')")
    @Operation(
        summary = "Generar PDF de nota SOAP firmada",
        description = "Genera documento PDF con encabezado de la clínica, datos del paciente "
            + "y nota SOAP firmada. Solo disponible para sesiones en estado FIRMADA.")
    @ApiResponse(responseCode = "200", description = "PDF generado")
    @ApiResponse(responseCode = "400", description = "Sesión aún no firmada")
    public ResponseEntity<byte[]> generarPdf(@PathVariable Long sesionId) {
        byte[] pdf = pdfService.generarPdf(sesionId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(
                ContentDisposition.attachment()
                        .filename("sesion-" + sesionId + ".pdf")
                        .build());
        return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
    }

    // ─── ADJUNTOS ─────────────────────────────────────────────────────────────

    @PostMapping(value = "/sesiones/{sesionId}/adjuntos",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','FISIOTERAPEUTA')")
    @Operation(
        summary = "Subir adjunto a sesión",
        description = "Sube un archivo (PDF, JPEG, PNG, WEBP, máx 10 MB) vinculado a la sesión. "
            + "Solo permitido mientras la sesión no esté firmada.")
    @ApiResponse(responseCode = "201", description = "Adjunto subido")
    @ApiResponse(responseCode = "400", description = "Tipo/tamaño no permitido o sesión firmada")
    public ResponseEntity<AdjuntoSesionResponse> subirAdjunto(
            @PathVariable Long sesionId,
            @RequestParam("archivo") MultipartFile archivo,
            Authentication authentication,
            HttpServletRequest httpRequest) {
        AdjuntoSesionResponse response = adjuntoService.subirAdjunto(
                sesionId, archivo, authentication.getName(), httpRequest.getRemoteAddr());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/sesiones/{sesionId}/adjuntos")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','FISIOTERAPEUTA','MEDICO')")
    @Operation(summary = "Listar adjuntos de sesión",
            description = "Devuelve metadata de todos los archivos adjuntos de la sesión.")
    @ApiResponse(responseCode = "200", description = "Lista obtenida")
    public ResponseEntity<List<AdjuntoSesionResponse>> listarAdjuntos(@PathVariable Long sesionId) {
        return ResponseEntity.ok(adjuntoService.listarAdjuntos(sesionId));
    }

    @GetMapping("/sesiones/{sesionId}/adjuntos/{adjuntoId}/descargar")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','FISIOTERAPEUTA','MEDICO')")
    @Operation(summary = "Descargar adjunto", description = "Descarga el archivo adjunto.")
    @ApiResponse(responseCode = "200", description = "Archivo descargado")
    public ResponseEntity<Resource> descargarAdjunto(
            @PathVariable Long sesionId,
            @PathVariable Long adjuntoId) {
        Resource resource = adjuntoService.descargarAdjunto(sesionId, adjuntoId);
        String mime = adjuntoService.obtenerTipoMime(sesionId, adjuntoId);
        String nombre = adjuntoService.obtenerNombreOriginal(sesionId, adjuntoId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(mime))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(nombre).build().toString())
                .body(resource);
    }

    @DeleteMapping("/sesiones/{sesionId}/adjuntos/{adjuntoId}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','FISIOTERAPEUTA')")
    @Operation(
        summary = "Eliminar adjunto",
        description = "Elimina un adjunto del disco y la base de datos. "
            + "Solo permitido mientras la sesión no esté firmada.")
    @ApiResponse(responseCode = "204", description = "Adjunto eliminado")
    @ApiResponse(responseCode = "400", description = "Sesión ya firmada")
    public ResponseEntity<Void> eliminarAdjunto(
            @PathVariable Long sesionId,
            @PathVariable Long adjuntoId,
            Authentication authentication,
            HttpServletRequest httpRequest) {
        adjuntoService.eliminarAdjunto(
                sesionId, adjuntoId, authentication.getName(), httpRequest.getRemoteAddr());
        return ResponseEntity.noContent().build();
    }
}
