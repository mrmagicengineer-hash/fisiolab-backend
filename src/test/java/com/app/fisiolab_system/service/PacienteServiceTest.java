package com.app.fisiolab_system.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.Year;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import com.app.fisiolab_system.dto.CreatePacienteRequest;
import com.app.fisiolab_system.dto.FichaFamiliarRequest;
import com.app.fisiolab_system.dto.UpdatePacienteRequest;
import com.app.fisiolab_system.model.EstadoArchivoPaciente;
import com.app.fisiolab_system.model.FichaFamiliar;
import com.app.fisiolab_system.model.HclSecuencia;
import com.app.fisiolab_system.model.Paciente;
import com.app.fisiolab_system.model.Usuario;
import com.app.fisiolab_system.repository.FichaFamiliarRepository;
import com.app.fisiolab_system.repository.HclSecuenciaRepository;
import com.app.fisiolab_system.repository.PacienteRepository;
import com.app.fisiolab_system.repository.UsuarioRepository;

class PacienteServiceTest {

    @Mock
    private PacienteRepository pacienteRepository;

    @Mock
    private FichaFamiliarRepository fichaFamiliarRepository;

    @Mock
    private HclSecuenciaRepository hclSecuenciaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private AuditoriaService auditoriaService;

    @Mock
    private HistoriaClinicaService historiaClinicaService;

    @InjectMocks
    private PacienteService pacienteService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(pacienteService, "appTimezone", "America/Guayaquil");
    }

    @Test
    void registrarPaciente_debe_generar_hcl_guardar_y_auditar() {
        CreatePacienteRequest req = new CreatePacienteRequest(
                "1712345678",
            "maria@test.com",
                "Maria Perez",
                LocalDate.of(1990, 5, 10),
                "FEMENINO",
                "Mestizo",
                "Soltera",
                "Docente",
                "IESS",
                "O+",
                "0999999999",
                "022222222",
                "Quito");

        int year = Year.now(ZoneId.of("America/Guayaquil")).getValue();

        when(pacienteRepository.existsByCedula("1712345678")).thenReturn(false);
    when(pacienteRepository.existsByEmail("maria@test.com")).thenReturn(false);
        when(hclSecuenciaRepository.findByAnioForUpdate(year)).thenReturn(Optional.empty());
        when(hclSecuenciaRepository.save(any(HclSecuencia.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(pacienteRepository.save(any(Paciente.class))).thenAnswer(invocation -> {
            Paciente p = invocation.getArgument(0);
            p.setId(1L);
            p.setFechaRegistro(LocalDateTime.now());
            p.setFechaUltimaAtencion(LocalDateTime.now());
            return p;
        });
        when(usuarioRepository.findByEmail("admin@fisiolab.com")).thenReturn(Optional.of(Usuario.builder().id(99L).build()));

        var response = pacienteService.registrarPaciente(req, "admin@fisiolab.com", "127.0.0.1");

        assertEquals(1L, response.id());
        assertEquals("1712345678", response.cedula());
        assertEquals("maria@test.com", response.email());
        assertTrue(response.numeroHcl().startsWith("HC-" + year + "-"));
        assertEquals(EstadoArchivoPaciente.ACTIVO, response.estadoArchivo());

        verify(auditoriaService).registrar(
                99L,
                "REGISTRO_PACIENTE",
                "Registro de paciente nuevo en tarjetero indice: " + response.numeroHcl(),
                "127.0.0.1");
        verify(historiaClinicaService).asegurarAperturaAutomatica(any(Paciente.class));
    }

    @Test
    void buscarPacientes_debe_fallar_si_query_tiene_menos_de_3_caracteres() {
        assertThrows(IllegalArgumentException.class, () -> pacienteService.buscarPacientes("ab"));
    }

    @Test
    void actualizarArchivosPasivosAutomaticamente_debe_marcar_pasivos_y_auditar() {
        Paciente p1 = Paciente.builder().id(1L).estadoArchivo(EstadoArchivoPaciente.ACTIVO).build();
        Paciente p2 = Paciente.builder().id(2L).estadoArchivo(EstadoArchivoPaciente.ACTIVO).build();

        when(pacienteRepository.findByEstadoArchivoAndFechaUltimaAtencionBefore(
                any(EstadoArchivoPaciente.class),
                any(LocalDateTime.class))).thenReturn(List.of(p1, p2));

        int total = pacienteService.actualizarArchivosPasivosAutomaticamente();

        assertEquals(2, total);
        assertEquals(EstadoArchivoPaciente.PASIVO, p1.getEstadoArchivo());
        assertEquals(EstadoArchivoPaciente.PASIVO, p2.getEstadoArchivo());
        verify(pacienteRepository).saveAll(List.of(p1, p2));
        verify(auditoriaService).registrar(
                0L,
                "ARCHIVO_PASIVO_AUTOMATICO",
                "Pacientes marcados como PASIVO por inactividad >= 5 anos: 2",
                "sistema");
    }

    @Test
    void guardarFichaFamiliar_debe_crear_o_actualizar_ficha() {
        Long pacienteId = 5L;
        Paciente paciente = Paciente.builder().id(pacienteId).numeroHcl("HC-2026-00010").build();
        FichaFamiliarRequest request = new FichaFamiliarRequest("Carlos Perez", 4, "Casa propia", "Agua potable");

        when(pacienteRepository.findById(pacienteId)).thenReturn(Optional.of(paciente));
        when(fichaFamiliarRepository.findByPacienteId(pacienteId)).thenReturn(Optional.empty());
        when(fichaFamiliarRepository.save(any(FichaFamiliar.class))).thenAnswer(invocation -> {
            FichaFamiliar ficha = invocation.getArgument(0);
            ficha.setId(20L);
            ficha.setFechaActualizacion(LocalDateTime.now());
            return ficha;
        });
        when(usuarioRepository.findByEmail("fisio@fisiolab.com")).thenReturn(Optional.of(Usuario.builder().id(10L).build()));

        var response = pacienteService.guardarFichaFamiliar(pacienteId, request, "fisio@fisiolab.com", "127.0.0.1");

        assertEquals(20L, response.id());
        assertEquals(pacienteId, response.pacienteId());
        assertEquals("Carlos Perez", response.jefeHogar());
        assertNotNull(response.fechaActualizacion());

        verify(auditoriaService).registrar(
                10L,
                "FICHA_FAMILIAR_ACTUALIZADA",
                "Registro/actualizacion de ficha familiar para paciente: HC-2026-00010",
                "127.0.0.1");
    }

    @Test
    void actualizarPaciente_debe_fallar_si_cedula_pertenece_a_otro_paciente() {
        Long id = 5L;
        UpdatePacienteRequest req = new UpdatePacienteRequest(
                "1711111111",
            "paciente@editado.com",
                "Paciente Editado",
                LocalDate.of(1991, 1, 1),
                "MASCULINO",
                null,
                null,
                null,
                null,
                null,
                "0991111111",
                null,
                null);

        Paciente actual = Paciente.builder().id(id).cedula("1700000000").numeroHcl("HC-2026-00011").build();
        Paciente otro = Paciente.builder().id(99L).cedula("1711111111").build();

        when(pacienteRepository.findById(id)).thenReturn(Optional.of(actual));
        when(pacienteRepository.findByCedula("1711111111")).thenReturn(Optional.of(otro));
    when(pacienteRepository.findByEmail("paciente@editado.com")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> pacienteService.actualizarPaciente(id, req, "admin@fisiolab.com", "127.0.0.1"));
    }

    @Test
    void buscarPacientes_debe_devolver_resultados() {
        Paciente p = Paciente.builder()
                .id(1L)
                .numeroHcl("HC-2026-00001")
                .cedula("1712345678")
                .email("maria@test.com")
                .nombresCompletos("Maria Perez")
                .fechaNacimiento(LocalDate.of(1990, 5, 10))
                .genero("FEMENINO")
                .estadoArchivo(EstadoArchivoPaciente.ACTIVO)
                .fechaRegistro(LocalDateTime.now())
                .fechaUltimaAtencion(LocalDateTime.now())
                .telefonoPrincipal("0999999999")
                .build();

        when(pacienteRepository.buscarTarjeteroIndice(any(String.class), any(Pageable.class))).thenReturn(List.of(p));

        var response = pacienteService.buscarPacientes("171");

        assertEquals(1, response.size());
        assertEquals("1712345678", response.get(0).cedula());
    }

    @Test
    void listarPacientesRegistrados_debe_devolver_todos_ordenados() {
        Paciente reciente = Paciente.builder()
                .id(2L)
                .numeroHcl("HC-2026-00002")
                .cedula("1700000002")
                .email("reciente@test.com")
                .nombresCompletos("Paciente Reciente")
                .fechaNacimiento(LocalDate.of(1995, 1, 10))
                .genero("FEMENINO")
                .telefonoPrincipal("0990000002")
                .estadoArchivo(EstadoArchivoPaciente.ACTIVO)
                .fechaRegistro(LocalDateTime.now())
                .fechaUltimaAtencion(LocalDateTime.now())
                .build();

        Paciente antiguo = Paciente.builder()
                .id(1L)
                .numeroHcl("HC-2026-00001")
                .cedula("1700000001")
                .email("antiguo@test.com")
                .nombresCompletos("Paciente Antiguo")
                .fechaNacimiento(LocalDate.of(1990, 1, 10))
                .genero("MASCULINO")
                .telefonoPrincipal("0990000001")
                .estadoArchivo(EstadoArchivoPaciente.ACTIVO)
                .fechaRegistro(LocalDateTime.now().minusDays(1))
                .fechaUltimaAtencion(LocalDateTime.now().minusDays(1))
                .build();

        when(pacienteRepository.findAllByOrderByFechaRegistroDesc()).thenReturn(List.of(reciente, antiguo));

        var response = pacienteService.listarPacientesRegistrados();

        assertEquals(2, response.size());
        assertEquals("1700000002", response.get(0).cedula());
        assertEquals("1700000001", response.get(1).cedula());
    }
}
