package com.app.fisiolab_system.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.app.fisiolab_system.dto.CreateEvaluacionClinicaRequest;
import com.app.fisiolab_system.dto.EvaluacionClinicaResponse;
import com.app.fisiolab_system.dto.SignosVitalesDto;
import com.app.fisiolab_system.dto.UpdateEvaluacionClinicaRequest;
import com.app.fisiolab_system.model.EpisodioClinico;
import com.app.fisiolab_system.model.EstadoEpisodioClinico;
import com.app.fisiolab_system.model.EvaluacionClinica;
import com.app.fisiolab_system.model.Usuario;
import com.app.fisiolab_system.repository.EpisodioClinicoRepository;
import com.app.fisiolab_system.repository.EvaluacionClinicaRepository;
import com.app.fisiolab_system.repository.UsuarioRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class EvaluacionClinicaService {

    private static final TypeReference<List<String>> LIST_STRING_TYPE = new TypeReference<>() {
    };

    private final EvaluacionClinicaRepository evaluacionRepository;
    private final EpisodioClinicoRepository episodioRepository;
    private final UsuarioRepository usuarioRepository;
    private final AuditoriaService auditoriaService;
    private final ObjectMapper objectMapper;

    public EvaluacionClinicaService(
            EvaluacionClinicaRepository evaluacionRepository,
            EpisodioClinicoRepository episodioRepository,
            UsuarioRepository usuarioRepository,
            AuditoriaService auditoriaService,
            ObjectMapper objectMapper) {
        this.evaluacionRepository = evaluacionRepository;
        this.episodioRepository = episodioRepository;
        this.usuarioRepository = usuarioRepository;
        this.auditoriaService = auditoriaService;
        this.objectMapper = objectMapper;
    }

    public EvaluacionClinicaResponse crear(
            Long episodioId,
            CreateEvaluacionClinicaRequest request,
            String actorEmail,
            String clientIp) {
        EpisodioClinico episodio = getEpisodioAbierto(episodioId);

        EvaluacionClinica entity = EvaluacionClinica.builder()
                .episodioClinico(episodio)
                .fechaEvaluacion(request.fechaEvaluacion())
                .fisioterapeutaId(trim(request.fisioterapeutaId()))
                .presionArterial(request.signosVitales().pa())
                .frecuenciaCardiaca(request.signosVitales().fc())
                .motivoConsulta(trim(request.motivoConsulta()))
                .observacionGeneral(trim(request.observacionGeneral()))
                .hallazgosPrincipalesJson(toJson(request.hallazgosPrincipales()))
                .escalaEva(request.escalaEva())
                .impresionDiagnostica(trim(request.impresionDiagnostica()))
                .planInicial(trim(request.planInicial()))
                .build();

        EvaluacionClinica saved = evaluacionRepository.save(entity);

        auditar(actorEmail,
                "REGISTRO_EVALUACION_CLINICA",
                "Evaluacion clinica creada para episodio " + episodio.getNumeroEpisodio(),
                clientIp);

        return toResponse(saved);
    }

    public List<EvaluacionClinicaResponse> listarPorEpisodio(Long episodioId) {
        asegurarEpisodioExiste(episodioId);
        return evaluacionRepository.findByEpisodioClinicoIdOrderByFechaEvaluacionAsc(episodioId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public EvaluacionClinicaResponse obtenerPorId(Long episodioId, Long evaluacionId) {
        return toResponse(getOrThrow(episodioId, evaluacionId));
    }

    public EvaluacionClinicaResponse actualizar(
            Long episodioId,
            Long evaluacionId,
            UpdateEvaluacionClinicaRequest request,
            String actorEmail,
            String clientIp) {
        EvaluacionClinica entity = getOrThrow(episodioId, evaluacionId);

        if (request.fechaEvaluacion() != null) {
            entity.setFechaEvaluacion(request.fechaEvaluacion());
        }
        if (request.fisioterapeutaId() != null) {
            entity.setFisioterapeutaId(trim(request.fisioterapeutaId()));
        }
        if (request.signosVitales() != null) {
            entity.setPresionArterial(request.signosVitales().pa());
            entity.setFrecuenciaCardiaca(request.signosVitales().fc());
        }
        if (request.motivoConsulta() != null) {
            entity.setMotivoConsulta(trim(request.motivoConsulta()));
        }
        if (request.observacionGeneral() != null) {
            entity.setObservacionGeneral(trim(request.observacionGeneral()));
        }
        if (request.hallazgosPrincipales() != null) {
            entity.setHallazgosPrincipalesJson(toJson(request.hallazgosPrincipales()));
        }
        if (request.escalaEva() != null) {
            entity.setEscalaEva(request.escalaEva());
        }
        if (request.impresionDiagnostica() != null) {
            entity.setImpresionDiagnostica(trim(request.impresionDiagnostica()));
        }
        if (request.planInicial() != null) {
            entity.setPlanInicial(trim(request.planInicial()));
        }

        EvaluacionClinica saved = evaluacionRepository.save(entity);

        auditar(actorEmail,
                "ACTUALIZACION_EVALUACION_CLINICA",
                "Evaluacion clinica " + evaluacionId + " actualizada",
                clientIp);

        return toResponse(saved);
    }

    public void eliminar(Long episodioId, Long evaluacionId, String actorEmail, String clientIp) {
        EvaluacionClinica entity = getOrThrow(episodioId, evaluacionId);
        evaluacionRepository.delete(entity);

        auditar(actorEmail,
                "ELIMINACION_EVALUACION_CLINICA",
                "Evaluacion clinica " + evaluacionId + " eliminada del episodio " + episodioId,
                clientIp);
    }

    private EpisodioClinico getEpisodioAbierto(Long episodioId) {
        EpisodioClinico episodio = episodioRepository.findById(episodioId)
                .orElseThrow(() -> new IllegalArgumentException("Episodio no encontrado: " + episodioId));
        if (episodio.getEstado() == EstadoEpisodioClinico.CERRADO) {
            throw new IllegalArgumentException("No se pueden registrar evaluaciones en un episodio cerrado.");
        }
        return episodio;
    }

    private void asegurarEpisodioExiste(Long episodioId) {
        if (!episodioRepository.existsById(episodioId)) {
            throw new IllegalArgumentException("Episodio no encontrado: " + episodioId);
        }
    }

    private EvaluacionClinica getOrThrow(Long episodioId, Long evaluacionId) {
        return evaluacionRepository.findByIdAndEpisodioClinicoId(evaluacionId, episodioId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Evaluacion clinica no encontrada: " + evaluacionId + " para episodio: " + episodioId));
    }

    private EvaluacionClinicaResponse toResponse(EvaluacionClinica entity) {
        SignosVitalesDto signosVitales = new SignosVitalesDto(
                entity.getPresionArterial(),
                entity.getFrecuenciaCardiaca());

        return new EvaluacionClinicaResponse(
                entity.getId(),
                entity.getEpisodioClinico().getId(),
                entity.getFechaEvaluacion(),
                entity.getFisioterapeutaId(),
                signosVitales,
                entity.getMotivoConsulta(),
                entity.getObservacionGeneral(),
                fromJson(entity.getHallazgosPrincipalesJson()),
                entity.getEscalaEva(),
                entity.getImpresionDiagnostica(),
                entity.getPlanInicial(),
                entity.getCreadoEn());
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception ex) {
            throw new IllegalStateException("Error serializando datos de evaluacion.", ex);
        }
    }

    private List<String> fromJson(String json) {
        if (json == null) return List.of();
        try {
            return objectMapper.readValue(json, LIST_STRING_TYPE);
        } catch (Exception ex) {
            throw new IllegalStateException("Error deserializando datos de evaluacion.", ex);
        }
    }

    private void auditar(String actorEmail, String accion, String detalle, String ip) {
        Long actorId = usuarioRepository.findByEmail(actorEmail)
                .map(Usuario::getId)
                .orElse(0L);
        auditoriaService.registrar(actorId, accion, detalle, ip);
    }

    private String trim(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
