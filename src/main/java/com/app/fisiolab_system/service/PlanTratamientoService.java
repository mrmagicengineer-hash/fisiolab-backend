package com.app.fisiolab_system.service;

import java.util.List;

import com.app.fisiolab_system.dto.CreatePlanTratamientoRequest;
import com.app.fisiolab_system.dto.CreateSeguimientoPlanRequest;
import com.app.fisiolab_system.dto.IndicadorAvancePlanResponse;
import com.app.fisiolab_system.dto.PlanTratamientoResponse;
import com.app.fisiolab_system.dto.SeguimientoPlanResponse;

public interface PlanTratamientoService {

    // RF-31: Crea un plan de tratamiento para un problema activo del episodio
    PlanTratamientoResponse crearPlan(Long episodioId, Long problemaId,
            CreatePlanTratamientoRequest request, String actorEmail, String clientIp);

    // Obtiene el plan asociado a un problema específico
    PlanTratamientoResponse obtenerPlanPorProblema(Long episodioId, Long problemaId);

    // Lista todos los planes del episodio
    List<PlanTratamientoResponse> listarPorEpisodio(Long episodioId);

    // RF-32: Registra una evaluación de seguimiento del plan
    SeguimientoPlanResponse registrarSeguimiento(Long episodioId, Long problemaId,
            CreateSeguimientoPlanRequest request, String actorEmail, String clientIp);

    // Lista el historial de seguimientos del plan
    List<SeguimientoPlanResponse> listarSeguimientos(Long episodioId, Long problemaId);

    // RF-33: Devuelve el indicador de avance de sesiones (realizadas vs planificadas)
    IndicadorAvancePlanResponse obtenerIndicador(Long episodioId, Long problemaId);

    // Lista pacientes con planes activos agrupados para la columna de navegación
    java.util.List<com.app.fisiolab_system.dto.PacienteResumenPlanesResponse> listarResumenPorPaciente();

    // Contadores globales para el dashboard (total, enRiesgo, finalizando)
    com.app.fisiolab_system.dto.EstadisticasDashboardResponse obtenerEstadisticasDashboard();

    // Línea de tiempo cronológica de sesiones SOAP y seguimientos del plan
    java.util.List<com.app.fisiolab_system.dto.TimelineItemResponse> getTimeline(Long planId);
}
