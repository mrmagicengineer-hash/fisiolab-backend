package com.app.fisiolab_system.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.app.fisiolab_system.model.AntecedenteFamiliar;

public interface AntecedenteFamiliarRepository extends JpaRepository<AntecedenteFamiliar, Long> {

    List<AntecedenteFamiliar> findByHistoriaClinicaIdOrderByFechaRegistroDesc(Long historiaClinicaId);

    void deleteByHistoriaClinicaId(Long historiaClinicaId);
}
