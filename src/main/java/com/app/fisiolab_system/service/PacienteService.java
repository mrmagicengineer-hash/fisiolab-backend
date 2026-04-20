package com.app.fisiolab_system.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.Year;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.app.fisiolab_system.dto.AntecedenteFamiliarResponse;
import com.app.fisiolab_system.dto.AntecedentePersonalResponse;
import com.app.fisiolab_system.dto.ContextoAgendamientoResponse;
import com.app.fisiolab_system.dto.ContextoAgendamientoResponse.EpisodioAgendamiento;
import com.app.fisiolab_system.dto.ContextoAgendamientoResponse.PlanAgendamiento;
import com.app.fisiolab_system.dto.CreatePacienteRequest;
import com.app.fisiolab_system.dto.EpisodioClinicoResponse;
import com.app.fisiolab_system.dto.FichaFamiliarRequest;
import com.app.fisiolab_system.dto.FichaFamiliarResponse;
import com.app.fisiolab_system.dto.HistoriaClinicaResumenResponse;
import com.app.fisiolab_system.dto.PacienteResumenCompletoResponse;
import com.app.fisiolab_system.dto.PacienteResponse;
import com.app.fisiolab_system.dto.ProblemaEpisodioResponse;
import com.app.fisiolab_system.dto.UpdatePacienteRequest;
import com.app.fisiolab_system.model.AntecedenteFamiliar;
import com.app.fisiolab_system.model.AntecedentePersonal;
import com.app.fisiolab_system.model.EpisodioClinico;
import com.app.fisiolab_system.model.EstadoArchivoPaciente;
import com.app.fisiolab_system.model.EstadoEpisodioClinico;
import com.app.fisiolab_system.model.EstadoPlan;
import com.app.fisiolab_system.model.EstadoProblemaEpisodio;
import com.app.fisiolab_system.model.FichaFamiliar;
import com.app.fisiolab_system.model.HclSecuencia;
import com.app.fisiolab_system.model.HistoriaClinica;
import com.app.fisiolab_system.model.Paciente;
import com.app.fisiolab_system.model.ProblemaEpisodio;
import com.app.fisiolab_system.model.Usuario;
import com.app.fisiolab_system.repository.AntecedenteFamiliarRepository;
import com.app.fisiolab_system.repository.AntecedentePersonalRepository;
import com.app.fisiolab_system.repository.EpisodioClinicoRepository;
import com.app.fisiolab_system.repository.EvaluacionFisicaEpisodioRepository;
import com.app.fisiolab_system.repository.FichaFamiliarRepository;
import com.app.fisiolab_system.repository.HclSecuenciaRepository;
import com.app.fisiolab_system.repository.HistoriaClinicaRepository;
import com.app.fisiolab_system.security.InputSanitizer;
import com.app.fisiolab_system.repository.PacienteRepository;
import com.app.fisiolab_system.repository.PlanTratamientoRepository;
import com.app.fisiolab_system.repository.ProblemaEpisodioRepository;
import com.app.fisiolab_system.repository.UsuarioRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class PacienteService {

    private final PacienteRepository pacienteRepository;
    private final FichaFamiliarRepository fichaFamiliarRepository;
    private final HclSecuenciaRepository hclSecuenciaRepository;
    private final UsuarioRepository usuarioRepository;
    private final AuditoriaService auditoriaService;
    private final HistoriaClinicaService historiaClinicaService;
    private final HistoriaClinicaRepository historiaClinicaRepository;
    private final AntecedentePersonalRepository antecedentePersonalRepository;
    private final AntecedenteFamiliarRepository antecedenteFamiliarRepository;
    private final EpisodioClinicoRepository episodioClinicoRepository;
    private final ProblemaEpisodioRepository problemaEpisodioRepository;
    private final EvaluacionFisicaEpisodioRepository evaluacionFisicaRepository;
    private final PlanTratamientoRepository planTratamientoRepository;

    @Value("${app.timezone:UTC}")
    private String appTimezone;

    public PacienteService(PacienteRepository pacienteRepository,
            FichaFamiliarRepository fichaFamiliarRepository,
            HclSecuenciaRepository hclSecuenciaRepository,
            UsuarioRepository usuarioRepository,
            AuditoriaService auditoriaService,
            HistoriaClinicaService historiaClinicaService,
            HistoriaClinicaRepository historiaClinicaRepository,
            AntecedentePersonalRepository antecedentePersonalRepository,
            AntecedenteFamiliarRepository antecedenteFamiliarRepository,
            EpisodioClinicoRepository episodioClinicoRepository,
            ProblemaEpisodioRepository problemaEpisodioRepository,
            EvaluacionFisicaEpisodioRepository evaluacionFisicaRepository,
            PlanTratamientoRepository planTratamientoRepository) {
        this.pacienteRepository = pacienteRepository;
        this.fichaFamiliarRepository = fichaFamiliarRepository;
        this.hclSecuenciaRepository = hclSecuenciaRepository;
        this.usuarioRepository = usuarioRepository;
        this.auditoriaService = auditoriaService;
        this.historiaClinicaService = historiaClinicaService;
        this.historiaClinicaRepository = historiaClinicaRepository;
        this.antecedentePersonalRepository = antecedentePersonalRepository;
        this.antecedenteFamiliarRepository = antecedenteFamiliarRepository;
        this.episodioClinicoRepository = episodioClinicoRepository;
        this.problemaEpisodioRepository = problemaEpisodioRepository;
        this.evaluacionFisicaRepository = evaluacionFisicaRepository;
        this.planTratamientoRepository = planTratamientoRepository;
    }

    public PacienteResponse registrarPaciente(CreatePacienteRequest request, String actorEmail, String clientIp) {
        String cedula = normalize(request.cedula());
        String email = normalizeEmail(request.email());
        if (pacienteRepository.existsByCedula(cedula)) {
            throw new IllegalArgumentException("Ya existe un paciente registrado con esa cedula.");
        }
        if (pacienteRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Ya existe un paciente registrado con ese correo electronico.");
        }

        String numeroHcl = generarSiguienteNumeroHcl();

        Paciente nuevo = Paciente.builder()
                .numeroHcl(numeroHcl)
                .cedula(cedula)
                .email(email)
                .nombresCompletos(normalize(request.nombresCompletos()))
                .fechaNacimiento(request.fechaNacimiento())
                .genero(normalize(request.genero()))
                .grupoCultural(normalizeNullable(request.grupoCultural()))
                .estadoCivil(normalizeNullable(request.estadoCivil()))
                .ocupacion(normalizeNullable(request.ocupacion()))
                .regimenSeguridadSocial(normalizeNullable(request.regimenSeguridadSocial()))
                .tipoSangre(normalizeNullable(request.tipoSangre()))
                .telefonoPrincipal(normalize(request.telefonoPrincipal()))
                .telefonoSecundario(normalizeNullable(request.telefonoSecundario()))
                .direccion(normalizeNullable(request.direccion()))
                .estadoArchivo(EstadoArchivoPaciente.ACTIVO)
                .build();

        Paciente saved = pacienteRepository.save(nuevo);
        historiaClinicaService.asegurarAperturaAutomatica(saved);

        Long actorId = usuarioRepository.findByEmail(actorEmail)
                .map(Usuario::getId)
                .orElse(0L);
        auditoriaService.registrar(actorId,
                "REGISTRO_PACIENTE",
                "Registro de paciente nuevo en tarjetero indice: " + saved.getNumeroHcl(),
                clientIp);

        return toResponse(saved);
    }

    public List<PacienteResponse> buscarPacientes(String query) {
        String sanitized = InputSanitizer.sanitizeSearchQuery(query);
        if (sanitized.length() < 3) {
            throw new IllegalArgumentException("La busqueda requiere minimo 3 caracteres.");
        }
        return pacienteRepository.buscarTarjeteroIndice(sanitized, PageRequest.of(0, 30))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<PacienteResponse> listarPacientesRegistrados() {
        return pacienteRepository.findAllByOrderByFechaRegistroDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public ContextoAgendamientoResponse getContextoAgendamiento(Long pacienteId) {
        Paciente paciente = pacienteRepository.findById(pacienteId)
                .orElseThrow(() -> new IllegalArgumentException("Paciente no encontrado: " + pacienteId));

        HistoriaClinica hc = historiaClinicaRepository.findByPacienteId(pacienteId)
                .orElseThrow(() -> new IllegalArgumentException("Historia clinica no encontrada para paciente: " + pacienteId));

        List<EpisodioClinico> episodiosAbiertos = episodioClinicoRepository
                .findByHistoriaClinicaIdOrderByFechaAperturaDesc(hc.getId())
                .stream()
                .filter(e -> e.getEstado() == EstadoEpisodioClinico.ABIERTO
                        || e.getEstado() == EstadoEpisodioClinico.ADMITIDO)
                .toList();

        List<EpisodioAgendamiento> episodiosDtos = episodiosAbiertos.stream()
                .map(ep -> {
                    List<PlanAgendamiento> planes = planTratamientoRepository
                            .findByEpisodioClinicoIdOrderByFechaCreacionDesc(ep.getId())
                            .stream()
                            .filter(p -> p.getEstado() == EstadoPlan.ACTIVO)
                            .map(p -> new PlanAgendamiento(
                                    p.getId(),
                                    p.getObjetivoGeneral(),
                                    p.getSesionesPlanificadas(),
                                    p.getSesionesRealizadas(),
                                    Math.max(0, p.getSesionesPlanificadas() - p.getSesionesRealizadas()),
                                    p.getCostoSesion(),
                                    p.getEstado()))
                            .toList();
                    return new EpisodioAgendamiento(
                            ep.getId(),
                            ep.getNumeroEpisodio(),
                            ep.getMotivoConsulta(),
                            ep.getEstado(),
                            planes);
                })
                .toList();

        return new ContextoAgendamientoResponse(pacienteId, paciente.getNombresCompletos(), episodiosDtos);
    }

    public PacienteResponse obtenerPorId(Long id) {
        Paciente paciente = pacienteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Paciente no encontrado: " + id));
        return toResponse(paciente);
    }

    public PacienteResumenCompletoResponse obtenerResumenCompleto(Long pacienteId) {
        Paciente paciente = pacienteRepository.findById(pacienteId)
                .orElseThrow(() -> new IllegalArgumentException("Paciente no encontrado: " + pacienteId));

        // 1. Datos demograficos
        PacienteResponse pacienteDto = toResponse(paciente);

        // 2. Ficha familiar (puede no existir)
        FichaFamiliarResponse fichaFamiliar = fichaFamiliarRepository.findByPacienteId(pacienteId)
                .map(this::toFichaResponse)
                .orElse(null);

        // 3. Historia clinica
        HistoriaClinica hc = historiaClinicaRepository.findByPacienteId(pacienteId)
                .orElseThrow(() -> new IllegalArgumentException("Historia clinica no encontrada para paciente: " + pacienteId));

        HistoriaClinicaResumenResponse historiaDto = new HistoriaClinicaResumenResponse(
                hc.getId(),
                paciente.getId(),
                hc.getNumeroHcl(),
                paciente.getNombresCompletos(),
                paciente.getCedula(),
                hc.getUnidadSalud(),
                hc.getEstado(),
                paciente.getEstadoArchivo(),
                hc.getFechaApertura());

        Long hcId = hc.getId();

        // 4. Antecedentes personales
        List<AntecedentePersonalResponse> antecedentesPersonales = antecedentePersonalRepository
                .findByHistoriaClinicaIdOrderByFechaRegistroDesc(hcId)
                .stream()
                .map(a -> new AntecedentePersonalResponse(
                        a.getId(), a.getTipo(), a.getDescripcion(),
                        a.getCodigoCie10(), a.getEstado(), a.getFechaRegistro()))
                .toList();

        // 5. Antecedentes familiares
        List<AntecedenteFamiliarResponse> antecedentesFamiliares = antecedenteFamiliarRepository
                .findByHistoriaClinicaIdOrderByFechaRegistroDesc(hcId)
                .stream()
                .map(a -> new AntecedenteFamiliarResponse(
                        a.getId(), a.getParentesco(), a.getCondicion(),
                        a.getCodigoCie10(), a.getFechaRegistro()))
                .toList();

        // 6. Episodios clinicos
        List<EpisodioClinico> episodios = episodioClinicoRepository
                .findByHistoriaClinicaIdOrderByFechaAperturaDesc(hcId);

        List<EpisodioClinicoResponse> episodiosDto = episodios.stream()
                .map(e -> new EpisodioClinicoResponse(
                        e.getId(), hcId, hc.getNumeroHcl(),
                        e.getNumeroSecuencial(), e.getNumeroEpisodio(),
                        e.getMotivoConsulta(),
                        e.getFechaApertura(), e.getFechaCierre(),
                        e.getEstado(), e.getEstadoCierre(), e.getObservacionCierre()))
                .toList();

        // 7. Problemas activos/cronicos (de TODOS los episodios, NO de antecedentes)
        List<Long> episodioIds = episodios.stream().map(EpisodioClinico::getId).toList();

        List<ProblemaEpisodioResponse> problemasActivos;
        if (episodioIds.isEmpty()) {
            problemasActivos = List.of();
        } else {
            problemasActivos = problemaEpisodioRepository
                    .findByEpisodioClinicoIdInAndEstadoIn(episodioIds,
                            List.of(EstadoProblemaEpisodio.ACTIVO, EstadoProblemaEpisodio.CRONICO))
                    .stream()
                    .map(p -> {
                        ProblemaEpisodioResponse r = new ProblemaEpisodioResponse();
                        r.setId(p.getId());
                        r.setNumeroSecuencial(p.getNumeroSecuencial());
                        r.setDescripcion(p.getDescripcion());
                        r.setCodigoCie10(p.getCodigoCie10());
                        r.setEstado(p.getEstado());
                        return r;
                    })
                    .toList();
        }

        // 8. Conteo de evaluaciones fisicas
        int totalEvaluaciones = 0;
        for (Long epId : episodioIds) {
            totalEvaluaciones += evaluacionFisicaRepository
                    .findByEpisodioClinicoIdOrderByFechaEvaluacionAsc(epId).size();
        }

        return new PacienteResumenCompletoResponse(
                pacienteDto,
                fichaFamiliar,
                historiaDto,
                antecedentesPersonales,
                antecedentesFamiliares,
                problemasActivos,
                episodiosDto,
                episodios.size(),
                totalEvaluaciones);
    }

        public PacienteResponse actualizarPaciente(Long id, UpdatePacienteRequest request, String actorEmail, String clientIp) {
        Paciente paciente = pacienteRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Paciente no encontrado: " + id));

        String cedula = normalize(request.cedula());
        String email = normalizeEmail(request.email());
        pacienteRepository.findByCedula(cedula).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
            throw new IllegalArgumentException("Ya existe otro paciente con esa cedula.");
            }
        });
        pacienteRepository.findByEmail(email).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new IllegalArgumentException("Ya existe otro paciente con ese correo electronico.");
            }
        });

        paciente.setCedula(cedula);
        paciente.setEmail(email);
        paciente.setNombresCompletos(normalize(request.nombresCompletos()));
        paciente.setFechaNacimiento(request.fechaNacimiento());
        paciente.setGenero(normalize(request.genero()));
        paciente.setGrupoCultural(normalizeNullable(request.grupoCultural()));
        paciente.setEstadoCivil(normalizeNullable(request.estadoCivil()));
        paciente.setOcupacion(normalizeNullable(request.ocupacion()));
        paciente.setRegimenSeguridadSocial(normalizeNullable(request.regimenSeguridadSocial()));
        paciente.setTipoSangre(normalizeNullable(request.tipoSangre()));
        paciente.setTelefonoPrincipal(normalize(request.telefonoPrincipal()));
        paciente.setTelefonoSecundario(normalizeNullable(request.telefonoSecundario()));
        paciente.setDireccion(normalizeNullable(request.direccion()));

        Paciente updated = pacienteRepository.save(paciente);

        Long actorId = usuarioRepository.findByEmail(actorEmail)
            .map(Usuario::getId)
            .orElse(0L);
        auditoriaService.registrar(actorId,
            "EDICION_PACIENTE",
            "Actualizacion de datos de paciente: " + updated.getNumeroHcl(),
            clientIp);

        return toResponse(updated);
        }

        public FichaFamiliarResponse guardarFichaFamiliar(Long pacienteId, FichaFamiliarRequest request, String actorEmail,
            String clientIp) {
        Paciente paciente = pacienteRepository.findById(pacienteId)
            .orElseThrow(() -> new IllegalArgumentException("Paciente no encontrado: " + pacienteId));

        FichaFamiliar ficha = fichaFamiliarRepository.findByPacienteId(pacienteId)
            .orElse(FichaFamiliar.builder().paciente(paciente).build());

        ficha.setJefeHogar(normalize(request.jefeHogar()));
        ficha.setNumeroMiembros(request.numeroMiembros());
        ficha.setTipoVivienda(normalize(request.tipoVivienda()));
        ficha.setCondicionesSanitarias(normalize(request.condicionesSanitarias()));

        FichaFamiliar saved = fichaFamiliarRepository.save(ficha);

        Long actorId = usuarioRepository.findByEmail(actorEmail)
            .map(Usuario::getId)
            .orElse(0L);
        auditoriaService.registrar(actorId,
            "FICHA_FAMILIAR_ACTUALIZADA",
            "Registro/actualizacion de ficha familiar para paciente: " + paciente.getNumeroHcl(),
            clientIp);

        return toFichaResponse(saved);
        }

        public FichaFamiliarResponse obtenerFichaFamiliar(Long pacienteId) {
        FichaFamiliar ficha = fichaFamiliarRepository.findByPacienteId(pacienteId)
            .orElseThrow(() -> new IllegalArgumentException("Ficha familiar no encontrada para paciente: " + pacienteId));
        return toFichaResponse(ficha);
        }

    public int actualizarArchivosPasivosAutomaticamente() {
        LocalDateTime now = LocalDateTime.now(ZoneId.of(appTimezone));
        LocalDateTime limite = now.minusYears(5);

        List<Paciente> candidatos = pacienteRepository.findByEstadoArchivoAndFechaUltimaAtencionBefore(
                EstadoArchivoPaciente.ACTIVO,
                limite);

        if (candidatos.isEmpty()) {
            return 0;
        }

        for (Paciente paciente : candidatos) {
            paciente.setEstadoArchivo(EstadoArchivoPaciente.PASIVO);
        }
        pacienteRepository.saveAll(candidatos);

        auditoriaService.registrar(
                0L,
                "ARCHIVO_PASIVO_AUTOMATICO",
                "Pacientes marcados como PASIVO por inactividad >= 5 anos: " + candidatos.size(),
                "sistema");

        return candidatos.size();
    }

    private String generarSiguienteNumeroHcl() {
        int anio = Year.now(ZoneId.of(appTimezone)).getValue();
        HclSecuencia secuencia = hclSecuenciaRepository.findByAnioForUpdate(anio)
                .orElse(HclSecuencia.builder().anio(anio).ultimoNumero(0).build());

        secuencia.setUltimoNumero(secuencia.getUltimoNumero() + 1);
        HclSecuencia saved = hclSecuenciaRepository.save(secuencia);

        return String.format("HC-%d-%05d", anio, saved.getUltimoNumero());
    }

    private PacienteResponse toResponse(Paciente paciente) {
        return new PacienteResponse(
                paciente.getId(),
                paciente.getNumeroHcl(),
                paciente.getCedula(),
                paciente.getEmail(),
                paciente.getNombresCompletos(),
                paciente.getFechaNacimiento(),
                paciente.getGenero(),
                paciente.getGrupoCultural(),
                paciente.getEstadoCivil(),
                paciente.getOcupacion(),
                paciente.getRegimenSeguridadSocial(),
                paciente.getTipoSangre(),
                paciente.getTelefonoPrincipal(),
                paciente.getTelefonoSecundario(),
                paciente.getDireccion(),
                paciente.getEstadoArchivo(),
                paciente.getFechaRegistro(),
                paciente.getFechaUltimaAtencion());
    }

    private FichaFamiliarResponse toFichaResponse(FichaFamiliar ficha) {
        return new FichaFamiliarResponse(
                ficha.getId(),
                ficha.getPaciente().getId(),
                ficha.getJefeHogar(),
                ficha.getNumeroMiembros(),
                ficha.getTipoVivienda(),
                ficha.getCondicionesSanitarias(),
                ficha.getFechaActualizacion());
    }

    private String normalize(String value) {
        return value == null ? null : value.trim();
    }

    private String normalizeNullable(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String normalizeEmail(String value) {
        if (value == null) {
            return null;
        }
        return value.trim().toLowerCase();
    }
}
