package com.app.fisiolab_system.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.app.fisiolab_system.dto.CreateUsuarioRequest;
import com.app.fisiolab_system.dto.UpdateUsuarioRequest;
import com.app.fisiolab_system.model.Auditoria;
import com.app.fisiolab_system.model.RolUsuario;
import com.app.fisiolab_system.model.Usuario;
import com.app.fisiolab_system.repository.AuditoriaRepository;
import com.app.fisiolab_system.repository.UsuarioRepository;

class UsuarioAdminServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private AuditoriaRepository auditoriaRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioAdminService usuarioAdminService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createProfessional_debe_crear_usuario_y_auditar() {
        CreateUsuarioRequest req = new CreateUsuarioRequest(
                "1234567890",
                "TEST@MAIL.COM",
                "Ana",
                "Perez",
                "Password#123",
                RolUsuario.FISIOTERAPEUTA,
                "Traumatologia",
                "Fisio",
                "REG-001");

        when(usuarioRepository.findByCedula("1234567890")).thenReturn(Optional.empty());
        when(usuarioRepository.findByEmail("TEST@MAIL.COM")).thenReturn(Optional.empty());
        when(usuarioRepository.existsByCodigoRegistro("REG-001")).thenReturn(false);
        when(passwordEncoder.encode("Password#123")).thenReturn("ENCODED");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario toSave = invocation.getArgument(0);
            toSave.setId(10L);
            return toSave;
        });

        var response = usuarioAdminService.createProfessional(req, "127.0.0.1");

        assertEquals(10L, response.id());
        assertEquals("test@mail.com", response.email());
        assertTrue(response.activo());

        ArgumentCaptor<Auditoria> auditoriaCaptor = ArgumentCaptor.forClass(Auditoria.class);
        verify(auditoriaRepository).save(auditoriaCaptor.capture());
        Auditoria savedAudit = auditoriaCaptor.getValue();
        assertEquals(10L, savedAudit.getUsuarioId());
        assertEquals("CREAR_USUARIO", savedAudit.getAccion());
    }

    @Test
    void createProfessional_debe_fallar_si_email_duplicado() {
        CreateUsuarioRequest req = new CreateUsuarioRequest(
                "1234567890",
                "dup@mail.com",
                "Ana",
                "Perez",
                "Password#123",
                RolUsuario.FISIOTERAPEUTA,
                null,
                null,
                null);

        Usuario existing = Usuario.builder().id(99L).email("dup@mail.com").build();

        when(usuarioRepository.findByCedula("1234567890")).thenReturn(Optional.empty());
        when(usuarioRepository.findByEmail("dup@mail.com")).thenReturn(Optional.of(existing));

        assertThrows(IllegalArgumentException.class,
                () -> usuarioAdminService.createProfessional(req, "127.0.0.1"));
    }

    @Test
    void actualizar_debe_modificar_campos_y_auditar() {
        Long id = 1L;
        Usuario dbUser = Usuario.builder()
                .id(id)
                .cedula("111")
                .email("old@mail.com")
                .name("Old")
                .lastName("User")
                .rol(RolUsuario.FISIOTERAPEUTA)
                .activo(true)
                .build();

        UpdateUsuarioRequest req = new UpdateUsuarioRequest(
                "222",
                "NEW@MAIL.COM",
                "Nuevo",
                "Nombre",
                RolUsuario.MEDICO,
                "Cardio",
                "Especialista",
                "REG-NEW");

        when(usuarioRepository.findById(id)).thenReturn(Optional.of(dbUser));
        when(usuarioRepository.findByCedula("222")).thenReturn(Optional.of(dbUser));
        when(usuarioRepository.findByEmail("NEW@MAIL.COM")).thenReturn(Optional.of(dbUser));
        when(usuarioRepository.existsByCodigoRegistro("REG-NEW")).thenReturn(false);
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = usuarioAdminService.actualizar(id, req, "127.0.0.1");

        assertEquals("222", response.cedula());
        assertEquals("new@mail.com", response.email());
        assertEquals(RolUsuario.MEDICO, response.rol());

        ArgumentCaptor<Auditoria> auditoriaCaptor = ArgumentCaptor.forClass(Auditoria.class);
        verify(auditoriaRepository).save(auditoriaCaptor.capture());
        assertEquals("ACTUALIZAR_USUARIO", auditoriaCaptor.getValue().getAccion());
    }

    @Test
    void activar_y_desactivar_deben_cambiar_estado() {
        Long id = 2L;
        Usuario user = Usuario.builder().id(id).activo(false).build();

        when(usuarioRepository.findById(id)).thenReturn(Optional.of(user));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var activated = usuarioAdminService.activar(id, "127.0.0.1");
        assertTrue(activated.activo());

        var deactivated = usuarioAdminService.desactivar(id, "127.0.0.1");
        assertFalse(deactivated.activo());
    }

    @Test
    void createProfessional_debe_fallar_si_codigo_registro_duplicado() {
        CreateUsuarioRequest req = new CreateUsuarioRequest(
                "1234567890",
                "ok@mail.com",
                "Ana",
                "Perez",
                "Password#123",
                RolUsuario.FISIOTERAPEUTA,
                null,
                null,
                "REG-001");

        Usuario existing = Usuario.builder().id(55L).codigoRegistro("REG-001").build();

        when(usuarioRepository.findByCedula("1234567890")).thenReturn(Optional.empty());
        when(usuarioRepository.findByEmail("ok@mail.com")).thenReturn(Optional.empty());
        when(usuarioRepository.existsByCodigoRegistro("REG-001")).thenReturn(true);
        when(usuarioRepository.findAll()).thenReturn(Collections.singletonList(existing));

        assertThrows(IllegalArgumentException.class,
                () -> usuarioAdminService.createProfessional(req, "127.0.0.1"));
    }

    @Test
    void listar_debe_retornar_usuarios_mapeados() {
        Usuario u1 = Usuario.builder().id(1L).cedula("111").email("u1@mail.com").name("A").lastName("B")
                .rol(RolUsuario.ADMINISTRADOR).activo(true).build();
        Usuario u2 = Usuario.builder().id(2L).cedula("222").email("u2@mail.com").name("C").lastName("D")
                .rol(RolUsuario.MEDICO).activo(false).build();

        when(usuarioRepository.findAll()).thenReturn(List.of(u1, u2));

        var response = usuarioAdminService.listar();

        assertEquals(2, response.size());
        assertEquals("u1@mail.com", response.get(0).email());
        assertEquals("u2@mail.com", response.get(1).email());
    }

    @Test
    void desactivarTiempoBloqueo_debe_limpiar_bloqueo_e_intentos() {
        Long id = 3L;
        Usuario user = Usuario.builder()
                .id(id)
                .activo(true)
                .intentosFallidos(5)
                .bloqueadoHasta(java.time.LocalDateTime.now().plusMinutes(10))
                .build();

        when(usuarioRepository.findById(id)).thenReturn(Optional.of(user));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = usuarioAdminService.desactivarTiempoBloqueo(id, "127.0.0.1");

        assertEquals(0, user.getIntentosFallidos());
        assertEquals(id, response.id());
        assertEquals(true, response.activo());
        assertEquals(null, user.getBloqueadoHasta());

        ArgumentCaptor<Auditoria> auditoriaCaptor = ArgumentCaptor.forClass(Auditoria.class);
        verify(auditoriaRepository).save(auditoriaCaptor.capture());
        assertEquals("DESACTIVAR_TIEMPO_BLOQUEO", auditoriaCaptor.getValue().getAccion());
    }
}
