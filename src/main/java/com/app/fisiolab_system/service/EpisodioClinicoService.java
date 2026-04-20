package com.app.fisiolab_system.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.app.fisiolab_system.dto.AdmisionEpisodioResponse;
import com.app.fisiolab_system.dto.CerrarEpisodioRequest;
import com.app.fisiolab_system.dto.CreateEpisodioClinicoRequest;
import com.app.fisiolab_system.dto.EgresoEpisodioResponse;
import com.app.fisiolab_system.dto.EpisodioClinicoResponse;
import com.app.fisiolab_system.dto.EpisodioClinicoContenidoResponse;
import com.app.fisiolab_system.dto.ConsultaResumeItemResponse;
import com.app.fisiolab_system.dto.PaginatedResponse;
import com.app.fisiolab_system.dto.PlanTratamientoConSeguimientosResponse;
import com.app.fisiolab_system.dto.PlanTratamientoResponse;
import com.app.fisiolab_system.dto.ProblemaEpisodioResponse;
import com.app.fisiolab_system.dto.RegistrarAdmisionRequest;
import com.app.fisiolab_system.dto.RegistrarEgresoRequest;
import com.app.fisiolab_system.model.AdmisionEpisodio;
import com.app.fisiolab_system.model.EgresoEpisodio;
import com.app.fisiolab_system.model.EpisodioClinico;
import com.app.fisiolab_system.model.EstadoEpisodioClinico;
import com.app.fisiolab_system.model.HistoriaClinica;
import com.app.fisiolab_system.model.Paciente;
import com.app.fisiolab_system.model.PlanTratamiento;
import com.app.fisiolab_system.model.ProblemaEpisodio;
import com.app.fisiolab_system.model.Usuario;
import com.app.fisiolab_system.repository.AdmisionEpisodioRepository;
import com.app.fisiolab_system.repository.EgresoEpisodioRepository;
import com.app.fisiolab_system.repository.EpisodioClinicoRepository;
import com.app.fisiolab_system.repository.HistoriaClinicaRepository;
import com.app.fisiolab_system.repository.PacienteRepository;
import com.app.fisiolab_system.repository.PlanTratamientoRepository;
import com.app.fisiolab_system.repository.ProblemaEpisodioRepository;
import com.app.fisiolab_system.repository.SeguimientoPlanRepository;
import com.app.fisiolab_system.repository.UsuarioRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class EpisodioClinicoService {

    private final EpisodioClinicoRepository episodioClinicoRepository;
    private final AdmisionEpisodioRepository admisionEpisodioRepository;
    private final EgresoEpisodioRepository egresoEpisodioRepository;
    private final HistoriaClinicaRepository historiaClinicaRepository;
    private final PacienteRepository pacienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final AuditoriaService auditoriaService;
    private final ProblemaEpisodioRepository problemaEpisodioRepository;
    private final EvaluacionFisicaService evaluacionFisicaService;
    private final PlanTratamientoService planTratamientoService;

    private final PlanTratamientoRepository planTratamientoRepository;
    private final SeguimientoPlanRepository seguimientoPlanRepository;

    public EpisodioClinicoService(
            EpisodioClinicoRepository episodioClinicoRepository,
            AdmisionEpisodioRepository admisionEpisodioRepository,
            EgresoEpisodioRepository egresoEpisodioRepository,
            HistoriaClinicaRepository historiaClinicaRepository,
            PacienteRepository pacienteRepository,
            UsuarioRepository usuarioRepository,
            AuditoriaService auditoriaService,
            ProblemaEpisodioRepository problemaEpisodioRepository,
            EvaluacionFisicaService evaluacionFisicaService,
            PlanTratamientoService planTratamientoService,
            PlanTratamientoRepository planTratamientoRepository,
            SeguimientoPlanRepository seguimientoPlanRepository) {
        this.episodioClinicoRepository = episodioClinicoRepository;
        this.admisionEpisodioRepository = admisionEpisodioRepository;
        this.egresoEpisodioRepository = egresoEpisodioRepository;
        this.historiaClinicaRepository = historiaClinicaRepository;
        this.pacienteRepository = pacienteRepository;
        this.usuarioRepository = usuarioRepository;
        this.auditoriaService = auditoriaService;
        this.problemaEpisodioRepository = problemaEpisodioRepository;
        this.evaluacionFisicaService = evaluacionFisicaService;
        this.planTratamientoService = planTratamientoService;
        this.planTratamientoRepository = planTratamientoRepository;
        this.seguimientoPlanRepository = seguimientoPlanRepository;
    }

    public EpisodioClinicoResponse abrirEpisodio(CreateEpisodioClinicoRequest request, String actorEmail, String clientIp) {
        HistoriaClinica hc = historiaClinicaRepository.findByNumeroHcl(request.pacienteId())
                .orElseThrow(() -> new IllegalArgumentException("Historia clinica no encontrada para paciente: " + request.pacienteId()));

        Integer ultimo = episodioClinicoRepository.findMaxSecuencialByHistoriaClinicaId(hc.getId());
        int siguiente = (ultimo == null ? 0 : ultimo) + 1;
        String numeroEpisodio = String.format("EP-%04d", siguiente);

        EpisodioClinico episodio = EpisodioClinico.builder()
                .historiaClinica(hc)
                .numeroSecuencial(siguiente)
                .numeroEpisodio(numeroEpisodio)
                .motivoConsulta(request.motivo().trim())
                .estado(EstadoEpisodioClinico.ABIERTO)
                .build();

        EpisodioClinico saved = episodioClinicoRepository.save(episodio);

        auditar(actorEmail,
                "APERTURA_EPISODIO",
                "Apertura de episodio " + saved.getNumeroEpisodio() + " para HC " + hc.getNumeroHcl(),
                clientIp);

        return toEpisodioResponse(saved);
    }

    public AdmisionEpisodioResponse registrarAdmision(Long episodioId, RegistrarAdmisionRequest request, String actorEmail,
            String clientIp) {
        EpisodioClinico episodio = getEpisodioOrThrow(episodioId);

        admisionEpisodioRepository.findByEpisodioClinicoId(episodioId).ifPresent(x -> {
            throw new IllegalStateException("El episodio ya tiene una admision registrada.");
        });

        AdmisionEpisodio admision = AdmisionEpisodio.builder()
                .episodioClinico(episodio)
                .fechaHoraAdmision(request.fechaHoraAdmision())
                .tipoAtencion(request.tipoAtencion())
                .motivoAtencion(request.motivoAtencion().trim())
                .profesionalAtiende(request.profesionalAtiende().trim())
                .build();

        AdmisionEpisodio saved = admisionEpisodioRepository.save(admision);

        auditar(actorEmail,
                "REGISTRO_ADMISION",
                "Registro de admision para episodio " + episodio.getNumeroEpisodio(),
                clientIp);

        return toAdmisionResponse(saved);
    }

    public EgresoEpisodioResponse registrarEgreso(Long episodioId, RegistrarEgresoRequest request, String actorEmail,
            String clientIp) {
        EpisodioClinico episodio = getEpisodioOrThrow(episodioId);

        egresoEpisodioRepository.findByEpisodioClinicoId(episodioId).ifPresent(x -> {
            throw new IllegalStateException("El episodio ya tiene un egreso registrado.");
        });

        EgresoEpisodio egreso = EgresoEpisodio.builder()
                .episodioClinico(episodio)
                .fechaHoraEgreso(request.fechaHoraEgreso())
                .condicionSalida(request.condicionSalida().trim())
                .causaAlta(request.causaAlta().trim())
                .destinoPaciente(request.destinoPaciente().trim())
                .referidoOtraInstitucion(request.referidoOtraInstitucion())
                .build();

        EgresoEpisodio saved = egresoEpisodioRepository.save(egreso);

        auditar(actorEmail,
                "REGISTRO_EGRESO",
                "Registro de egreso para episodio " + episodio.getNumeroEpisodio(),
                clientIp);

        return toEgresoResponse(saved);
    }

    public List<EpisodioClinicoResponse> historialPorNumeroHcl(String numeroHcl) {
        HistoriaClinica hc = historiaClinicaRepository.findByNumeroHcl(numeroHcl)
                .orElseThrow(() -> new IllegalArgumentException("Historia clinica no encontrada para HCL: " + numeroHcl));

        return episodioClinicoRepository.findByHistoriaClinicaIdOrderByFechaAperturaDesc(hc.getId())
                .stream()
                .map(this::toEpisodioResponse)
                .toList();
    }

    public EpisodioClinicoResponse cerrarEpisodio(Long episodioId, CerrarEpisodioRequest request, String actorEmail,
            String clientIp) {
        EpisodioClinico episodio = getEpisodioOrThrow(episodioId);
        if (episodio.getEstado() == EstadoEpisodioClinico.CERRADO) {
            throw new IllegalStateException("El episodio ya se encuentra cerrado.");
        }

        episodio.setEstado(EstadoEpisodioClinico.CERRADO);
        episodio.setEstadoCierre(request.estadoCierre());
        episodio.setObservacionCierre(normalizeNullable(request.observacionCierre()));
        episodio.setFechaCierre(LocalDateTime.now());

        EpisodioClinico saved = episodioClinicoRepository.save(episodio);

        Paciente paciente = saved.getHistoriaClinica().getPaciente();
        paciente.setFechaUltimaAtencion(LocalDateTime.now());
        pacienteRepository.save(paciente);

        auditar(actorEmail,
                "CIERRE_EPISODIO",
                "Cierre de episodio " + saved.getNumeroEpisodio() + " para HC " + saved.getHistoriaClinica().getNumeroHcl(),
                clientIp);

        return toEpisodioResponse(saved);
    }

    private EpisodioClinico getEpisodioOrThrow(Long episodioId) {
        return episodioClinicoRepository.findById(episodioId)
                .orElseThrow(() -> new IllegalArgumentException("Episodio no encontrado: " + episodioId));
    }

    private void auditar(String actorEmail, String accion, String detalle, String ip) {
        Long actorId = usuarioRepository.findByEmail(actorEmail)
                .map(Usuario::getId)
                .orElse(0L);
        auditoriaService.registrar(actorId, accion, detalle, ip);
    }

    private EpisodioClinicoResponse toEpisodioResponse(EpisodioClinico episodio) {
        return new EpisodioClinicoResponse(
                episodio.getId(),
                episodio.getHistoriaClinica().getId(),
                episodio.getHistoriaClinica().getNumeroHcl(),
                episodio.getNumeroSecuencial(),
                episodio.getNumeroEpisodio(),
                episodio.getMotivoConsulta(),
                episodio.getFechaApertura(),
                episodio.getFechaCierre(),
                episodio.getEstado(),
                episodio.getEstadoCierre(),
                episodio.getObservacionCierre());
    }

    private AdmisionEpisodioResponse toAdmisionResponse(AdmisionEpisodio admision) {
        return new AdmisionEpisodioResponse(
                admision.getId(),
                admision.getEpisodioClinico().getId(),
                admision.getFechaHoraAdmision(),
                admision.getTipoAtencion(),
                admision.getMotivoAtencion(),
                admision.getProfesionalAtiende());
    }

    private EgresoEpisodioResponse toEgresoResponse(EgresoEpisodio egreso) {
        return new EgresoEpisodioResponse(
                egreso.getId(),
                egreso.getEpisodioClinico().getId(),
                egreso.getFechaHoraEgreso(),
                egreso.getCondicionSalida(),
                egreso.getCausaAlta(),
                egreso.getDestinoPaciente(),
                egreso.isReferidoOtraInstitucion());
    }

    private String normalizeNullable(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public EpisodioClinicoContenidoResponse obtenerContenidoCompleto(Long episodioId) {
        EpisodioClinico episodio = getEpisodioOrThrow(episodioId);

        // Obtener problemas del episodio
        List<ProblemaEpisodioResponse> problemas = problemaEpisodioRepository
                .findByEpisodioClinicoIdOrderByNumeroSecuencialAsc(episodioId)
                .stream()
                .map(this::toProblemaResponse)
                .toList();

        // Obtener evaluaciones del episodio
        var evaluaciones = evaluacionFisicaService.listarPorEpisodio(episodioId);

        // Obtener planes de tratamiento con sus seguimientos
        List<PlanTratamientoConSeguimientosResponse> planesTratamiento = planTratamientoService
                .listarPorEpisodio(episodioId)
                .stream()
                .map(planResponse -> convertirPlanConSeguimientos(planResponse))
                .toList();

        return new EpisodioClinicoContenidoResponse(
                episodio.getId(),
                episodio.getHistoriaClinica().getId(),
                episodio.getHistoriaClinica().getNumeroHcl(),
                episodio.getNumeroSecuencial(),
                episodio.getNumeroEpisodio(),
                episodio.getMotivoConsulta(),
                episodio.getFechaApertura(),
                episodio.getFechaCierre(),
                episodio.getEstado(),
                episodio.getEstadoCierre(),
                episodio.getObservacionCierre(),
                problemas,
                evaluaciones,
                planesTratamiento);
    }

    private PlanTratamientoConSeguimientosResponse convertirPlanConSeguimientos(
            PlanTratamientoResponse planResponse) {
        var seguimientos = planTratamientoService.listarSeguimientos(
                planResponse.episodioClinicoId(),
                planResponse.problemaEpisodioId());

        return new PlanTratamientoConSeguimientosResponse(
                planResponse.id(),
                planResponse.episodioClinicoId(),
                planResponse.problemaEpisodioId(),
                planResponse.objetivoGeneral(),
                planResponse.objetivosEspecificos(),
                planResponse.fechaInicio(),
                planResponse.fechaFinEstimada(),
                planResponse.sesionesPlanificadas(),
                planResponse.sesionesRealizadas(),
                planResponse.porcentajeAvanceTotal(),
                planResponse.indicacionesEducativas(),
                planResponse.codigoAlarma(),
                planResponse.estado(),
                planResponse.fechaCreacion(),
                planResponse.costoSesion(),
                seguimientos);
    }

    private ProblemaEpisodioResponse toProblemaResponse(ProblemaEpisodio problema) {
        ProblemaEpisodioResponse response = new ProblemaEpisodioResponse();
        response.setId(problema.getId());
        response.setNumeroSecuencial(problema.getNumeroSecuencial());
        response.setDescripcion(problema.getDescripcion());
        response.setCodigoCie10(problema.getCodigoCie10());
        response.setEstado(problema.getEstado());
        return response;
    }

    public PaginatedResponse<ConsultaResumeItemResponse> obtenerResumenConsultas(String search, int page, int limit) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page - 1, limit);
        org.springframework.data.domain.Page<Paciente> pacientePage = pacienteRepository.buscarConsultasResumen(search, pageable);

        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");

        List<ConsultaResumeItemResponse> data = pacientePage.getContent().stream().map(paciente -> {
            HistoriaClinica hc = historiaClinicaRepository.findByPacienteId(paciente.getId()).orElse(null);
            
            int totalConsultas = 0;
            String ultimaConsultaFecha = "";
            String ultimaConsultaMotivo = "";
            boolean tienePlanTratamiento = false;
            String estadoSeguimiento = "Pendiente de Evaluación";

            if (hc != null) {
                List<EpisodioClinico> episodios = episodioClinicoRepository.findByHistoriaClinicaIdOrderByFechaAperturaDesc(hc.getId());
                totalConsultas = episodios.size();
                
                if (!episodios.isEmpty()) {
                    EpisodioClinico ultimo = episodios.get(0);
                    ultimaConsultaFecha = ultimo.getFechaApertura() != null ? ultimo.getFechaApertura().format(formatter) : "";
                    ultimaConsultaMotivo = ultimo.getMotivoConsulta() != null ? ultimo.getMotivoConsulta() : "";
                    
                    tienePlanTratamiento = !planTratamientoService.listarPorEpisodio(ultimo.getId()).isEmpty();
                    
                    if (ultimo.getEstado() == EstadoEpisodioClinico.CERRADO) {
                        estadoSeguimiento = "Finalizado";
                    } else if (tienePlanTratamiento) {
                        estadoSeguimiento = "En Progreso";
                    } else {
                        estadoSeguimiento = "Pendiente de Evaluación";
                    }
                }
            }

            return ConsultaResumeItemResponse.builder()
                    .id(String.valueOf(paciente.getId()))
                    .hcl(paciente.getNumeroHcl())
                    .nombresCompletos(paciente.getNombresCompletos())
                    .totalConsultas(totalConsultas)
                    .ultimaConsultaFecha(ultimaConsultaFecha)
                    .ultimaConsultaMotivo(ultimaConsultaMotivo)
                    .tienePlanTratamiento(tienePlanTratamiento)
                    .estadoSeguimiento(estadoSeguimiento)
                    .build();
        }).toList();

        PaginatedResponse.Meta meta = PaginatedResponse.Meta.builder()
                .totalItems(pacientePage.getTotalElements())
                .itemCount(data.size())
                .itemsPerPage(limit)
                .totalPages(pacientePage.getTotalPages())
                .currentPage(page)
                .build();

        return new PaginatedResponse<>(data, meta);
    }

    public PaginatedResponse<com.app.fisiolab_system.dto.PlanResumenGlobalResponse> obtenerResumenPlanes(int page, int limit) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page - 1, limit);
        org.springframework.data.domain.Page<PlanTratamiento> planesPage =
            planTratamientoRepository.findAllWithDetails(pageable);

        List<com.app.fisiolab_system.dto.PlanResumenGlobalResponse> data = planesPage.getContent().stream().map(p -> {
            com.app.fisiolab_system.model.SeguimientoPlan ultimo = (p.getSeguimientos() == null || p.getSeguimientos().isEmpty())
                ? null
                : p.getSeguimientos().get(p.getSeguimientos().size() - 1);

            double porcentaje = p.getSesionesPlanificadas() > 0
                ? p.getSesionesRealizadas() * 100.0 / p.getSesionesPlanificadas()
                : 0.0;

            return com.app.fisiolab_system.dto.PlanResumenGlobalResponse.builder()
                .id(String.valueOf(p.getId()))
                .hcl(p.getEpisodioClinico().getHistoriaClinica().getNumeroHcl())
                .paciente(p.getEpisodioClinico().getHistoriaClinica().getPaciente().getNombresCompletos())
                .episodioId(p.getEpisodioClinico().getId())
                .problemaId(p.getProblemaEpisodio().getId())
                .problemaDescripcion(p.getProblemaEpisodio().getDescripcion())
                .codigoCie10(p.getProblemaEpisodio().getCodigoCie10())
                .objetivoGeneral(p.getObjetivoGeneral())
                .sesionesPlanificadas(p.getSesionesPlanificadas())
                .sesionesRealizadas(p.getSesionesRealizadas())
                .porcentajeAvance(porcentaje)
                .codigoAlarma(p.getCodigoAlarma() != null ? p.getCodigoAlarma().name() : null)
                .estado(p.getEstado() != null ? p.getEstado().name() : null)
                .ultimoResultado(ultimo != null ? ultimo.getResultadoGeneral().name() : null)
                .fechaInicio(p.getFechaInicio() != null ? p.getFechaInicio().toString() : null)
                .fechaFinEstimada(p.getFechaFinEstimada() != null ? p.getFechaFinEstimada().toString() : null)
                .build();
        }).toList();

        PaginatedResponse.Meta meta = PaginatedResponse.Meta.builder()
            .totalItems(planesPage.getTotalElements())
            .itemCount(data.size())
            .itemsPerPage(limit)
            .totalPages(planesPage.getTotalPages())
            .currentPage(page)
            .build();

        return new PaginatedResponse<>(data, meta);
    }

    public com.app.fisiolab_system.dto.ContextoPlanesResponse contextoPlanes(Long pacienteId) {
        com.app.fisiolab_system.model.Paciente pac = pacienteRepository.findById(pacienteId)
            .orElseThrow(() -> new IllegalArgumentException("Paciente no encontrado: " + pacienteId));

        com.app.fisiolab_system.model.HistoriaClinica hc = historiaClinicaRepository.findByPacienteId(pacienteId)
            .orElseThrow(() -> new IllegalArgumentException("Historia clínica no encontrada para paciente: " + pacienteId));

        List<EpisodioClinico> episodios = episodioClinicoRepository
            .findByHistoriaClinicaIdAndEstadoInOrderByFechaAperturaDesc(
                hc.getId(),
                List.of(EstadoEpisodioClinico.ABIERTO, EstadoEpisodioClinico.ADMITIDO));

        List<com.app.fisiolab_system.dto.ContextoPlanesResponse.EpisodioItem> episodioItems = episodios.stream()
            .map(ep -> {
                List<PlanTratamiento> planes = planTratamientoRepository
                    .findByEpisodioClinicoIdOrderByFechaCreacionDesc(ep.getId());

                List<com.app.fisiolab_system.dto.ContextoPlanesResponse.PlanItem> planItems = planes.stream()
                    .map(p -> {
                        int realizadas = (int) seguimientoPlanRepository.countByPlanTratamientoId(p.getId());
                        int porcentaje = p.getSesionesPlanificadas() > 0
                            ? Math.min(100, realizadas * 100 / p.getSesionesPlanificadas())
                            : 0;
                        return com.app.fisiolab_system.dto.ContextoPlanesResponse.PlanItem.builder()
                            .planId(p.getId())
                            .problemaId(p.getProblemaEpisodio().getId())
                            .problemaDescripcion(p.getProblemaEpisodio().getDescripcion())
                            .codigoCie10(p.getProblemaEpisodio().getCodigoCie10())
                            .objetivoGeneral(p.getObjetivoGeneral())
                            .sesionesPlanificadas(p.getSesionesPlanificadas())
                            .sesionesRealizadas(realizadas)
                            .porcentajeAvance(porcentaje)
                            .codigoAlarma(p.getCodigoAlarma() != null ? p.getCodigoAlarma().name() : null)
                            .estado(p.getEstado() != null ? p.getEstado().name() : null)
                            .fechaInicio(p.getFechaInicio())
                            .fechaFinEstimada(p.getFechaFinEstimada())
                            .build();
                    })
                    .toList();

                return com.app.fisiolab_system.dto.ContextoPlanesResponse.EpisodioItem.builder()
                    .episodioId(ep.getId())
                    .numeroEpisodio(ep.getNumeroEpisodio())
                    .motivoConsulta(ep.getMotivoConsulta())
                    .estado(ep.getEstado().name())
                    .fechaApertura(ep.getFechaApertura())
                    .planes(planItems)
                    .build();
            })
            .toList();

        return com.app.fisiolab_system.dto.ContextoPlanesResponse.builder()
            .pacienteId(pac.getId())
            .pacienteNombre(pac.getNombresCompletos())
            .hcl(pac.getNumeroHcl())
            .episodios(episodioItems)
            .build();
    }
}
