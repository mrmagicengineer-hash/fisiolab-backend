package com.app.fisiolab_system.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.app.fisiolab_system.model.AdmisionEpisodio;

public interface AdmisionEpisodioRepository extends JpaRepository<AdmisionEpisodio, Long> {

    Optional<AdmisionEpisodio> findByEpisodioClinicoId(Long episodioClinicoId);
}
