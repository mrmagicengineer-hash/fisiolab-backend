package com.app.fisiolab_system.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.app.fisiolab_system.dto.AntecedenteFamiliarResponse;
import com.app.fisiolab_system.dto.AntecedentePersonalResponse;
import com.app.fisiolab_system.dto.ProblemaEpisodioResponse;
import com.app.fisiolab_system.dto.CreateAntecedenteFamiliarRequest;
import com.app.fisiolab_system.dto.CreateAntecedentePersonalRequest;
import com.app.fisiolab_system.dto.HistoriaClinicaCompletaResponse;
import com.app.fisiolab_system.dto.HistoriaClinicaResumenResponse;
import com.app.fisiolab_system.model.AntecedenteFamiliar;
import com.app.fisiolab_system.model.AntecedentePersonal;
import com.app.fisiolab_system.model.EpisodioClinico;
import com.app.fisiolab_system.model.EstadoArchivoPaciente;
import com.app.fisiolab_system.model.EstadoHistoriaClinica;
import com.app.fisiolab_system.model.EstadoProblemaEpisodio;
import com.app.fisiolab_system.model.HistoriaClinica;
import com.app.fisiolab_system.model.Paciente;
import com.app.fisiolab_system.model.ProblemaEpisodio;
import com.app.fisiolab_system.model.Usuario;
import com.app.fisiolab_system.repository.AntecedenteFamiliarRepository;
import com.app.fisiolab_system.repository.AntecedentePersonalRepository;
import com.app.fisiolab_system.repository.EpisodioClinicoRepository;
import com.app.fisiolab_system.repository.HistoriaClinicaRepository;
import com.app.fisiolab_system.repository.ProblemaEpisodioRepository;
import com.app.fisiolab_system.repository.PacienteRepository;
import com.app.fisiolab_system.repository.UsuarioRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class HistoriaClinicaService {

    private final HistoriaClinicaRepository historiaClinicaRepository;
    private final PacienteRepository pacienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final AntecedentePersonalRepository antecedentePersonalRepository;
    private final AntecedenteFamiliarRepository antecedenteFamiliarRepository;
    private final EpisodioClinicoRepository episodioClinicoRepository;
    private final ProblemaEpisodioRepository problemaEpisodioRepository;
    private final AuditoriaService auditoriaService;

    @Value("${app.timezone:UTC}")
    private String appTimezone;

    @Value("${app.unidad-salud:Clinica Fisiolab}")
    private String unidadSalud;

    public HistoriaClinicaService(
            HistoriaClinicaRepository historiaClinicaRepository,
            PacienteRepository pacienteRepository,
            UsuarioRepository usuarioRepository,
            AntecedentePersonalRepository antecedentePersonalRepository,
            AntecedenteFamiliarRepository antecedenteFamiliarRepository,
            EpisodioClinicoRepository episodioClinicoRepository,
            ProblemaEpisodioRepository problemaEpisodioRepository,
            AuditoriaService auditoriaService) {
        this.historiaClinicaRepository = historiaClinicaRepository;
        this.pacienteRepository = pacienteRepository;
        this.usuarioRepository = usuarioRepository;
        this.antecedentePersonalRepository = antecedentePersonalRepository;
        this.antecedenteFamiliarRepository = antecedenteFamiliarRepository;
        this.episodioClinicoRepository = episodioClinicoRepository;
        this.problemaEpisodioRepository = problemaEpisodioRepository;
        this.auditoriaService = auditoriaService;
    }

    public HistoriaClinica asegurarAperturaAutomatica(Paciente paciente) {
        return historiaClinicaRepository.findByPacienteId(paciente.getId())
                .orElseGet(() -> historiaClinicaRepository.save(HistoriaClinica.builder()
                        .paciente(paciente)
                        .numeroHcl(paciente.getNumeroHcl())
                        .unidadSalud(unidadSalud)
                        .estado(EstadoHistoriaClinica.ABIERTA)
                        .build()));
    }

        public AntecedentePersonalResponse registrarAntecedentePersonal(String numeroHcl,
            CreateAntecedentePersonalRequest request,
            String actorEmail,
            String clientIp) {
                HistoriaClinica hc = getHistoriaOrThrowByNumeroHcl(numeroHcl);

        AntecedentePersonal antecedente = AntecedentePersonal.builder()
                .historiaClinica(hc)
                .tipo(request.tipo())
                .descripcion(request.descripcion().trim())
                .codigoCie10(normalizeNullable(request.codigoCie10()))
                .estado(request.estado())
                .build();

        AntecedentePersonal saved = antecedentePersonalRepository.save(antecedente);
        auditar(actorEmail, "ANTECEDENTE_PERSONAL_REGISTRADO",
                "Registro de antecedente personal en HC: " + hc.getNumeroHcl(), clientIp);

        return toPersonalResponse(saved);
    }

        public AntecedenteFamiliarResponse registrarAntecedenteFamiliar(String numeroHcl,
            CreateAntecedenteFamiliarRequest request,
            String actorEmail,
            String clientIp) {
                HistoriaClinica hc = getHistoriaOrThrowByNumeroHcl(numeroHcl);

        AntecedenteFamiliar antecedente = AntecedenteFamiliar.builder()
                .historiaClinica(hc)
                .parentesco(request.parentesco().trim())
                .condicion(request.condicion().trim())
                .codigoCie10(normalizeNullable(request.codigoCie10()))
                .build();

        AntecedenteFamiliar saved = antecedenteFamiliarRepository.save(antecedente);
        auditar(actorEmail, "ANTECEDENTE_FAMILIAR_REGISTRADO",
                "Registro de antecedente familiar en HC: " + hc.getNumeroHcl(), clientIp);

        return toFamiliarResponse(saved);
    }

        	public HistoriaClinicaCompletaResponse obtenerHistoriaCompleta(String numeroHcl) {
		HistoriaClinica hc = getHistoriaOrThrowByNumeroHcl(numeroHcl);
		Long historiaClinicaId = hc.getId();

        List<AntecedentePersonalResponse> personales = antecedentePersonalRepository
                .findByHistoriaClinicaIdOrderByFechaRegistroDesc(historiaClinicaId)
                .stream()
                .map(this::toPersonalResponse)
                .toList();

        List<AntecedenteFamiliarResponse> familiares = antecedenteFamiliarRepository
                .findByHistoriaClinicaIdOrderByFechaRegistroDesc(historiaClinicaId)
                .stream()
                .map(this::toFamiliarResponse)
                .toList();

        // Obtener problemas activos reales desde ProblemaEpisodio (no desde antecedentes)
        List<Long> episodioIds = episodioClinicoRepository
                .findByHistoriaClinicaIdOrderByFechaAperturaDesc(historiaClinicaId)
                .stream()
                .map(EpisodioClinico::getId)
                .toList();

        List<ProblemaEpisodioResponse> problemasActivos = episodioIds.isEmpty()
                ? List.of()
                : problemaEpisodioRepository
                        .findByEpisodioClinicoIdInAndEstadoIn(episodioIds,
                                List.of(EstadoProblemaEpisodio.ACTIVO, EstadoProblemaEpisodio.CRONICO))
                        .stream()
                        .map(this::toProblemaResponse)
                        .toList();

        HistoriaClinicaResumenResponse resumen = toResumen(hc);

        return new HistoriaClinicaCompletaResponse(
                resumen,
                personales,
                familiares,
                problemasActivos,
                List.of());
    }

        public List<AntecedentePersonalResponse> listarAntecedentesPersonales(String numeroHcl) {
                HistoriaClinica hc = getHistoriaOrThrowByNumeroHcl(numeroHcl);
                return antecedentePersonalRepository.findByHistoriaClinicaIdOrderByFechaRegistroDesc(hc.getId())
                                .stream()
                                .map(this::toPersonalResponse)
                                .toList();
        }

        public List<AntecedenteFamiliarResponse> listarAntecedentesFamiliares(String numeroHcl) {
                HistoriaClinica hc = getHistoriaOrThrowByNumeroHcl(numeroHcl);
                return antecedenteFamiliarRepository.findByHistoriaClinicaIdOrderByFechaRegistroDesc(hc.getId())
                                .stream()
                                .map(this::toFamiliarResponse)
                                .toList();
        }

    public HistoriaClinicaResumenResponse obtenerResumenPorPaciente(Long pacienteId) {
        HistoriaClinica hc = historiaClinicaRepository.findByPacienteId(pacienteId)
                .orElseThrow(() -> new IllegalArgumentException("Historia clinica no encontrada para paciente: " + pacienteId));
        return toResumen(hc);
    }

    public List<HistoriaClinicaResumenResponse> listarCandidatasDepuracion() {
        LocalDateTime limite = LocalDateTime.now(ZoneId.of(appTimezone)).minusYears(15);

        return pacienteRepository.findByEstadoArchivoAndFechaUltimaAtencionBefore(
                EstadoArchivoPaciente.PASIVO,
                limite).stream()
                .map(p -> historiaClinicaRepository.findByPacienteId(p.getId()).orElse(null))
                .filter(hc -> hc != null)
                .map(this::toResumen)
                .toList();
    }

        public void depurarHistoriaClinica(String numeroHcl, String actorEmail, String clientIp) {
                HistoriaClinica hc = getHistoriaOrThrowByNumeroHcl(numeroHcl);
                Long historiaClinicaId = hc.getId();
        Paciente paciente = hc.getPaciente();

        LocalDateTime limite = LocalDateTime.now(ZoneId.of(appTimezone)).minusYears(15);
        boolean esCandidata = paciente.getEstadoArchivo() == EstadoArchivoPaciente.PASIVO
                && paciente.getFechaUltimaAtencion() != null
                && paciente.getFechaUltimaAtencion().isBefore(limite);

        if (!esCandidata) {
            throw new IllegalStateException("La historia clinica no cumple criterios de depuracion (15+ anos en pasivo).");
        }

        antecedentePersonalRepository.deleteByHistoriaClinicaId(historiaClinicaId);
        antecedenteFamiliarRepository.deleteByHistoriaClinicaId(historiaClinicaId);
        historiaClinicaRepository.delete(hc);

        auditar(actorEmail,
                "DEPURACION_HC",
                "Depuracion aprobada y ejecutada para HC: " + hc.getNumeroHcl(),
                clientIp);
    }

        private HistoriaClinica getHistoriaOrThrowByNumeroHcl(String numeroHcl) {
                return historiaClinicaRepository.findByNumeroHcl(numeroHcl)
                                .orElseThrow(() -> new IllegalArgumentException("Historia clinica no encontrada para HCL: " + numeroHcl));
    }

    private HistoriaClinicaResumenResponse toResumen(HistoriaClinica hc) {
        Paciente p = hc.getPaciente();
        return new HistoriaClinicaResumenResponse(
                hc.getId(),
                p.getId(),
                hc.getNumeroHcl(),
                p.getNombresCompletos(),
                p.getCedula(),
                hc.getUnidadSalud(),
                hc.getEstado(),
                p.getEstadoArchivo(),
                hc.getFechaApertura());
    }

    private AntecedentePersonalResponse toPersonalResponse(AntecedentePersonal antecedente) {
        return new AntecedentePersonalResponse(
                antecedente.getId(),
                antecedente.getTipo(),
                antecedente.getDescripcion(),
                antecedente.getCodigoCie10(),
                antecedente.getEstado(),
                antecedente.getFechaRegistro());
    }

    private AntecedenteFamiliarResponse toFamiliarResponse(AntecedenteFamiliar antecedente) {
        return new AntecedenteFamiliarResponse(
                antecedente.getId(),
                antecedente.getParentesco(),
                antecedente.getCondicion(),
                antecedente.getCodigoCie10(),
                antecedente.getFechaRegistro());
    }

    private void auditar(String actorEmail, String accion, String detalle, String ip) {
        Long actorId = usuarioRepository.findByEmail(actorEmail)
                .map(Usuario::getId)
                .orElse(0L);
        auditoriaService.registrar(actorId, accion, detalle, ip);
    }

    private ProblemaEpisodioResponse toProblemaResponse(ProblemaEpisodio p) {
        ProblemaEpisodioResponse r = new ProblemaEpisodioResponse();
        r.setId(p.getId());
        r.setNumeroSecuencial(p.getNumeroSecuencial());
        r.setDescripcion(p.getDescripcion());
        r.setCodigoCie10(p.getCodigoCie10());
        r.setEstado(p.getEstado());
        return r;
    }

    private String normalizeNullable(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
