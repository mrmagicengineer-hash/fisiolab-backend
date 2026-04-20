package com.app.fisiolab_system.service;

import org.springframework.stereotype.Service;

import com.app.fisiolab_system.model.Auditoria;
import com.app.fisiolab_system.repository.AuditoriaRepository;

@Service
public class AuditoriaService {
    
    // Creamos una instancia del repositorio de auditoría (puede ser una base de datos, un archivo, etc.)
    private final AuditoriaRepository auditoriaRepository;

    // Constructor para inyectar el repositorio de auditoría
    public AuditoriaService(AuditoriaRepository auditoriaRepository) {
        this.auditoriaRepository = auditoriaRepository;
    }

    // Método para registrar una acción en la auditoría
    public void registrar(Long usuarioId, String accion, String detalle, String ip) {
        Auditoria evento = Auditoria.builder()
                .usuarioId(usuarioId != null ? usuarioId : 0L) // Si no se proporciona un ID de usuario, se asigna 0
                .accion(accion)
                .detalle(detalle)
                .ip(ip)
                .build();
        auditoriaRepository.save(evento);
    }
}
