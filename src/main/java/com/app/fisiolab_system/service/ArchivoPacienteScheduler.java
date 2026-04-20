package com.app.fisiolab_system.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ArchivoPacienteScheduler {

    private final PacienteService pacienteService;

    public ArchivoPacienteScheduler(PacienteService pacienteService) {
        this.pacienteService = pacienteService;
    }

    @Scheduled(cron = "${app.pacientes.archivo-cron:0 0 2 * * *}")
    public void actualizarArchivoPasivo() {
        pacienteService.actualizarArchivosPasivosAutomaticamente();
    }
}
