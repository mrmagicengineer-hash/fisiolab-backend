package com.app.fisiolab_system.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.app.fisiolab_system.model.EpisodioClinico;
import com.app.fisiolab_system.model.EstadoEpisodioClinico;

public interface EpisodioClinicoRepository extends JpaRepository<EpisodioClinico, Long> {

    List<EpisodioClinico> findByHistoriaClinicaIdOrderByFechaAperturaDesc(Long historiaClinicaId);

    Optional<EpisodioClinico> findByHistoriaClinicaNumeroHclAndNumeroSecuencial(String numeroHcl, Integer numeroSecuencial);

    List<EpisodioClinico> findByHistoriaClinicaIdAndEstadoInOrderByFechaAperturaDesc(Long historiaClinicaId, List<EstadoEpisodioClinico> estados);

    @Query("select coalesce(max(e.numeroSecuencial), 0) from EpisodioClinico e where e.historiaClinica.id = :historiaClinicaId")
    Integer findMaxSecuencialByHistoriaClinicaId(Long historiaClinicaId);
}
