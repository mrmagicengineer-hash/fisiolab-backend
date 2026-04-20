package com.app.fisiolab_system.service;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;

import org.springframework.context.event.EventListener;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.app.fisiolab_system.dto.ActualizarNotaSOAPRequest;
import com.app.fisiolab_system.dto.NotaSOAPResponse;
import com.app.fisiolab_system.dto.PlanResumenResponse;
import com.app.fisiolab_system.dto.ProgresoSesionResponse;
import com.app.fisiolab_system.dto.ProgresoSesionResponse.EvaluacionReferencia;
import com.app.fisiolab_system.dto.ProgresoSesionResponse.SesionResumenItem;
import com.app.fisiolab_system.dto.SesionTerapiaResponse;
import com.app.fisiolab_system.event.CitaRealizadaEvent;
import com.app.fisiolab_system.model.Cita;
import com.app.fisiolab_system.model.EpisodioClinico;
import com.app.fisiolab_system.model.EstadoCita;
import com.app.fisiolab_system.model.EstadoSesionTerapia;
import com.app.fisiolab_system.model.EvaluacionFisicaEpisodio;
import com.app.fisiolab_system.model.NotaSOAP;
import com.app.fisiolab_system.model.Paciente;
import com.app.fisiolab_system.model.PlanTratamiento;
import com.app.fisiolab_system.model.RolUsuario;
import com.app.fisiolab_system.model.SesionTerapia;
import com.app.fisiolab_system.model.TipoEvaluacionFisioterapeutica;
import com.app.fisiolab_system.model.Usuario;
import com.app.fisiolab_system.repository.CitaRepository;
import com.app.fisiolab_system.repository.EpisodioClinicoRepository;
import com.app.fisiolab_system.repository.EvaluacionFisicaEpisodioRepository;
import com.app.fisiolab_system.repository.NotaSOAPRepository;
import com.app.fisiolab_system.repository.PlanTratamientoRepository;
import com.app.fisiolab_system.repository.SesionTerapiaRepository;
import com.app.fisiolab_system.repository.UsuarioRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class SesionTerapiaService {

    private static final TypeReference<List<String>> STRING_LIST_TYPE = new TypeReference<>() {};

    private final SesionTerapiaRepository sesionRepository;
    private final NotaSOAPRepository notaSOAPRepository;
    private final CitaRepository citaRepository;
    private final PlanTratamientoRepository planRepository;
    private final EpisodioClinicoRepository episodioRepository;
    private final EvaluacionFisicaEpisodioRepository evaluacionRepository;
    private final UsuarioRepository usuarioRepository;
    private final AuditoriaService auditoriaService;
    private final ObjectMapper objectMapper;

    public SesionTerapiaService(
            SesionTerapiaRepository sesionRepository,
            NotaSOAPRepository notaSOAPRepository,
            CitaRepository citaRepository,
            PlanTratamientoRepository planRepository,
            EpisodioClinicoRepository episodioRepository,
            EvaluacionFisicaEpisodioRepository evaluacionRepository,
            UsuarioRepository usuarioRepository,
            AuditoriaService auditoriaService,
            ObjectMapper objectMapper) {
        this.sesionRepository = sesionRepository;
        this.notaSOAPRepository = notaSOAPRepository;
        this.citaRepository = citaRepository;
        this.planRepository = planRepository;
        this.episodioRepository = episodioRepository;
        this.evaluacionRepository = evaluacionRepository;
        this.usuarioRepository = usuarioRepository;
        this.auditoriaService = auditoriaService;
        this.objectMapper = objectMapper;
    }

    // ─── TRIGGER PRINCIPAL ────────────────────────────────────────────────────

    /**
     * Convierte cita PROGRAMADA en sesión activa.
     * Cambia estado de cita, crea SesionTerapia y NotaSOAP en borrador.
     */
    public SesionTerapiaResponse iniciarSesion(Long citaId, String actorEmail, String clientIp) {
        Cita cita = citaRepository.findById(citaId)
                .orElseThrow(() -> new IllegalArgumentException("Cita no encontrada: " + citaId));

        if (cita.getEstado() != EstadoCita.PROGRAMADA) {
            throw new IllegalStateException(
                    "Solo se puede iniciar sesión desde una cita PROGRAMADA. Estado actual: " + cita.getEstado());
        }

        // Idempotencia: si ya se creó la sesión para esta cita, retorna la existente
        if (sesionRepository.existsByCitaId(citaId)) {
            SesionTerapia existente = sesionRepository.findByCitaId(citaId).orElseThrow();
            return toResponse(existente);
        }

        cita.setEstado(EstadoCita.REALIZADA);
        citaRepository.save(cita);

        SesionTerapia sesion = crearSesionDesdeCita(cita);

        auditoriaService.registrar(
                resolverActorId(actorEmail),
                "SESION_INICIADA",
                "Sesión %d iniciada desde cita %d para paciente %s".formatted(
                        sesion.getId(),
                        citaId,
                        cita.getPaciente().getNumeroHcl()),
                clientIp);

        return toResponse(sesion);
    }

    // ─── SESIÓN DIRECTA (sin cita previa) ────────────────────────────────────

    public SesionTerapiaResponse iniciarSesionDirecta(Long planId, String actorEmail, String clientIp) {
        PlanTratamiento plan = planRepository.findById(planId)
            .orElseThrow(() -> new IllegalArgumentException("Plan no encontrado: " + planId));

        if (plan.getEstado() != com.app.fisiolab_system.model.EstadoPlan.ACTIVO) {
            throw new IllegalStateException("Solo se puede iniciar sesión directa en planes ACTIVOS. Estado: " + plan.getEstado());
        }

        Usuario profesional = resolverUsuario(actorEmail);
        Paciente paciente = plan.getEpisodioClinico().getHistoriaClinica().getPaciente();
        LocalDateTime ahora = LocalDateTime.now();

        Cita citaFantasma = Cita.builder()
            .paciente(paciente)
            .profesional(profesional)
            .creadoPor(profesional)
            .fechaHoraInicio(ahora)
            .fechaHoraFin(ahora.plusHours(1))
            .estado(EstadoCita.REALIZADA)
            .motivoConsulta("Sesión directa — plan #" + planId)
            .episodioClinicoId(plan.getEpisodioClinico().getId())
            .planTratamientoId(planId)
            .build();
        citaFantasma = citaRepository.save(citaFantasma);

        SesionTerapia sesion = crearSesionDesdeCita(citaFantasma);

        auditoriaService.registrar(
            resolverActorId(actorEmail),
            "SESION_DIRECTA_INICIADA",
            "Sesión directa %d creada para plan #%d, paciente %s".formatted(
                sesion.getId(), planId, paciente.getNumeroHcl()),
            clientIp);

        return toResponse(sesion);
    }

    // ─── FALLBACK EVENT LISTENER ──────────────────────────────────────────────

    /**
     * Listener idempotente. Se activa si alguien cambia estado de cita vía el
     * endpoint genérico de estado (en lugar de /atender).
     */
    @EventListener
    public void onCitaRealizada(CitaRealizadaEvent event) {
        if (sesionRepository.existsByCitaId(event.citaId())) {
            return;
        }
        Cita cita = citaRepository.findById(event.citaId()).orElse(null);
        if (cita == null) return;
        SesionTerapia sesion = crearSesionDesdeCita(cita);
        // actualiza referencia en cita
        cita.setSesionGeneradaId(sesion.getId());
        citaRepository.save(cita);
    }

    // ─── ACTUALIZAR NOTA SOAP (BORRADOR) ─────────────────────────────────────

    public NotaSOAPResponse actualizarNotaSOAP(Long sesionId, ActualizarNotaSOAPRequest request,
            String actorEmail, String clientIp) {
        SesionTerapia sesion = getSesionOrThrow(sesionId);
        validarEditable(sesion);
        validarOwnershipSesion(sesion, resolverUsuario(actorEmail));

        NotaSOAP nota = notaSOAPRepository.findBySesionTerapiaId(sesionId)
                .orElseThrow(() -> new IllegalStateException("Nota SOAP no encontrada para sesión: " + sesionId));

        if (!nota.isModoBorrador()) {
            throw new IllegalStateException("La nota SOAP está firmada y no puede modificarse.");
        }

        if (request.subjetivo() != null) nota.setSubjetivo(request.subjetivo().trim());
        if (request.objetivo() != null) nota.setObjetivo(request.objetivo().trim());
        if (request.analisis() != null) nota.setAnalisis(request.analisis().trim());
        if (request.plan() != null) nota.setPlan(request.plan().trim());

        NotaSOAP saved = notaSOAPRepository.save(nota);

        auditoriaService.registrar(resolverActorId(actorEmail), "NOTA_SOAP_ACTUALIZADA",
                "Nota SOAP de sesión %d actualizada".formatted(sesionId), clientIp);

        return toNotaResponse(saved);
    }

    // ─── FIRMAR Y BLOQUEAR ────────────────────────────────────────────────────

    public SesionTerapiaResponse finalizarYFirmarSesion(Long sesionId, String actorEmail, String clientIp) {
        SesionTerapia sesion = getSesionOrThrow(sesionId);
        Usuario actor = resolverUsuario(actorEmail);

        validarFirmante(sesion, actor);

        NotaSOAP nota = notaSOAPRepository.findBySesionTerapiaId(sesionId)
                .orElseThrow(() -> new IllegalStateException("Nota SOAP no encontrada para sesión: " + sesionId));

        if (!nota.isModoBorrador()) {
            throw new IllegalStateException("La sesión ya está firmada.");
        }

        validarCamposSOAPCompletos(nota);

        LocalDateTime ahora = LocalDateTime.now();
        String hash = generarHashIntegridad(nota, ahora);

        nota.setModoBorrador(false);
        nota.setFirmadoPor(actor);
        nota.setFirmadoEn(ahora);
        nota.setHashIntegridad(hash);
        notaSOAPRepository.save(nota);

        sesion.setEstado(EstadoSesionTerapia.FIRMADA);
        sesion.setFirmadoPor(actor);
        sesion.setFirmadoEn(ahora);
        sesion.setHashIntegridad(hash);
        SesionTerapia saved = sesionRepository.save(sesion);

        // Increment sesionesRealizadas counter in the linked plan
        if (sesion.getPlanTratamiento() != null) {
            PlanTratamiento plan = sesion.getPlanTratamiento();
            plan.setSesionesRealizadas(plan.getSesionesRealizadas() + 1);
            planRepository.save(plan);
        }

        auditoriaService.registrar(actor.getId(), "SESION_FIRMADA",
                "Sesión %d firmada por %s. Hash: %s".formatted(sesionId, actor.getEmail(), hash),
                clientIp);

        return toResponse(saved);
    }

    // ─── CONSULTAS ────────────────────────────────────────────────────────────

    public SesionTerapiaResponse obtenerPorId(Long sesionId) {
        return toResponse(getSesionOrThrow(sesionId));
    }

    public List<SesionTerapiaResponse> historialPorEpisodio(Long episodioId) {
        if (!episodioRepository.existsById(episodioId)) {
            throw new IllegalArgumentException("Episodio no encontrado: " + episodioId);
        }
        return sesionRepository.findByEpisodioClinicoIdOrderByFechaHoraInicioAsc(episodioId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // ─── LÓGICA INTERNA ───────────────────────────────────────────────────────

    private SesionTerapia crearSesionDesdeCita(Cita cita) {
        EpisodioClinico episodio = null;
        PlanTratamiento plan = null;
        BigDecimal costoSesion = null;
        Integer numeroSesion = null;

        if (cita.getEpisodioClinicoId() != null) {
            episodio = episodioRepository.findById(cita.getEpisodioClinicoId()).orElse(null);
        }

        if (episodio != null) {
            // Prefer explicit plan from cita; fall back to most recent plan of episode
            if (cita.getPlanTratamientoId() != null) {
                plan = planRepository.findById(cita.getPlanTratamientoId()).orElse(null);
            }
            if (plan == null) {
                List<PlanTratamiento> planes = planRepository.findByEpisodioClinicoIdOrderByFechaCreacionDesc(episodio.getId());
                if (!planes.isEmpty()) {
                    plan = planes.get(0);
                }
            }
            if (plan != null) {
                costoSesion = plan.getCostoSesion();

                // Control de cupos
                int realizadas = sesionRepository.findMaxNumeroSesionByPlanId(plan.getId());
                if (realizadas >= plan.getSesionesPlanificadas()) {
                    auditoriaService.registrar(0L, "CUPO_EXCEDIDO",
                            "Plan %d: sesiones realizadas (%d) superan las planificadas (%d)".formatted(
                                    plan.getId(), realizadas, plan.getSesionesPlanificadas()), "");
                }
                numeroSesion = realizadas + 1;
            }
        }

        SesionTerapia sesion = SesionTerapia.builder()
                .cita(cita)
                .planTratamiento(plan)
                .paciente(cita.getPaciente())
                .episodioClinico(episodio)
                .profesional(cita.getProfesional())
                .costoSesion(costoSesion)
                .numeroSesionEnPlan(numeroSesion)
                .fechaHoraInicio(cita.getFechaHoraInicio())
                .estado(EstadoSesionTerapia.EN_PROGRESO)
                .build();

        sesion = sesionRepository.save(sesion);

        // Crear NotaSOAP en borrador
        NotaSOAP nota = NotaSOAP.builder()
                .sesionTerapia(sesion)
                .modoBorrador(true)
                .build();
        notaSOAPRepository.save(nota);

        // Actualizar referencia en cita
        cita.setSesionGeneradaId(sesion.getId());
        citaRepository.save(cita);

        return sesion;
    }

    private void validarEditable(SesionTerapia sesion) {
        if (sesion.getEstado() == EstadoSesionTerapia.FIRMADA) {
            throw new IllegalStateException("La sesión está firmada y no puede modificarse.");
        }
    }

    private void validarOwnershipSesion(SesionTerapia sesion, Usuario actor) {
        if (actor.getRol() == RolUsuario.ADMINISTRADOR) return;
        if (!sesion.getProfesional().getId().equals(actor.getId())) {
            throw new AccessDeniedException("Solo el fisioterapeuta asignado puede modificar esta sesión.");
        }
    }

    private void validarFirmante(SesionTerapia sesion, Usuario actor) {
        if (actor.getRol() == RolUsuario.ADMINISTRADOR) return;
        if (!sesion.getProfesional().getId().equals(actor.getId())) {
            throw new AccessDeniedException("Solo el fisioterapeuta asignado o un administrador puede firmar esta sesión.");
        }
    }

    private void validarCamposSOAPCompletos(NotaSOAP nota) {
        if (isBlank(nota.getSubjetivo()) || isBlank(nota.getObjetivo())
                || isBlank(nota.getAnalisis()) || isBlank(nota.getPlan())) {
            throw new IllegalStateException(
                    "La nota SOAP debe tener todos los campos completos (S, O, A, P) antes de firmar.");
        }
    }

    private String generarHashIntegridad(NotaSOAP nota, LocalDateTime firmadoEn) {
        try {
            String contenido = String.join("|",
                    safe(nota.getSubjetivo()),
                    safe(nota.getObjetivo()),
                    safe(nota.getAnalisis()),
                    safe(nota.getPlan()),
                    firmadoEn.toString());
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(contenido.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception ex) {
            throw new IllegalStateException("Error generando hash de integridad.", ex);
        }
    }

    // ─── RESUMEN DE PROGRESO ──────────────────────────────────────────────────

    public ProgresoSesionResponse getResumenProgreso(Long sesionId) {
        SesionTerapia sesion = getSesionOrThrow(sesionId);

        PlanTratamiento plan = sesion.getPlanTratamiento();
        EpisodioClinico episodio = sesion.getEpisodioClinico();

        // Historial de sesiones del plan (solo si existe plan)
        List<SesionResumenItem> historial = List.of();
        int totalPlan = 0;
        int realizadas = 0;
        int restantes = 0;

        if (plan != null) {
            totalPlan = plan.getSesionesPlanificadas();
            realizadas = plan.getSesionesRealizadas();
            restantes = Math.max(0, totalPlan - realizadas);
            historial = sesionRepository
                    .findByPlanTratamientoIdOrderByNumeroSesionEnPlanAsc(plan.getId())
                    .stream()
                    .map(s -> new SesionResumenItem(
                            s.getId(),
                            s.getNumeroSesionEnPlan(),
                            s.getFechaHoraInicio(),
                            s.getEstado()))
                    .toList();
        }

        // Evaluaciones físicas del episodio para comparar EVA
        EvaluacionReferencia evalInicial = null;
        EvaluacionReferencia evalReciente = null;
        Integer deltaEva = null;

        if (episodio != null) {
            List<EvaluacionFisicaEpisodio> evaluaciones = evaluacionRepository
                    .findByEpisodioClinicoIdOrderByFechaEvaluacionAsc(episodio.getId());

            if (!evaluaciones.isEmpty()) {
                EvaluacionFisicaEpisodio primera = evaluaciones.get(0);
                evalInicial = new EvaluacionReferencia(
                        primera.getId(),
                        primera.getFechaEvaluacion(),
                        primera.getEva(),
                        primera.getPuntajeFuncionalPromedio(),
                        primera.getInterpretacionFuncional(),
                        primera.getTipoEvaluacion());

                EvaluacionFisicaEpisodio ultima = evaluaciones.get(evaluaciones.size() - 1);
                if (!ultima.getId().equals(primera.getId())) {
                    evalReciente = new EvaluacionReferencia(
                            ultima.getId(),
                            ultima.getFechaEvaluacion(),
                            ultima.getEva(),
                            ultima.getPuntajeFuncionalPromedio(),
                            ultima.getInterpretacionFuncional(),
                            ultima.getTipoEvaluacion());
                    deltaEva = primera.getEva() - ultima.getEva(); // positivo = mejoría
                }
            }
        }

        return new ProgresoSesionResponse(
                sesionId,
                sesion.getNumeroSesionEnPlan(),
                totalPlan,
                realizadas,
                restantes,
                evalInicial,
                evalReciente,
                deltaEva,
                historial);
    }

    // ─── MAPPERS ──────────────────────────────────────────────────────────────

    private SesionTerapiaResponse toResponse(SesionTerapia s) {
        NotaSOAPResponse notaResponse = notaSOAPRepository.findBySesionTerapiaId(s.getId())
                .map(this::toNotaResponse)
                .orElse(null);

        PlanResumenResponse planResumen = s.getPlanTratamiento() != null
                ? toPlanResumen(s.getPlanTratamiento())
                : null;

        String pacienteNombre = s.getPaciente().getNombresCompletos();
        String profesionalNombre = s.getProfesional().getName() + " " + s.getProfesional().getLastName();

        return new SesionTerapiaResponse(
                s.getId(),
                s.getCita().getId(),
                s.getPlanTratamiento() != null ? s.getPlanTratamiento().getId() : null,
                s.getPaciente().getId(),
                pacienteNombre,
                s.getEpisodioClinico() != null ? s.getEpisodioClinico().getId() : null,
                s.getProfesional().getId(),
                profesionalNombre,
                s.getCostoSesion(),
                s.getNumeroSesionEnPlan(),
                s.getFechaHoraInicio(),
                s.getEstado(),
                s.getFirmadoPor() != null ? s.getFirmadoPor().getId() : null,
                s.getFirmadoEn(),
                s.getHashIntegridad(),
                s.getFechaCreacion(),
                notaResponse,
                planResumen);
    }

    private PlanResumenResponse toPlanResumen(PlanTratamiento plan) {
        List<String> objetivos = List.of();
        try {
            if (plan.getObjetivosEspecificosJson() != null) {
                objetivos = objectMapper.readValue(plan.getObjetivosEspecificosJson(), STRING_LIST_TYPE);
            }
        } catch (Exception ignored) {}
        return new PlanResumenResponse(
                plan.getId(),
                plan.getObjetivoGeneral(),
                objetivos,
                plan.getIndicacionesEducativas(),
                plan.getSesionesPlanificadas(),
                plan.getSesionesRealizadas(),
                plan.getCostoSesion(),
                plan.getEstado());
    }

    private NotaSOAPResponse toNotaResponse(NotaSOAP n) {
        String firmadoPorNombre = n.getFirmadoPor() != null
                ? n.getFirmadoPor().getName() + " " + n.getFirmadoPor().getLastName()
                : null;
        return new NotaSOAPResponse(
                n.getId(),
                n.getSesionTerapia().getId(),
                n.getSubjetivo(),
                n.getObjetivo(),
                n.getAnalisis(),
                n.getPlan(),
                n.isModoBorrador(),
                n.getFirmadoPor() != null ? n.getFirmadoPor().getId() : null,
                firmadoPorNombre,
                n.getFirmadoEn(),
                n.getHashIntegridad(),
                n.getFechaCreacion(),
                n.getFechaModificacion());
    }

    // ─── HELPERS ──────────────────────────────────────────────────────────────

    private SesionTerapia getSesionOrThrow(Long sesionId) {
        return sesionRepository.findById(sesionId)
                .orElseThrow(() -> new IllegalArgumentException("Sesión no encontrada: " + sesionId));
    }

    private Usuario resolverUsuario(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + email));
    }

    private Long resolverActorId(String email) {
        return usuarioRepository.findByEmail(email).map(Usuario::getId).orElse(0L);
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }
}
