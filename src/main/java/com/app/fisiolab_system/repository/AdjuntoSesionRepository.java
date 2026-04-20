package com.app.fisiolab_system.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.app.fisiolab_system.model.AdjuntoSesion;

public interface AdjuntoSesionRepository extends JpaRepository<AdjuntoSesion, Long> {

    List<AdjuntoSesion> findBySesionTerapiaIdOrderByFechaSubidaAsc(Long sesionId);

    Optional<AdjuntoSesion> findByIdAndSesionTerapiaId(Long id, Long sesionId);
}
