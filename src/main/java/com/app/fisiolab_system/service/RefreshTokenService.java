package com.app.fisiolab_system.service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.app.fisiolab_system.model.RefreshToken;
import com.app.fisiolab_system.model.Usuario;
import com.app.fisiolab_system.repository.RefreshTokenRepository;

import jakarta.transaction.Transactional;

@Service
public class RefreshTokenService {

    @Value("${jwt.refresh.expiration:604800000}")
    private long refreshExpirationMs;

    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Transactional
    public RefreshToken createRefreshToken(Usuario usuario) {
        refreshTokenRepository.revokeAllByUsuario(usuario);

        Instant now = Instant.now();
        RefreshToken token = RefreshToken.builder()
                .usuario(usuario)
                .token(UUID.randomUUID().toString() + "-" + UUID.randomUUID())
                .createdAt(now)
                .expiryDate(now.plusMillis(refreshExpirationMs))
                .revoked(false)
                .build();

        return refreshTokenRepository.save(token);
    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    public RefreshToken verifyExpirationAndStatus(RefreshToken token) {
        if (token.isRevoked()) {
            throw new IllegalStateException("Refresh token has been revoked. Please login again.");
        }
        if (token.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(token);
            throw new IllegalStateException("Refresh token expired. Please login again.");
        }
        return token;
    }

    @Transactional
    public void revokeByUsuario(Usuario usuario) {
        refreshTokenRepository.revokeAllByUsuario(usuario);
    }

    public long getRefreshExpirationMs() {
        return refreshExpirationMs;
    }
}
