package com.app.fisiolab_system.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.springframework.stereotype.Service;

import com.app.fisiolab_system.dto.BiometriaResponse;
import com.app.fisiolab_system.dto.RegistrarBiometriaRequest;
import com.app.fisiolab_system.model.BiometriaPaciente;
import com.app.fisiolab_system.model.EpisodioClinico;
import com.app.fisiolab_system.model.EstadoEpisodioClinico;
import com.app.fisiolab_system.model.Usuario;
import com.app.fisiolab_system.repository.BiometriaPacienteRepository;
import com.app.fisiolab_system.repository.EpisodioClinicoRepository;
import com.app.fisiolab_system.repository.UsuarioRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class BiometriaService {

    private final BiometriaPacienteRepository biometriaRepository;
    private final EpisodioClinicoRepository episodioRepository;
    private final UsuarioRepository usuarioRepository;
    private final AuditoriaService auditoriaService;

    public BiometriaService(
            BiometriaPacienteRepository biometriaRepository,
            EpisodioClinicoRepository episodioRepository,
            UsuarioRepository usuarioRepository,
            AuditoriaService auditoriaService) {
        this.biometriaRepository = biometriaRepository;
        this.episodioRepository = episodioRepository;
        this.usuarioRepository = usuarioRepository;
        this.auditoriaService = auditoriaService;
    }

    public BiometriaResponse registrar(
            Long episodioId,
            RegistrarBiometriaRequest request,
            String actorEmail,
            String clientIp) {

        EpisodioClinico episodio = episodioRepository.findById(episodioId)
                .orElseThrow(() -> new IllegalArgumentException("Episodio no encontrado: " + episodioId));

        if (episodio.getEstado() == EstadoEpisodioClinico.CERRADO) {
            throw new IllegalArgumentException("No se puede registrar biometria en un episodio cerrado.");
        }

        BigDecimal imc = calcularImc(request.pesoKg(), request.tallaCm());

        BiometriaPaciente entity = BiometriaPaciente.builder()
                .episodioClinico(episodio)
                .fechaRegistro(request.fechaRegistro())
                .pesoKg(request.pesoKg())
                .tallaCm(request.tallaCm())
                .imcCalculado(imc)
                .perimetroCinturaCm(request.perimetroCinturaCm())
                .perimetroCaderaCm(request.perimetroCaderaCm())
                .presionArterialSistolica(request.presionArterialSistolica())
                .presionArterialDiastolica(request.presionArterialDiastolica())
                .frecuenciaCardiaca(request.frecuenciaCardiaca())
                .frecuenciaRespiratoria(request.frecuenciaRespiratoria())
                .temperatura(request.temperatura())
                .saturacionOxigeno(request.saturacionOxigeno())
                .observaciones(request.observaciones() != null ? request.observaciones().trim() : null)
                .registradoPor(actorEmail)
                .build();

        BiometriaPaciente saved = biometriaRepository.save(entity);

        auditar(actorEmail,
                "REGISTRO_BIOMETRIA",
                "Registro de biometria para episodio " + episodio.getNumeroEpisodio()
                        + " — IMC: " + imc,
                clientIp);

        return toResponse(saved);
    }

    public List<BiometriaResponse> listarPorEpisodio(Long episodioId) {
        if (!episodioRepository.existsById(episodioId)) {
            throw new IllegalArgumentException("Episodio no encontrado: " + episodioId);
        }
        return biometriaRepository.findByEpisodioClinicoIdOrderByFechaRegistroAsc(episodioId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public BiometriaResponse obtenerPorId(Long episodioId, Long biometriaId) {
        BiometriaPaciente entity = biometriaRepository.findByIdAndEpisodioClinicoId(biometriaId, episodioId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Biometria no encontrada: " + biometriaId + " para episodio: " + episodioId));
        return toResponse(entity);
    }

    private BigDecimal calcularImc(BigDecimal pesoKg, BigDecimal tallaCm) {
        BigDecimal tallaMetros = tallaCm.divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP);
        BigDecimal tallaCuadrado = tallaMetros.multiply(tallaMetros);
        return pesoKg.divide(tallaCuadrado, 2, RoundingMode.HALF_UP);
    }

    private String clasificarImc(BigDecimal imc) {
        if (imc.compareTo(BigDecimal.valueOf(18.5)) < 0) {
            return "Bajo peso";
        }
        if (imc.compareTo(BigDecimal.valueOf(25.0)) < 0) {
            return "Peso normal";
        }
        if (imc.compareTo(BigDecimal.valueOf(30.0)) < 0) {
            return "Sobrepeso";
        }
        if (imc.compareTo(BigDecimal.valueOf(35.0)) < 0) {
            return "Obesidad grado I";
        }
        if (imc.compareTo(BigDecimal.valueOf(40.0)) < 0) {
            return "Obesidad grado II";
        }
        return "Obesidad grado III";
    }

    private String clasificarPresionArterial(Integer sistolica, Integer diastolica) {
        if (sistolica == null || diastolica == null) {
            return null;
        }
        if (sistolica < 120 && diastolica < 80) {
            return "Normal";
        }
        if (sistolica < 130 && diastolica < 80) {
            return "Elevada";
        }
        if (sistolica < 140 || diastolica < 90) {
            return "Hipertension grado 1";
        }
        if (sistolica < 180 || diastolica < 120) {
            return "Hipertension grado 2";
        }
        return "Crisis hipertensiva";
    }

    private BiometriaResponse toResponse(BiometriaPaciente entity) {
        return new BiometriaResponse(
                entity.getId(),
                entity.getEpisodioClinico().getId(),
                entity.getFechaRegistro(),
                entity.getPesoKg(),
                entity.getTallaCm(),
                entity.getImcCalculado(),
                clasificarImc(entity.getImcCalculado()),
                entity.getPerimetroCinturaCm(),
                entity.getPerimetroCaderaCm(),
                entity.getPresionArterialSistolica(),
                entity.getPresionArterialDiastolica(),
                clasificarPresionArterial(entity.getPresionArterialSistolica(), entity.getPresionArterialDiastolica()),
                entity.getFrecuenciaCardiaca(),
                entity.getFrecuenciaRespiratoria(),
                entity.getTemperatura(),
                entity.getSaturacionOxigeno(),
                entity.getObservaciones(),
                entity.getRegistradoPor());
    }

    private void auditar(String actorEmail, String accion, String detalle, String ip) {
        Long actorId = usuarioRepository.findByEmail(actorEmail)
                .map(Usuario::getId)
                .orElse(0L);
        auditoriaService.registrar(actorId, accion, detalle, ip);
    }
}
