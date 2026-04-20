package com.app.fisiolab_system.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.app.fisiolab_system.model.Cita;
import com.app.fisiolab_system.model.EstadoCita;

public interface CitaRepository extends JpaRepository<Cita, Long> {

    List<Cita> findByProfesionalIdOrderByFechaHoraInicioAsc(Long profesionalId);

    List<Cita> findByPacienteIdOrderByFechaHoraInicioDesc(Long pacienteId);

    List<Cita> findAllByOrderByFechaHoraInicioAsc();

    // Citas de un profesional en un rango de fechas (para vista de agenda/FullCalendar)
    @Query("""
            SELECT c FROM Cita c
            WHERE c.profesional.id = :profesionalId
              AND c.fechaHoraInicio >= :desde
              AND c.fechaHoraInicio < :hasta
            ORDER BY c.fechaHoraInicio ASC
            """)
    List<Cita> findByProfesionalAndRango(
            @Param("profesionalId") Long profesionalId,
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta);

    // Todas las citas en un rango (uso exclusivo ADMINISTRADOR)
    @Query("""
            SELECT c FROM Cita c
            WHERE c.fechaHoraInicio >= :desde
              AND c.fechaHoraInicio < :hasta
            ORDER BY c.fechaHoraInicio ASC
            """)
    List<Cita> findAllByRango(
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta);

    /**
     * Detección de solapamiento para un profesional.
     * Excluye estados CANCELADA y NO_ASISTIDA.
     * excludeId permite reutilizar la query en actualizaciones (pasar null si es nueva cita).
     */
    @Query("""
            SELECT COUNT(c) FROM Cita c
            WHERE c.profesional.id = :profesionalId
              AND c.estado NOT IN (
                  com.app.fisiolab_system.model.EstadoCita.CANCELADA,
                  com.app.fisiolab_system.model.EstadoCita.NO_ASISTIDA
              )
              AND c.fechaHoraInicio < :fin
              AND c.fechaHoraFin > :inicio
              AND (:excludeId IS NULL OR c.id != :excludeId)
            """)
    long contarSolapamientos(
            @Param("profesionalId") Long profesionalId,
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin,
            @Param("excludeId") Long excludeId);

    List<Cita> findByProfesionalIdAndEstado(Long profesionalId, EstadoCita estado);
}
