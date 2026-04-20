package com.app.fisiolab_system.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.app.fisiolab_system.dto.ComparativaEscalaResponse;
import com.app.fisiolab_system.dto.ComparativaEvaluacionResponse;
import com.app.fisiolab_system.dto.CreateEvaluacionFisicaRequest;
import com.app.fisiolab_system.dto.EscalaFuncionalCalculadaResponse;
import com.app.fisiolab_system.dto.EscalaFuncionalItemRequest;
import com.app.fisiolab_system.dto.EvaluacionFisicaResponse;
import com.app.fisiolab_system.dto.FuerzaMuscularItemRequest;
import com.app.fisiolab_system.dto.GoniometriaItemRequest;
import com.app.fisiolab_system.dto.PruebaEspecialItemRequest;
import com.app.fisiolab_system.dto.PuntoProgresoEvaluacionResponse;
import com.app.fisiolab_system.model.EpisodioClinico;
import com.app.fisiolab_system.model.EstadoEpisodioClinico;
import com.app.fisiolab_system.model.EvaluacionFisicaEpisodio;
import com.app.fisiolab_system.model.TipoEvaluacionFisioterapeutica;
import com.app.fisiolab_system.model.Usuario;
import com.app.fisiolab_system.repository.EpisodioClinicoRepository;
import com.app.fisiolab_system.repository.EvaluacionFisicaEpisodioRepository;
import com.app.fisiolab_system.repository.UsuarioRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class EvaluacionFisicaService {

    private static final TypeReference<List<GoniometriaItemRequest>> GONIOMETRIA_TYPE = new TypeReference<>() {
    };
    private static final TypeReference<List<FuerzaMuscularItemRequest>> FUERZA_TYPE = new TypeReference<>() {
    };
    private static final TypeReference<List<EscalaFuncionalCalculadaResponse>> ESCALAS_TYPE = new TypeReference<>() {
    };
    private static final TypeReference<List<PruebaEspecialItemRequest>> PRUEBAS_TYPE = new TypeReference<>() {
    };

    private final EvaluacionFisicaEpisodioRepository evaluacionRepository;
    private final EpisodioClinicoRepository episodioRepository;
    private final UsuarioRepository usuarioRepository;
    private final AuditoriaService auditoriaService;
    private final ObjectMapper objectMapper;

    public EvaluacionFisicaService(
            EvaluacionFisicaEpisodioRepository evaluacionRepository,
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

    public EvaluacionFisicaResponse registrarEvaluacion(
            Long episodioId,
            CreateEvaluacionFisicaRequest request,
            String actorEmail,
            String clientIp) {
        EpisodioClinico episodio = getEpisodioAbierto(episodioId);

        validarTipoEvaluacion(episodioId, request.tipoEvaluacion());

        BigDecimal imc = calcularImc(request.pesoKg(), request.tallaCm());
        List<EscalaFuncionalCalculadaResponse> escalasCalculadas = calcularEscalas(request.escalasFuncionales());
        BigDecimal promedioFuncional = calcularPromedioFuncional(escalasCalculadas);

        Integer ultimoNumero = evaluacionRepository.findMaxNumeroEvaluacionByEpisodioId(episodioId);
        int numeroEvaluacion = (ultimoNumero == null ? 0 : ultimoNumero) + 1;

        EvaluacionFisicaEpisodio entity = EvaluacionFisicaEpisodio.builder()
                .episodioClinico(episodio)
                .tipoEvaluacion(request.tipoEvaluacion())
                .fechaEvaluacion(request.fechaEvaluacion())
                .frecuenciaCardiaca(normalizeNullable(request.frecuenciaCardiaca()))
                .frecuenciaRespiratoria(normalizeNullable(request.frecuenciaRespiratoria()))
                .presionArterial(normalizeNullable(request.presionArterial()))
                .saturacionOxigeno(normalizeNullable(request.saturacionOxigeno()))
                .tallaCm(request.tallaCm())
                .pesoKg(request.pesoKg())
                .imc(imc)
                .eva(request.eva())
                .localizacionDolor(normalize(request.localizacionDolor()))
                .tipoDolor(normalize(request.tipoDolor()))
                .examenFisicoSegmentario(normalize(request.examenFisicoSegmentario()))
                .diagnosticosPresuntivos(normalize(request.diagnosticosPresuntivos()))
                .goniometriaJson(toJson(request.goniometria()))
                .fuerzaMuscularJson(toJson(request.fuerzaMuscular()))
                .escalasFuncionalesJson(toJson(escalasCalculadas))
                .pruebasEspecialesJson(toJson(request.pruebasEspeciales()))
                .puntajeFuncionalPromedio(promedioFuncional)
                .interpretacionFuncional(interpretarPromedioFuncional(promedioFuncional))
                .numeroEvaluacion(numeroEvaluacion)
                .build();

        EvaluacionFisicaEpisodio saved = evaluacionRepository.save(entity);

        auditar(actorEmail,
                "REGISTRO_EVALUACION_FISICA",
                "Registro de evaluacion " + saved.getTipoEvaluacion() + " para episodio " + episodio.getNumeroEpisodio(),
                clientIp);

        return toResponse(saved);
    }

    public List<EvaluacionFisicaResponse> listarPorEpisodio(Long episodioId) {
        asegurarEpisodioExiste(episodioId);
        return evaluacionRepository.findByEpisodioClinicoIdOrderByFechaEvaluacionAsc(episodioId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public EvaluacionFisicaResponse obtenerPorId(Long episodioId, Long evaluacionId) {
        return toResponse(getEvaluacionOrThrow(episodioId, evaluacionId));
    }

    public ComparativaEvaluacionResponse compararConInicial(Long episodioId, Long evaluacionId) {
        EvaluacionFisicaEpisodio evaluacionInicial = evaluacionRepository
                .findFirstByEpisodioClinicoIdAndTipoEvaluacionOrderByFechaEvaluacionAsc(
                        episodioId,
                        TipoEvaluacionFisioterapeutica.INICIAL)
                .orElseThrow(() -> new IllegalArgumentException("No existe evaluacion inicial para este episodio."));

        EvaluacionFisicaEpisodio evaluacionComparada = getEvaluacionOrThrow(episodioId, evaluacionId);

        List<EscalaFuncionalCalculadaResponse> escalasInicial = readEscalas(evaluacionInicial.getEscalasFuncionalesJson());
        List<EscalaFuncionalCalculadaResponse> escalasComparada = readEscalas(evaluacionComparada.getEscalasFuncionalesJson());

        Map<String, EscalaFuncionalCalculadaResponse> mapaInicial = escalasInicial.stream()
                .collect(Collectors.toMap(e -> e.nombreEscala().toUpperCase(), Function.identity(), (a, b) -> a));

        List<ComparativaEscalaResponse> deltasEscalas = new ArrayList<>();
        for (EscalaFuncionalCalculadaResponse escalaComparada : escalasComparada) {
            EscalaFuncionalCalculadaResponse escalaInicial = mapaInicial.get(escalaComparada.nombreEscala().toUpperCase());
            if (escalaInicial == null) {
                continue;
            }
            BigDecimal delta = escalaComparada.porcentaje().subtract(escalaInicial.porcentaje())
                    .setScale(2, RoundingMode.HALF_UP);
            deltasEscalas.add(new ComparativaEscalaResponse(
                    escalaComparada.nombreEscala(),
                    escalaInicial.porcentaje(),
                    escalaComparada.porcentaje(),
                    delta));
        }

        Integer deltaEva = evaluacionComparada.getEva() - evaluacionInicial.getEva();
        BigDecimal deltaPromedio = safe(evaluacionComparada.getPuntajeFuncionalPromedio())
                .subtract(safe(evaluacionInicial.getPuntajeFuncionalPromedio()))
                .setScale(2, RoundingMode.HALF_UP);

        return new ComparativaEvaluacionResponse(
                evaluacionInicial.getId(),
                evaluacionComparada.getId(),
                deltaEva,
                deltaPromedio,
                deltasEscalas.stream()
                        .sorted(Comparator.comparing(ComparativaEscalaResponse::nombreEscala))
                        .toList());
    }

    public List<PuntoProgresoEvaluacionResponse> obtenerProgreso(Long episodioId) {
        asegurarEpisodioExiste(episodioId);
        return evaluacionRepository.findByEpisodioClinicoIdOrderByFechaEvaluacionAsc(episodioId)
                .stream()
                .map(e -> new PuntoProgresoEvaluacionResponse(
                        e.getId(),
                        e.getNumeroEvaluacion(),
                        e.getTipoEvaluacion(),
                        e.getFechaEvaluacion(),
                        e.getEva(),
                        e.getPuntajeFuncionalPromedio()))
                .toList();
    }

    private void validarTipoEvaluacion(Long episodioId, TipoEvaluacionFisioterapeutica tipo) {
        if (tipo == TipoEvaluacionFisioterapeutica.INICIAL
                && evaluacionRepository.existsByEpisodioClinicoIdAndTipoEvaluacion(episodioId, tipo)) {
            throw new IllegalArgumentException("El episodio ya tiene una evaluacion inicial registrada.");
        }

        if (tipo == TipoEvaluacionFisioterapeutica.ALTA
                && evaluacionRepository.existsByEpisodioClinicoIdAndTipoEvaluacion(episodioId, tipo)) {
            throw new IllegalArgumentException("El episodio ya tiene una evaluacion de alta registrada.");
        }

        if (tipo != TipoEvaluacionFisioterapeutica.INICIAL) {
            boolean existeInicial = evaluacionRepository.existsByEpisodioClinicoIdAndTipoEvaluacion(
                    episodioId,
                    TipoEvaluacionFisioterapeutica.INICIAL);
            if (!existeInicial) {
                throw new IllegalArgumentException("Debe registrarse primero la evaluacion inicial del episodio.");
            }
        }
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

    private EvaluacionFisicaEpisodio getEvaluacionOrThrow(Long  episodioId, Long evaluacionId) {
        return evaluacionRepository.findByIdAndEpisodioClinicoId(evaluacionId, episodioId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Evaluacion no encontrada: " + evaluacionId + " para episodio: " + episodioId));
    }

    private EvaluacionFisicaResponse toResponse(EvaluacionFisicaEpisodio entity) {
        return new EvaluacionFisicaResponse(
                entity.getId(),
                entity.getEpisodioClinico().getId(),
                entity.getNumeroEvaluacion(),
                entity.getTipoEvaluacion(),
                entity.getFechaEvaluacion(),
                entity.getFrecuenciaCardiaca(),
                entity.getFrecuenciaRespiratoria(),
                entity.getPresionArterial(),
                entity.getSaturacionOxigeno(),
                entity.getTallaCm(),
                entity.getPesoKg(),
                entity.getImc(),
                entity.getEva(),
                entity.getLocalizacionDolor(),
                entity.getTipoDolor(),
                entity.getExamenFisicoSegmentario(),
                entity.getDiagnosticosPresuntivos(),
                fromJson(entity.getGoniometriaJson(), GONIOMETRIA_TYPE),
                fromJson(entity.getFuerzaMuscularJson(), FUERZA_TYPE),
                fromJson(entity.getEscalasFuncionalesJson(), ESCALAS_TYPE),
                fromJson(entity.getPruebasEspecialesJson(), PRUEBAS_TYPE),
                entity.getPuntajeFuncionalPromedio(),
                entity.getInterpretacionFuncional());
    }

    private BigDecimal calcularImc(BigDecimal pesoKg, BigDecimal tallaCm) {
        BigDecimal tallaMetros = tallaCm.divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP);
        BigDecimal tallaCuadrado = tallaMetros.multiply(tallaMetros);
        return pesoKg.divide(tallaCuadrado, 2, RoundingMode.HALF_UP);
    }

    private List<EscalaFuncionalCalculadaResponse> calcularEscalas(List<EscalaFuncionalItemRequest> escalas) {
        return escalas.stream().map(item -> {
            BigDecimal porcentaje = item.puntajeObtenido()
                    .multiply(BigDecimal.valueOf(100))
                    .divide(item.puntajeMaximo(), 2, RoundingMode.HALF_UP);
            return new EscalaFuncionalCalculadaResponse(
                    normalize(item.nombreEscala()),
                    item.puntajeObtenido().setScale(2, RoundingMode.HALF_UP),
                    item.puntajeMaximo().setScale(2, RoundingMode.HALF_UP),
                    porcentaje,
                    interpretarPorcentajeEscala(porcentaje));
        }).toList();
    }

    private BigDecimal calcularPromedioFuncional(List<EscalaFuncionalCalculadaResponse> escalas) {
        BigDecimal suma = escalas.stream()
                .map(EscalaFuncionalCalculadaResponse::porcentaje)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return suma.divide(BigDecimal.valueOf(escalas.size()), 2, RoundingMode.HALF_UP);
    }

    private String interpretarPromedioFuncional(BigDecimal promedio) {
        if (promedio.compareTo(BigDecimal.valueOf(20)) <= 0) {
            return "Compromiso funcional leve";
        }
        if (promedio.compareTo(BigDecimal.valueOf(40)) <= 0) {
            return "Compromiso funcional moderado";
        }
        if (promedio.compareTo(BigDecimal.valueOf(60)) <= 0) {
            return "Compromiso funcional severo";
        }
        if (promedio.compareTo(BigDecimal.valueOf(80)) <= 0) {
            return "Compromiso funcional muy severo";
        }
        return "Compromiso funcional extremo";
    }

    private String interpretarPorcentajeEscala(BigDecimal porcentaje) {
        if (porcentaje.compareTo(BigDecimal.valueOf(20)) <= 0) {
            return "Leve";
        }
        if (porcentaje.compareTo(BigDecimal.valueOf(40)) <= 0) {
            return "Moderada";
        }
        if (porcentaje.compareTo(BigDecimal.valueOf(60)) <= 0) {
            return "Severa";
        }
        if (porcentaje.compareTo(BigDecimal.valueOf(80)) <= 0) {
            return "Muy severa";
        }
        return "Extrema";
    }

    private void auditar(String actorEmail, String accion, String detalle, String ip) {
        Long actorId = usuarioRepository.findByEmail(actorEmail)
                .map(Usuario::getId)
                .orElse(0L);
        auditoriaService.registrar(actorId, accion, detalle, ip);
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception ex) {
            throw new IllegalStateException("No fue posible serializar datos de evaluacion.", ex);
        }
    }

    private <T> List<T> fromJson(String json, TypeReference<List<T>> typeReference) {
        try {
            return objectMapper.readValue(json, typeReference);
        } catch (Exception ex) {
            throw new IllegalStateException("No fue posible deserializar datos de evaluacion.", ex);
        }
    }

    private List<EscalaFuncionalCalculadaResponse> readEscalas(String json) {
        return fromJson(json, ESCALAS_TYPE);
    }

    private BigDecimal safe(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
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
}
