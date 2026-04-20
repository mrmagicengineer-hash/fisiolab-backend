package com.app.fisiolab_system.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.app.fisiolab_system.model.RefreshToken;
import com.app.fisiolab_system.model.Usuario;

import jakarta.transaction.Transactional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    @Modifying
    @Transactional
    @Query("update RefreshToken rt set rt.revoked = true where rt.usuario = :usuario and rt.revoked = false")
    int revokeAllByUsuario(@Param("usuario") Usuario usuario);

    @Modifying
    @Transactional
    int deleteByUsuario(Usuario usuario);
}
