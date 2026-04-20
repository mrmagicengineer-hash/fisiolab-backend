package com.app.fisiolab_system.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.app.fisiolab_system.model.EvaluacionClinica;

public interface EvaluacionClinicaRepository extends JpaRepository<EvaluacionClinica, Long> {

    List<EvaluacionClinica> findByEpisodioClinicoIdOrderByFechaEvaluacionAsc(Long episodioId);

    Optional<EvaluacionClinica> findByIdAndEpisodioClinicoId(Long id, Long episodioId);

    boolean existsByIdAndEpisodioClinicoId(Long id, Long episodioId);
}
