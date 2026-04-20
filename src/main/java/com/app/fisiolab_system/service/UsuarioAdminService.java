package com.app.fisiolab_system.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.app.fisiolab_system.dto.CreateUsuarioRequest;
import com.app.fisiolab_system.dto.UpdateUsuarioRequest;
import com.app.fisiolab_system.dto.UsuarioAdminResponse;
import com.app.fisiolab_system.model.Auditoria;
import com.app.fisiolab_system.model.Usuario;
import com.app.fisiolab_system.repository.AuditoriaRepository;
import com.app.fisiolab_system.repository.UsuarioRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class UsuarioAdminService {
    private final UsuarioRepository usuarioRepository;
    private final AuditoriaRepository auditoriaRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.timezone:UTC}")
    private String appTimezone;

    public UsuarioAdminService(
            UsuarioRepository usuarioRepository,
            AuditoriaRepository auditoriaRepository,
            PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.auditoriaRepository = auditoriaRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Step 1: Create a professional user account
    public UsuarioAdminResponse createProfessional(CreateUsuarioRequest req, String clientIp) {
        validateUniqueFields(req.cedula(), req.email(), req.codigoRegistro(), null);

        Usuario usuario = Usuario.builder()
                .cedula(req.cedula().trim())
                .email(req.email().trim().toLowerCase())
                .name(req.name().trim())
                .lastName(req.lastName().trim())
                .passwordHash(passwordEncoder.encode(req.password()))
                .rol(req.rol())
                .activo(true)
                .intentosFallidos(0)
                .especialidad(req.especialidad())
                .tipoProfesional(req.tipoProfesional())
                .codigoRegistro(req.codigoRegistro())
                .build();

        Usuario saved = usuarioRepository.save(usuario);

        audit(saved.getId(), "CREAR_USUARIO", "Admin created a new professional user", clientIp);
        return toResponse(saved);
    }

    // Step 2: Update a professinal user account
    public UsuarioAdminResponse actualizar(Long id, UpdateUsuarioRequest req, String clientIp) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + id));

        validateUniqueFields(req.cedula(), req.email(), req.codigoRegistro(), id);

        usuario.setCedula(req.cedula().trim());
        usuario.setEmail(req.email().trim().toLowerCase());
        usuario.setName(req.name().trim());
        usuario.setLastName(req.lastName().trim());
        usuario.setRol(req.rol());
        usuario.setEspecialidad(req.especialidad());
        usuario.setTipoProfesional(req.tipoProfesional());
        usuario.setCodigoRegistro(req.codigoRegistro());

        Usuario updated = usuarioRepository.save(usuario);

        audit(updated.getId(), "ACTUALIZAR_USUARIO", "Admin updated a professional account", clientIp);
        return toResponse(updated);
    }

    // Step 3: Activate account
    public UsuarioAdminResponse activar(Long id, String clientIp) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + id));

        usuario.setActivo(true);
        Usuario updated = usuarioRepository.save(usuario);

        audit(updated.getId(), "ACTIVAR_USUARIO", "Admin activated a user account", clientIp);
        return toResponse(updated);
    }

    // Step 4: Deactivate account
    public UsuarioAdminResponse desactivar(Long id, String clientIp) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + id));

        usuario.setActivo(false);
        Usuario updated = usuarioRepository.save(usuario);

        audit(updated.getId(), "DESACTIVAR_USUARIO", "Admin deactivated a user account", clientIp);
        return toResponse(updated);
    }

    // Step 5: List users (Useful for admin UI)
    public List<UsuarioAdminResponse> listar() {
        return usuarioRepository.findAll().stream().map(this::toResponse).toList();
    }

    // Step 6: Disable temporary lock time for a user.
    public UsuarioAdminResponse desactivarTiempoBloqueo(Long id, String clientIp) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + id));

        usuario.setIntentosFallidos(0);
        usuario.setBloqueadoHasta(null);
        Usuario updated = usuarioRepository.save(usuario);

        audit(updated.getId(), "DESACTIVAR_TIEMPO_BLOQUEO", "Admin disabled temporary lock time", clientIp);
        return toResponse(updated);
    }
    
    public List<UsuarioAdminResponse> listarActivos() {
        return usuarioRepository.findByActivoTrue().stream().map(this::toResponse).toList();
    }

    public List<UsuarioAdminResponse> listarDesactivados(){
        return usuarioRepository.findByActivoFalse().stream().map(this::toResponse).toList();
    }

    public List<UsuarioAdminResponse> listarBloqueados(){
        LocalDateTime now = LocalDateTime.now(ZoneId.of(appTimezone));
        return usuarioRepository.findByBloqueadoHastaAfter(now).stream().map(this::toResponse).toList();
    }

    public List<Auditoria> listarEventosAuditoria() {
        return auditoriaRepository.findAllByOrderByFechaHoraDesc();
    }


    private void validateUniqueFields(String cedula, String email, String codigoRegistro, Long currentId) {
        usuarioRepository.findByCedula(cedula).ifPresent(u -> {
            if (!u.getId().equals(currentId)) {
                throw new IllegalArgumentException("La cédula ya está en uso por otro usuario.");
            }
        });

        usuarioRepository.findByEmail(email).ifPresent(u -> {
            if (!u.getId().equals(currentId)) {
                throw new IllegalArgumentException("El correo ya esta en uso");
            }
        });

        if (codigoRegistro != null && !codigoRegistro.isBlank()) {
            boolean exists = usuarioRepository.existsByCodigoRegistro(codigoRegistro);
            if (exists) {
                usuarioRepository.findAll().stream()
                        .filter(u -> codigoRegistro.equals(u.getCodigoRegistro()) && !u.getId().equals(currentId))
                        .findFirst()
                        .ifPresent(u -> {
                            if (!u.getId().equals(currentId)) {
                                throw new IllegalArgumentException(
                                        "El código de registro ya está en uso por otro usuario.");
                            }
                        });
            }
        }
    }

    private void audit(Long usuarioId, String action, String detalle, String ip) {
        Auditoria event = Auditoria.builder()
                .usuarioId(usuarioId)
                .accion(action)
                .detalle(detalle)
                .ip(ip)
                .build();
        auditoriaRepository.save(event);
    }

    private UsuarioAdminResponse toResponse(Usuario u) {
        return new UsuarioAdminResponse(
                u.getId(),
                u.getCedula(),
                u.getEmail(),
                u.getName(),
                u.getLastName(),
                u.getRol(),
                u.isActivo(),
                u.getEspecialidad(),
                u.getTipoProfesional(),
                u.getCodigoRegistro());
    }

    
}
