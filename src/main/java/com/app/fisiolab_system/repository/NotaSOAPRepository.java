package com.app.fisiolab_system.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.app.fisiolab_system.model.NotaSOAP;

public interface NotaSOAPRepository extends JpaRepository<NotaSOAP, Long> {

    Optional<NotaSOAP> findBySesionTerapiaId(Long sesionTerapiaId);
}
