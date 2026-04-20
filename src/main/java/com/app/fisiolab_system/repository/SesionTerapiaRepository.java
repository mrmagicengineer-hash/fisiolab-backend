package com.app.fisiolab_system.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.app.fisiolab_system.model.EstadoSesionTerapia;
import com.app.fisiolab_system.model.SesionTerapia;

public interface SesionTerapiaRepository extends JpaRepository<SesionTerapia, Long> {

    Optional<SesionTerapia> findByCitaId(Long citaId);

    boolean existsByCitaId(Long citaId);

    List<SesionTerapia> findByEpisodioClinicoIdOrderByFechaHoraInicioAsc(Long episodioClinicoId);

    List<SesionTerapia> findByPacienteIdOrderByFechaHoraInicioDesc(Long pacienteId);

    List<SesionTerapia> findByPlanTratamientoIdOrderByNumeroSesionEnPlanAsc(Long planTratamientoId);

    long countByPlanTratamientoIdAndEstado(Long planTratamientoId, EstadoSesionTerapia estado);

    @Query("select coalesce(max(s.numeroSesionEnPlan), 0) from SesionTerapia s where s.planTratamiento.id = :planId")
    int findMaxNumeroSesionByPlanId(Long planId);
}
