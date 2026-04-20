package com.app.fisiolab_system.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import com.app.fisiolab_system.dto.LoginRequest;
import com.app.fisiolab_system.model.RolUsuario;
import com.app.fisiolab_system.model.Usuario;
import com.app.fisiolab_system.repository.UsuarioRepository;



class AuthServiceTest {

    @Mock UsuarioRepository usuarioRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtService jwtService;
    @Mock AuditoriaService auditoriaService;


    @InjectMocks AuthService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(authService, "expirationIn", 3600000L); // Set expiration time for testing
        ReflectionTestUtils.setField(authService, "maxFailedAttempts", 5);
        ReflectionTestUtils.setField(authService, "lockMinutes", 15L);
        ReflectionTestUtils.setField(authService, "appTimezone", "UTC");
    }
    
    @Test
    void login_ok_debe_generar_token_y_resetear_intentos() {
        // Implementar test para login exitoso

        Usuario u = Usuario.builder()
            .id(1L).email("test@example.com").passwordHash("hashedPassword")
            .rol(RolUsuario.ADMINISTRADOR).activo(true).intentosFallidos(3).bloqueadoHasta(LocalDateTime.now().minusMinutes(1))
            .build();

        when(usuarioRepository.findByEmail("test@example.com")).thenReturn(Optional.of(u));
        when(passwordEncoder.matches("plainPassword", "hashedPassword")).thenReturn(true);
        when(jwtService.generateToken(u)).thenReturn("jwt");

        var response = authService.login(new LoginRequest("test@example.com", "plainPassword"), "127.0.0.1");

        assertEquals("jwt", response.token());
        assertEquals("Bearer", response.type());
        assertEquals(0, u.getIntentosFallidos());
        assertNull(u.getBloqueadoHasta());
        verify(usuarioRepository).save(u);
        verify(auditoriaService).registrar(1L, "LOGIN_EXITOSO", "User logged in successfully", "127.0.0.1");
    }

    @Test 
    void login_password_incorrecto_debe_incrementar_intentos() {
        Usuario u = Usuario.builder()
            .id(1L).email("test@example.com").passwordHash("hashedPassword")
            .rol(RolUsuario.ADMINISTRADOR).activo(true)
            .intentosFallidos(4)
            .build();

        when(usuarioRepository.findByEmail("test@example.com")).thenReturn(Optional.of(u));
        when(passwordEncoder.matches("bad", "hashedPassword")).thenReturn(false);

        assertThrows(BadCredentialsException.class, 
            () -> authService.login(new LoginRequest("test@example.com", "bad"), "127.0.0.1")
        );
        assertEquals(5, u.getIntentosFallidos());
        assertNotNull(u.getBloqueadoHasta());
        verify(usuarioRepository).save(u);
        verify(auditoriaService).registrar(1L, "BLOQUEO_CUENTA", 
            "Account locked due to too many failed login attempts", "127.0.0.1");
    }

    @Test
    void login_con_bloqueo_expirado_y_password_incorrecto_debe_reiniciar_contador_antes_de_sumar() {
        Usuario u = Usuario.builder()
            .id(1L).email("test@example.com").passwordHash("hashedPassword")
            .rol(RolUsuario.ADMINISTRADOR).activo(true)
            .intentosFallidos(5)
            .bloqueadoHasta(LocalDateTime.now().minusMinutes(1))
            .build();

        when(usuarioRepository.findByEmail("test@example.com")).thenReturn(Optional.of(u));
        when(passwordEncoder.matches("bad", "hashedPassword")).thenReturn(false);

        assertThrows(BadCredentialsException.class,
            () -> authService.login(new LoginRequest("test@example.com", "bad"), "127.0.0.1")
        );

        assertEquals(1, u.getIntentosFallidos());
        assertNull(u.getBloqueadoHasta());
        verify(usuarioRepository).save(u);
    }
}