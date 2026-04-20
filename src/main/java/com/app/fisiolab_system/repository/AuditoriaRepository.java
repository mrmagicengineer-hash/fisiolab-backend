package com.app.fisiolab_system.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.app.fisiolab_system.model.Auditoria;

public interface AuditoriaRepository extends JpaRepository<Auditoria, Long>{
    List<Auditoria> findAllByOrderByFechaHoraDesc();
}
