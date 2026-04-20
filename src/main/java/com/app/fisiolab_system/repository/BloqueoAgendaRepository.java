package com.app.fisiolab_system.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.app.fisiolab_system.model.BloqueoAgenda;

public interface BloqueoAgendaRepository extends JpaRepository<BloqueoAgenda, Long> {

    List<BloqueoAgenda> findByProfesionalIdOrderByFechaHoraInicioAsc(Long profesionalId);

    List<BloqueoAgenda> findAllByOrderByFechaHoraInicioAsc();

    // Bloqueos de un profesional en un rango (para vista FullCalendar)
    @Query("""
            SELECT b FROM BloqueoAgenda b
            WHERE b.profesional.id = :profesionalId
              AND b.fechaHoraInicio < :hasta
              AND b.fechaHoraFin > :desde
            ORDER BY b.fechaHoraInicio ASC
            """)
    List<BloqueoAgenda> findByProfesionalAndRango(
            @Param("profesionalId") Long profesionalId,
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta);

    // Todos los bloqueos en un rango (uso ADMINISTRADOR)
    @Query("""
            SELECT b FROM BloqueoAgenda b
            WHERE b.fechaHoraInicio < :hasta
              AND b.fechaHoraFin > :desde
            ORDER BY b.fechaHoraInicio ASC
            """)
    List<BloqueoAgenda> findAllByRango(
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta);

    /**
     * Detecta si un nuevo rango cae dentro de algún bloqueo activo del profesional.
     */
    @Query("""
            SELECT COUNT(b) FROM BloqueoAgenda b
            WHERE b.profesional.id = :profesionalId
              AND b.fechaHoraInicio < :fin
              AND b.fechaHoraFin > :inicio
            """)
    long contarBloqueosSolapados(
            @Param("profesionalId") Long profesionalId,
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin);
}
