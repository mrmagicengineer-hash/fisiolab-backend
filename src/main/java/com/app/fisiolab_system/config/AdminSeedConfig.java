package com.app.fisiolab_system.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.app.fisiolab_system.model.RolUsuario;
import com.app.fisiolab_system.model.Usuario;
import com.app.fisiolab_system.repository.UsuarioRepository;

@Configuration
public class AdminSeedConfig {
    @Bean
    CommandLineRunner seedAdminUser(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            String adminEmail = "admin@fisiolab.com";
            String adminCedula = "1721212312";
            
            // Idempotencia: Solo crear el usuario admin si no existe por email o cédula
            if (usuarioRepository.existsByEmail(adminEmail) ||  usuarioRepository.existsByCedula(adminCedula)){
                return;
            }

            Usuario admin = Usuario.builder()
                    .cedula(adminCedula)
                    .email(adminEmail)
                    .name("Mr Magic")
                    .lastName("Admin")
                    .passwordHash(passwordEncoder.encode("Admin123!"))
                    .rol(RolUsuario.ADMINISTRADOR)
                    .activo(true)
                    .intentosFallidos(0)
                    .especialidad("Administracion")
                    .tipoProfesional("Administrador")
                    .codigoRegistro("ADM-001")
                    .build();

            usuarioRepository.save(admin);

        };
    }
}
