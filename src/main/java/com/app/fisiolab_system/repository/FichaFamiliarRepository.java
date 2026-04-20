package com.app.fisiolab_system.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.app.fisiolab_system.model.FichaFamiliar;

public interface FichaFamiliarRepository extends JpaRepository<FichaFamiliar, Long> {

    Optional<FichaFamiliar> findByPacienteId(Long pacienteId);
}
