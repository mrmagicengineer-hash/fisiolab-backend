package com.app.fisiolab_system.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.app.fisiolab_system.dto.BloqueoResponse;
import com.app.fisiolab_system.dto.CrearBloqueoRequest;
import com.app.fisiolab_system.model.BloqueoAgenda;
import com.app.fisiolab_system.model.RolUsuario;
import com.app.fisiolab_system.model.Usuario;
import com.app.fisiolab_system.repository.BloqueoAgendaRepository;
import com.app.fisiolab_system.repository.UsuarioRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class BloqueoAgendaService {

    private final BloqueoAgendaRepository bloqueoAgendaRepository;
    private final UsuarioRepository usuarioRepository;
    private final AuditoriaService auditoriaService;

    public BloqueoAgendaService(BloqueoAgendaRepository bloqueoAgendaRepository,
            UsuarioRepository usuarioRepository,
            AuditoriaService auditoriaService) {
        this.bloqueoAgendaRepository = bloqueoAgendaRepository;
        this.usuarioRepository = usuarioRepository;
        this.auditoriaService = auditoriaService;
    }

    public BloqueoResponse crearBloqueo(CrearBloqueoRequest request, String actorEmail, String clientIp) {
        if (!request.fechaHoraFin().isAfter(request.fechaHoraInicio())) {
            throw new IllegalArgumentException("La fecha y hora de fin debe ser posterior al inicio.");
        }

        Usuario actor = resolverUsuario(actorEmail);
        Usuario profesional = resolverProfesional(request.profesionalId());

        BloqueoAgenda bloqueo = BloqueoAgenda.builder()
                .profesional(profesional)
                .creadoPor(actor)
                .fechaHoraInicio(request.fechaHoraInicio())
                .fechaHoraFin(request.fechaHoraFin())
                .motivo(request.motivo())
                .descripcion(normalizeNullable(request.descripcion()))
                .build();

        BloqueoAgenda saved = bloqueoAgendaRepository.save(bloqueo);

        auditoriaService.registrar(actor.getId(),
                "BLOQUEO_AGENDA_CREADO",
                "Bloqueo %s creado para profesional %s desde %s hasta %s".formatted(
                        request.motivo(),
                        profesional.getEmail(),
                        request.fechaHoraInicio(),
                        request.fechaHoraFin()),
                clientIp);

        return toResponse(saved);
    }

    public List<BloqueoResponse> listarBloqueos(String actorEmail) {
        Usuario actor = resolverUsuario(actorEmail);
        List<BloqueoAgenda> bloqueos = actor.getRol() == RolUsuario.FISIOTERAPEUTA
                ? bloqueoAgendaRepository.findByProfesionalIdOrderByFechaHoraInicioAsc(actor.getId())
                : bloqueoAgendaRepository.findAllByOrderByFechaHoraInicioAsc();
        return bloqueos.stream().map(this::toResponse).toList();
    }

    public void eliminarBloqueo(Long bloqueoId, String actorEmail, String clientIp) {
        BloqueoAgenda bloqueo = bloqueoAgendaRepository.findById(bloqueoId)
                .orElseThrow(() -> new IllegalArgumentException("Bloqueo no encontrado: " + bloqueoId));

        Usuario actor = resolverUsuario(actorEmail);

        bloqueoAgendaRepository.delete(bloqueo);

        auditoriaService.registrar(actor.getId(),
                "BLOQUEO_AGENDA_ELIMINADO",
                "Bloqueo %d eliminado (profesional: %s)".formatted(
                        bloqueoId, bloqueo.getProfesional().getEmail()),
                clientIp);
    }

    BloqueoResponse toResponse(BloqueoAgenda bloqueo) {
        String profesionalNombre = bloqueo.getProfesional().getName() + " " + bloqueo.getProfesional().getLastName();
        String creadoPorNombre = bloqueo.getCreadoPor().getName() + " " + bloqueo.getCreadoPor().getLastName();
        return new BloqueoResponse(
                bloqueo.getId(),
                bloqueo.getProfesional().getId(),
                profesionalNombre,
                bloqueo.getCreadoPor().getId(),
                creadoPorNombre,
                bloqueo.getFechaHoraInicio(),
                bloqueo.getFechaHoraFin(),
                bloqueo.getMotivo(),
                bloqueo.getDescripcion(),
                bloqueo.getFechaCreacion());
    }

    private Usuario resolverUsuario(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + email));
    }

    private Usuario resolverProfesional(Long profesionalId) {
        Usuario profesional = usuarioRepository.findById(profesionalId)
                .orElseThrow(() -> new IllegalArgumentException("Profesional no encontrado: " + profesionalId));
        if (profesional.getRol() != RolUsuario.FISIOTERAPEUTA) {
            throw new IllegalArgumentException("El usuario seleccionado no es un Fisioterapeuta.");
        }
        return profesional;
    }

    private String normalizeNullable(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
