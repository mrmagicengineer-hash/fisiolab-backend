package com.app.fisiolab_system.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.app.fisiolab_system.model.BiometriaPaciente;

public interface BiometriaPacienteRepository extends JpaRepository<BiometriaPaciente, Long> {

    List<BiometriaPaciente> findByEpisodioClinicoIdOrderByFechaRegistroAsc(Long episodioClinicoId);

    Optional<BiometriaPaciente> findByIdAndEpisodioClinicoId(Long id, Long episodioClinicoId);
}
