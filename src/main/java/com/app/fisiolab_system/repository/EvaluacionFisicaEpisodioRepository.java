package com.app.fisiolab_system.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.app.fisiolab_system.model.EvaluacionFisicaEpisodio;
import com.app.fisiolab_system.model.TipoEvaluacionFisioterapeutica;

public interface EvaluacionFisicaEpisodioRepository extends JpaRepository<EvaluacionFisicaEpisodio, String> {

    List<EvaluacionFisicaEpisodio> findByEpisodioClinicoIdOrderByFechaEvaluacionAsc(Long episodioId);

    Optional<EvaluacionFisicaEpisodio> findByIdAndEpisodioClinicoId(Long evaluacionId, Long episodioId);

    Optional<EvaluacionFisicaEpisodio> findFirstByEpisodioClinicoIdAndTipoEvaluacionOrderByFechaEvaluacionAsc(
            Long episodioId,
            TipoEvaluacionFisioterapeutica tipoEvaluacion);

    boolean existsByEpisodioClinicoIdAndTipoEvaluacion(Long episodioId, TipoEvaluacionFisioterapeutica tipoEvaluacion);

    @Query("select coalesce(max(e.numeroEvaluacion), 0) from EvaluacionFisicaEpisodio e where e.episodioClinico.id = :episodioId")
    Integer findMaxNumeroEvaluacionByEpisodioId(Long episodioId);
}
