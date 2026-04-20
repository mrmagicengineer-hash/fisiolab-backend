package com.app.fisiolab_system.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.app.fisiolab_system.model.HclSecuencia;

public interface HclSecuenciaRepository extends JpaRepository<HclSecuencia, Integer> {

    @Query(value = "select anio, ultimo_numero, version from hcl_secuencia where anio = :anio for update", nativeQuery = true)
    Optional<HclSecuencia> findByAnioForUpdate(Integer anio);
}
