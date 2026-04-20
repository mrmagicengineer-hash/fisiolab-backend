package com.app.fisiolab_system.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.app.fisiolab_system.dto.ChangePasswordRequest;
import com.app.fisiolab_system.dto.LoginRequest;
import com.app.fisiolab_system.dto.LoginResponse;
import com.app.fisiolab_system.model.RefreshToken;
import com.app.fisiolab_system.model.Usuario;
import com.app.fisiolab_system.repository.UsuarioRepository;

/**
 * Authentication Service
 * 
 * This class handles the business logic for user login.
 * It validates credentials, manages account security (failed attempts and locking),
 * and generates a JWT token upon successful authentication.
 */
@Service
public class AuthService {

    // Definir Regex para validar nueva contraseña
    private static final Pattern STRONG_PASSWORD_PATTERN = 
        Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#._\\-]).{8,}$");

    @Value("${jwt.expiration}")
    private long expirationIn;   // Token expiration time in milliseconds

    @Value("${auth.max-failed-attempts:5}")
    private int maxFailedAttempts;

    @Value("${auth.lock-minutes:15}")
    private long lockMinutes;

    @Value("${app.timezone:UTC}")
    private String appTimezone;

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuditoriaService auditoriaService;
    private final RefreshTokenService refreshTokenService;

    /**
     * Constructor with dependency injection
     */
    public AuthService(UsuarioRepository usuarioRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       AuditoriaService auditoriaService,
                       RefreshTokenService refreshTokenService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.auditoriaService = auditoriaService;
        this.refreshTokenService = refreshTokenService;
    }

    /**
     * Main login method.
     * Performs all security validations and returns a JWT token if login is successful.
     *
     * @param request LoginRequest containing email and password
     * @return LoginResponse containing the JWT token and token type
     */
    public LoginResponse login(LoginRequest request, String clientIp) {
        LocalDateTime now = LocalDateTime.now(ZoneId.of(appTimezone));

        // Step 1: Find the user by email
        Usuario usuario = usuarioRepository.findByEmail(request.email())
                .orElseThrow(() -> {
                    // Registrar en la auditoría
                    auditoriaService.registrar(null, "LOGIN_FALLIDO", "Intento de login con email no registrado: " + request.email(), clientIp);
                    return new BadCredentialsException("Invalid credentials");
                });
        // Step 2: Check if the user account is active
        if (!usuario.isActivo()) {
            // Registrar en la auditoría
            auditoriaService.registrar(usuario.getId(), "LOGIN_DENEGADO", "Inactive account", clientIp);
            throw new IllegalStateException("User is inactive. Please contact the administrator.");
        }

        // Step 3: Check if the account is temporarily locked due to too many failed attempts
        if (usuario.getBloqueadoHasta() != null && usuario.getBloqueadoHasta().isAfter(now)) {
            // Registrar en la auditoría
            auditoriaService.registrar(usuario.getId(), "LOGIN_BLOQUEADO", "Blocked account login attempt", clientIp);
            throw new IllegalStateException(
                "Maximum login attempts exceeded. Your account has been temporarily locked until: " 
                + usuario.getBloqueadoHasta()
                + ". Please contact the administrator."
            );
        }

        // If lock time has already expired, clear lock state before validating credentials.
        if (usuario.getBloqueadoHasta() != null && !usuario.getBloqueadoHasta().isAfter(now)) {
            usuario.setBloqueadoHasta(null);
            usuario.setIntentosFallidos(0);
        }

        // Step 4: Verify the password
        if (!passwordEncoder.matches(request.password(), usuario.getPasswordHash())) {
            
            // Increase failed attempt counter
            usuario.setIntentosFallidos(usuario.getIntentosFallidos() + 1);

            // Lock the account if the user has reached 5 failed attempts
            if (usuario.getIntentosFallidos() >= maxFailedAttempts) {
                usuario.setBloqueadoHasta(now.plusMinutes(lockMinutes));
                // Registrar en la auditoría
                auditoriaService.registrar(usuario.getId(), "BLOQUEO_CUENTA", "Account locked due to too many failed login attempts", clientIp);
            }

            // Save the updated user information
            usuarioRepository.save(usuario);

            throw new BadCredentialsException("Invalid credentials");
        }

        // Step 5: Successful login - Reset security counters
        usuario.setIntentosFallidos(0);
        usuario.setBloqueadoHasta(null);
        usuarioRepository.save(usuario);

        // Registrar en la auditoría
        auditoriaService.registrar(usuario.getId(), "LOGIN_EXITOSO", "User logged in successfully", clientIp);

        // Step 6: Generate JWT token
        String token = jwtService.generateToken(usuario);

        // Step 7: Generate refresh token (rotates and revokes previous ones)
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(usuario);

        // Step 8: Return login response with token information
        return new LoginResponse(token, refreshToken.getToken(), "Bearer", expirationIn);
    }

    /**
     * Exchanges a valid refresh token for a new JWT access token.
     * Rotates the refresh token so the previous one is revoked.
     */
    public LoginResponse refreshToken(String refreshTokenValue, String clientIp) {
        RefreshToken stored = refreshTokenService.findByToken(refreshTokenValue)
                .orElseThrow(() -> {
                    auditoriaService.registrar(null, "REFRESH_TOKEN_FALLIDO",
                            "Refresh token no encontrado", clientIp);
                    return new BadCredentialsException("Invalid refresh token");
                });

        refreshTokenService.verifyExpirationAndStatus(stored);

        Usuario usuario = stored.getUsuario();
        if (!usuario.isActivo()) {
            auditoriaService.registrar(usuario.getId(), "REFRESH_TOKEN_DENEGADO",
                    "Refresh token usado por cuenta inactiva", clientIp);
            throw new IllegalStateException("User is inactive. Please contact the administrator.");
        }

        String newAccessToken = jwtService.generateToken(usuario);
        RefreshToken rotated = refreshTokenService.createRefreshToken(usuario);

        auditoriaService.registrar(usuario.getId(), "REFRESH_TOKEN_EXITOSO",
                "Refresh token utilizado y rotado", clientIp);

        return new LoginResponse(newAccessToken, rotated.getToken(), "Bearer", expirationIn);
    }

    /**
     * Revokes all refresh tokens for the authenticated user.
     */
    public void logout(String email, String clientIp) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + email));
        refreshTokenService.revokeByUsuario(usuario);
        auditoriaService.registrar(usuario.getId(), "LOGOUT",
                "Sesion cerrada, refresh tokens revocados", clientIp);
    }

    // Funcion para cambiar la contraseña de un usuario
    public void changePassword(String email, ChangePasswordRequest request, String clientIp) {
        // 1. Buscar el usuario por email
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + email));

        // 2. Verificar que la contraseña actual sea correcta
        if(!passwordEncoder.matches(request.currentPassword(), usuario.getPasswordHash())) {
            //Registrar en la auditoría
            auditoriaService.registrar(usuario.getId(), "PASSWORD_CHANGE_FAILED", "Current password is incorrect", clientIp);
            throw new BadCredentialsException("Current password is incorrect");
        }
        // 3. Validar que la nueva contraseña no sea la misma que la actual
        if (passwordEncoder.matches(request.newPassword(), usuario.getPasswordHash())){
            throw new IllegalArgumentException("New password cannot be the same as the current password");
        }
        // 4. Validar que la nueva contraseña cumpla con los requisitos de seguridad
        if (!STRONG_PASSWORD_PATTERN.matcher(request.newPassword()).matches()) {
            throw new IllegalArgumentException(" La contraseña debe tener al menos 8 caracteres, incluyendo mayúsculas, minúsculas, números y caracteres especiales.");
        }

        // 5. Encriptar la nueva contraseña y actualizar el usuario
        usuario.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        usuarioRepository.save(usuario);

        //Registrar en la auditoría
        auditoriaService.registrar(usuario.getId(), "PASSWORD_CHANGED", "User changed their password successfully", clientIp);
    }
}