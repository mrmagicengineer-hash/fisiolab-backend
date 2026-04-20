package com.app.fisiolab_system.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.app.fisiolab_system.model.SeguimientoPlan;

public interface SeguimientoPlanRepository extends JpaRepository<SeguimientoPlan, Long> {

    List<SeguimientoPlan> findByPlanTratamientoIdOrderByNumeroSesionAsc(Long planId);

    long countByPlanTratamientoId(Long planId);

    @Query("SELECT COALESCE(MAX(s.numeroSesion), 0) FROM SeguimientoPlan s WHERE s.planTratamiento.id = :planId")
    Integer findMaxNumeroSesionByPlanId(@Param("planId") Long planId);
}
