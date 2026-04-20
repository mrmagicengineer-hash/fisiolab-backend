package com.app.fisiolab_system.event;

import java.time.LocalDateTime;

/**
 * Evento publicado cuando una Cita transiciona al estado REALIZADA.
 * El Módulo 7 (SesionSOAP) escucha este evento para crear el borrador
 * de la nota SOAP con los datos pre-cargados (traspaso de contexto).
 */
public record CitaRealizadaEvent(
        Long citaId,
        Long pacienteId,
        Long profesionalId,
        Long episodioClinicoId,
        Long planTratamientoId,
        String motivoConsulta,
        String codigoCie10Sugerido,
        LocalDateTime fechaHoraRealizada
) {
}
