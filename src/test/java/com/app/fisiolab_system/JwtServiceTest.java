package com.app.fisiolab_system;

import com.app.fisiolab_system.model.RolUsuario;
import com.app.fisiolab_system.model.Usuario;
import com.app.fisiolab_system.service.JwtService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;



public class JwtServiceTest {
    JwtService jwtService = new JwtService();

        @BeforeEach
        void init() {
            ReflectionTestUtils.setField(jwtService, "secret", "12345678901234567890123456789012");
            ReflectionTestUtils.setField(jwtService, "expiration", 3600000L); // 1 hour
        }

        @Test 
        void generate_y_extractUsername_ok() {
            Usuario u  = Usuario.builder().id(1L).email("admin@mail.com").rol(RolUsuario.ADMINISTRADOR).build();
            String token = jwtService.generateToken(u);

            assertEquals("admin@mail.com", jwtService.extractUsername(token));
        }

        @Test
        void isTokenValid_debe_retornar_true_para_usuario_correcto() {
            Usuario u = Usuario.builder().id(1L).email("admin@mail.com").rol(RolUsuario.ADMINISTRADOR).build();
            String token = jwtService.generateToken(u);

            UserDetails userDetails = User.withUsername("admin@mail.com")
                    .password("ignored")
                    .authorities("ROLE_ADMINISTRADOR")
                    .build();

            assertTrue(jwtService.isTokenValid(token, userDetails));
        }

        @Test
        void isTokenValid_debe_retornar_false_para_otro_usuario() {
            Usuario u = Usuario.builder().id(1L).email("admin@mail.com").rol(RolUsuario.ADMINISTRADOR).build();
            String token = jwtService.generateToken(u);

            UserDetails anotherUser = User.withUsername("otro@mail.com")
                    .password("ignored")
                    .authorities("ROLE_ADMINISTRADOR")
                    .build();

            assertFalse(jwtService.isTokenValid(token, anotherUser));
        }
}
