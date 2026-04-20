package com.app.fisiolab_system.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.app.fisiolab_system.model.PlanTratamiento;

public interface PlanTratamientoRepository extends JpaRepository<PlanTratamiento, Long> {

    List<PlanTratamiento> findByEpisodioClinicoIdOrderByFechaCreacionDesc(Long episodioId);

    Optional<PlanTratamiento> findByProblemaEpisodioId(Long problemaId);

    Optional<PlanTratamiento> findByIdAndEpisodioClinicoId(Long id, Long episodioId);

    boolean existsByProblemaEpisodioId(Long problemaId);

    @Query(
        value = "SELECT p FROM PlanTratamiento p JOIN FETCH p.episodioClinico e JOIN FETCH e.historiaClinica h JOIN FETCH h.paciente pac JOIN FETCH p.problemaEpisodio pe ORDER BY p.fechaCreacion DESC",
        countQuery = "SELECT count(p) FROM PlanTratamiento p"
    )
    Page<PlanTratamiento> findAllWithDetails(Pageable pageable);

    @Query("SELECT p FROM PlanTratamiento p JOIN FETCH p.episodioClinico e JOIN FETCH e.historiaClinica h JOIN FETCH h.paciente pac WHERE p.estado = com.app.fisiolab_system.model.EstadoPlan.ACTIVO ORDER BY pac.nombresCompletos ASC")
    List<PlanTratamiento> findAllActivoWithPatientDetails();

    @Query("SELECT COUNT(p) FROM PlanTratamiento p WHERE p.estado = com.app.fisiolab_system.model.EstadoPlan.ACTIVO")
    long countActivos();

    @Query("SELECT COUNT(p) FROM PlanTratamiento p WHERE p.estado = com.app.fisiolab_system.model.EstadoPlan.ACTIVO AND p.codigoAlarma IN :alarmas")
    long countActivosByAlarmas(java.util.List<com.app.fisiolab_system.model.CodigoAlarma> alarmas);

    @Query("SELECT COUNT(p) FROM PlanTratamiento p WHERE p.estado = com.app.fisiolab_system.model.EstadoPlan.ACTIVO AND p.sesionesPlanificadas > 0 AND (p.sesionesRealizadas * 1.0 / p.sesionesPlanificadas) >= :umbral")
    long countActivosFinalizando(double umbral);
}

