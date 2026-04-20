package com.app.fisiolab_system.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.fisiolab_system.dto.CreateProblemaEpisodioRequest;
import com.app.fisiolab_system.dto.ProblemaEpisodioResponse;
import com.app.fisiolab_system.model.EpisodioClinico;
import com.app.fisiolab_system.model.ProblemaEpisodio;
import com.app.fisiolab_system.repository.EpisodioClinicoRepository;
import com.app.fisiolab_system.repository.ProblemaEpisodioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProblemaEpisodioService {
    private final ProblemaEpisodioRepository problemaEpisodioRepository;
    private final EpisodioClinicoRepository episodioClinicoRepository;

    @Transactional
    public ProblemaEpisodioResponse registrarProblema(Long episodioId, CreateProblemaEpisodioRequest request) {
        EpisodioClinico episodio = episodioClinicoRepository.findById(episodioId)
            .orElseThrow(() -> new IllegalArgumentException("Episodio clínico no encontrado: " + episodioId));
        int secuencial = problemaEpisodioRepository.countByEpisodioClinicoId(episodioId) + 1;
        ProblemaEpisodio problema = ProblemaEpisodio.builder()
            .episodioClinico(episodio)
            .numeroSecuencial(secuencial)
            .descripcion(request.getDescripcion())
            .codigoCie10(request.getCodigoCie10())
            .estado(request.getEstado())
            .build();
        problema = problemaEpisodioRepository.save(problema);
        return toResponse(problema);
    }

    @Transactional(readOnly = true)
    public List<ProblemaEpisodioResponse> listarPorEpisodio(Long episodioId) {
        return problemaEpisodioRepository.findByEpisodioClinicoIdOrderByNumeroSecuencialAsc(episodioId)
            .stream().map(this::toResponse).collect(Collectors.toList());
    }

    private ProblemaEpisodioResponse toResponse(ProblemaEpisodio p) {
        ProblemaEpisodioResponse r = new ProblemaEpisodioResponse();
        r.setId(p.getId());
        r.setNumeroSecuencial(p.getNumeroSecuencial());
        r.setDescripcion(p.getDescripcion());
        r.setCodigoCie10(p.getCodigoCie10());
        r.setEstado(p.getEstado());
        return r;
    }
}
