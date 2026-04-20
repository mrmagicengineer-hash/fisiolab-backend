package com.app.fisiolab_system.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.app.fisiolab_system.model.AntecedentePersonal;

public interface AntecedentePersonalRepository extends JpaRepository<AntecedentePersonal, Long> {

    List<AntecedentePersonal> findByHistoriaClinicaIdOrderByFechaRegistroDesc(Long historiaClinicaId);

    void deleteByHistoriaClinicaId(Long historiaClinicaId);
}
