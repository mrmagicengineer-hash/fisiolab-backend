package com.app.fisiolab_system.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.app.fisiolab_system.model.HistoriaClinica;

public interface HistoriaClinicaRepository extends JpaRepository<HistoriaClinica, Long> {

    Optional<HistoriaClinica> findByPacienteId(Long pacienteId);

    Optional<HistoriaClinica> findByNumeroHcl(String numeroHcl);
}
