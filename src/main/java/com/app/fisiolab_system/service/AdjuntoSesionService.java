package com.app.fisiolab_system.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.app.fisiolab_system.dto.AdjuntoSesionResponse;
import com.app.fisiolab_system.model.AdjuntoSesion;
import com.app.fisiolab_system.model.EstadoSesionTerapia;
import com.app.fisiolab_system.model.RolUsuario;
import com.app.fisiolab_system.model.SesionTerapia;
import com.app.fisiolab_system.model.Usuario;
import com.app.fisiolab_system.repository.AdjuntoSesionRepository;
import com.app.fisiolab_system.repository.SesionTerapiaRepository;
import com.app.fisiolab_system.repository.UsuarioRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class AdjuntoSesionService {

    private static final List<String> TIPOS_PERMITIDOS = List.of(
            "application/pdf",
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    private final AdjuntoSesionRepository adjuntoRepository;
    private final SesionTerapiaRepository sesionRepository;
    private final UsuarioRepository usuarioRepository;
    private final AuditoriaService auditoriaService;

    @Value("${app.storage.path:./uploads}")
    private String storagePath;

    public AdjuntoSesionService(
            AdjuntoSesionRepository adjuntoRepository,
            SesionTerapiaRepository sesionRepository,
            UsuarioRepository usuarioRepository,
            AuditoriaService auditoriaService) {
        this.adjuntoRepository = adjuntoRepository;
        this.sesionRepository = sesionRepository;
        this.usuarioRepository = usuarioRepository;
        this.auditoriaService = auditoriaService;
    }

    public AdjuntoSesionResponse subirAdjunto(Long sesionId, MultipartFile archivo,
            String actorEmail, String clientIp) {
        SesionTerapia sesion = getSesionOrThrow(sesionId);
        Usuario actor = getUsuarioOrThrow(actorEmail);

        validarEditable(sesion);
        validarOwnership(sesion, actor);
        validarArchivo(archivo);

        String nombreAlmacenado = UUID.randomUUID() + "_" + sanitizarNombre(archivo.getOriginalFilename());
        String rutaRelativa = "sesiones/" + sesionId + "/" + nombreAlmacenado;

        Path destino = Paths.get(storagePath).resolve(rutaRelativa);
        try {
            Files.createDirectories(destino.getParent());
            Files.copy(archivo.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IllegalStateException("Error al guardar el archivo: " + e.getMessage(), e);
        }

        AdjuntoSesion adjunto = AdjuntoSesion.builder()
                .sesionTerapia(sesion)
                .nombreOriginal(archivo.getOriginalFilename())
                .tipoMime(archivo.getContentType())
                .rutaAlmacenamiento(rutaRelativa)
                .tamanoBytes(archivo.getSize())
                .subidoPor(actor)
                .build();

        AdjuntoSesion saved = adjuntoRepository.save(adjunto);

        auditoriaService.registrar(actor.getId(), "ADJUNTO_SUBIDO",
                "Adjunto '%s' subido a sesión %d".formatted(archivo.getOriginalFilename(), sesionId),
                clientIp);

        return toResponse(saved);
    }

    public List<AdjuntoSesionResponse> listarAdjuntos(Long sesionId) {
        if (!sesionRepository.existsById(sesionId)) {
            throw new IllegalArgumentException("Sesión no encontrada: " + sesionId);
        }
        return adjuntoRepository.findBySesionTerapiaIdOrderByFechaSubidaAsc(sesionId)
                .stream().map(this::toResponse).toList();
    }

    public Resource descargarAdjunto(Long sesionId, Long adjuntoId) {
        AdjuntoSesion adjunto = adjuntoRepository.findByIdAndSesionTerapiaId(adjuntoId, sesionId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Adjunto %d no encontrado en sesión %d".formatted(adjuntoId, sesionId)));
        try {
            Path archivo = Paths.get(storagePath).resolve(adjunto.getRutaAlmacenamiento());
            Resource resource = new UrlResource(archivo.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new IllegalStateException("Archivo no disponible en disco.");
            }
            return resource;
        } catch (IOException e) {
            throw new IllegalStateException("Error al leer el archivo.", e);
        }
    }

    public String obtenerTipoMime(Long sesionId, Long adjuntoId) {
        return adjuntoRepository.findByIdAndSesionTerapiaId(adjuntoId, sesionId)
                .map(AdjuntoSesion::getTipoMime)
                .orElseThrow(() -> new IllegalArgumentException("Adjunto no encontrado."));
    }

    public String obtenerNombreOriginal(Long sesionId, Long adjuntoId) {
        return adjuntoRepository.findByIdAndSesionTerapiaId(adjuntoId, sesionId)
                .map(AdjuntoSesion::getNombreOriginal)
                .orElseThrow(() -> new IllegalArgumentException("Adjunto no encontrado."));
    }

    public void eliminarAdjunto(Long sesionId, Long adjuntoId, String actorEmail, String clientIp) {
        SesionTerapia sesion = getSesionOrThrow(sesionId);
        Usuario actor = getUsuarioOrThrow(actorEmail);
        validarEditable(sesion);
        validarOwnership(sesion, actor);

        AdjuntoSesion adjunto = adjuntoRepository.findByIdAndSesionTerapiaId(adjuntoId, sesionId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Adjunto %d no encontrado en sesión %d".formatted(adjuntoId, sesionId)));

        Path archivo = Paths.get(storagePath).resolve(adjunto.getRutaAlmacenamiento());
        try {
            Files.deleteIfExists(archivo);
        } catch (IOException e) {
            throw new IllegalStateException("Error al eliminar el archivo del disco.", e);
        }

        adjuntoRepository.delete(adjunto);

        auditoriaService.registrar(actor.getId(), "ADJUNTO_ELIMINADO",
                "Adjunto '%s' eliminado de sesión %d".formatted(adjunto.getNombreOriginal(), sesionId),
                clientIp);
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private void validarEditable(SesionTerapia sesion) {
        if (sesion.getEstado() == EstadoSesionTerapia.FIRMADA) {
            throw new IllegalStateException("Sesión firmada — no se pueden agregar ni eliminar adjuntos.");
        }
    }

    private void validarOwnership(SesionTerapia sesion, Usuario actor) {
        if (actor.getRol() == RolUsuario.ADMINISTRADOR) return;
        if (!sesion.getProfesional().getId().equals(actor.getId())) {
            throw new AccessDeniedException("Solo el fisioterapeuta asignado puede gestionar adjuntos de esta sesión.");
        }
    }

    private void validarArchivo(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new IllegalArgumentException("El archivo no puede estar vacío.");
        }
        String mime = archivo.getContentType();
        if (mime == null || !TIPOS_PERMITIDOS.contains(mime)) {
            throw new IllegalArgumentException(
                    "Tipo de archivo no permitido: " + mime + ". Permitidos: PDF, JPEG, PNG, WEBP.");
        }
        if (archivo.getSize() > 10 * 1024 * 1024) {
            throw new IllegalArgumentException("El archivo supera el límite de 10 MB.");
        }
    }

    private String sanitizarNombre(String nombre) {
        if (nombre == null) return "archivo";
        return nombre.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    private SesionTerapia getSesionOrThrow(Long sesionId) {
        return sesionRepository.findById(sesionId)
                .orElseThrow(() -> new IllegalArgumentException("Sesión no encontrada: " + sesionId));
    }

    private Usuario getUsuarioOrThrow(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + email));
    }

    private AdjuntoSesionResponse toResponse(AdjuntoSesion a) {
        String nombreProfesional = a.getSubidoPor().getName() + " " + a.getSubidoPor().getLastName();
        String urlDescarga = "/sesiones/" + a.getSesionTerapia().getId()
                + "/adjuntos/" + a.getId() + "/descargar";
        return new AdjuntoSesionResponse(
                a.getId(),
                a.getSesionTerapia().getId(),
                a.getNombreOriginal(),
                a.getTipoMime(),
                a.getTamanoBytes(),
                a.getSubidoPor().getId(),
                nombreProfesional,
                a.getFechaSubida(),
                urlDescarga);
    }
}
