package com.app.fisiolab_system.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.app.fisiolab_system.model.EgresoEpisodio;

public interface EgresoEpisodioRepository extends JpaRepository<EgresoEpisodio, Long> {

    Optional<EgresoEpisodio> findByEpisodioClinicoId(Long episodioClinicoId);
}
