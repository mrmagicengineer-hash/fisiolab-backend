package com.app.fisiolab_system.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.app.fisiolab_system.model.EstadoArchivoPaciente;
import com.app.fisiolab_system.model.Paciente;

public interface PacienteRepository extends JpaRepository<Paciente, Long> {

    boolean existsByCedula(String cedula);

    boolean existsByEmail(String email);

    Optional<Paciente> findByCedula(String cedula);

    Optional<Paciente> findByEmail(String email);

    List<Paciente> findAllByOrderByFechaRegistroDesc();

    @Query("""
            select p
            from Paciente p
            where lower(p.cedula) like lower(concat('%', :query, '%'))
               or lower(p.numeroHcl) like lower(concat('%', :query, '%'))
               or lower(p.nombresCompletos) like lower(concat('%', :query, '%'))
            order by p.fechaRegistro desc
            """)
        List<Paciente> buscarTarjeteroIndice(@Param("query") String query, Pageable pageable);

    @Query("""
            select p
            from Paciente p
            where coalesce(:query, '') = ''
               or lower(p.cedula) like lower(concat('%', :query, '%'))
               or lower(p.numeroHcl) like lower(concat('%', :query, '%'))
               or lower(p.nombresCompletos) like lower(concat('%', :query, '%'))
            """)
    org.springframework.data.domain.Page<Paciente> buscarConsultasResumen(@Param("query") String query, Pageable pageable);


    List<Paciente> findByEstadoArchivoAndFechaUltimaAtencionBefore(
            EstadoArchivoPaciente estadoArchivo,
            LocalDateTime fechaUltimaAtencion);
}
