package com.app.fisiolab_system.dto;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Formato compatible con FullCalendar para renderizado de agenda.
 * Soporta tanto Cita como BloqueoAgenda usando el campo tipo.
 */
public record CalendarEventResponse(
        String id,
        String title,
        LocalDateTime start,
        LocalDateTime end,
        String color,
        String tipo,
        Map<String, Object> extendedProps
) {
}
