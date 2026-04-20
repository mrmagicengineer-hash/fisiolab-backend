package com.app.fisiolab_system.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.app.fisiolab_system.model.EstadoProblemaEpisodio;
import com.app.fisiolab_system.model.ProblemaEpisodio;

public interface ProblemaEpisodioRepository extends JpaRepository<ProblemaEpisodio, Long> {
    List<ProblemaEpisodio> findByEpisodioClinicoIdOrderByNumeroSecuencialAsc(Long episodioClinicoId);
    int countByEpisodioClinicoId(Long episodioClinicoId);
    List<ProblemaEpisodio> findByEpisodioClinicoIdInAndEstadoIn(List<Long> episodioIds, List<EstadoProblemaEpisodio> estados);
}
