# Rutas Implementadas — Fisiolab Backend

Base: `/api/v1`

---

## Módulo de Agenda — /citas

| Método | Ruta | Descripción |
|--------|------|-------------|
| `POST` | `/citas` | Crear cita. Acepta `planTratamientoId` (opcional). Valida que plan pertenezca al episodio. |
| `GET` | `/citas` | Listar citas. FISIOTERAPEUTA solo ve las suyas. |
| `GET` | `/citas/{id}` | Detalle de cita. |
| `PATCH` | `/citas/{id}/estado` | Cambiar estado (PROGRAMADA → REALIZADA / CANCELADA / NO_ASISTIDA). |
| `GET` | `/citas/disponibilidad` | ★ NUEVO — Verificar disponibilidad. Params: `profesionalId`, `desde`, `hasta`. Retorna `{"disponible": true/false}`. |
| `GET` | `/citas/agenda/view` | Vista FullCalendar. Params: `profesionalId` (opcional), `desde`, `hasta`. |

---

## Módulo de Sesiones — /sesiones

| Método | Ruta | Descripción |
|--------|------|-------------|
| `PATCH` | `/citas/{citaId}/atender` | Convertir cita PROGRAMADA → sesión activa. **Respuesta incluye `planResumen` embebido** (objetivos, sesiones restantes, costo). |
| `GET` | `/sesiones/{sesionId}` | Obtener sesión con nota SOAP y resumen del plan. |
| `GET` | `/episodios/{episodioId}/historial` | Todas las sesiones del episodio, ordenadas cronológicamente. |
| `PUT` | `/sesiones/{sesionId}/nota-soap` | Actualizar SOAP en borrador. |
| `PATCH` | `/sesiones/{sesionId}/firmar` | Firmar sesión (SHA-256, bloquea edición). **Incrementa `sesionesRealizadas` en el plan automáticamente.** |

---

## Módulo de Pacientes — /pacientes

| Método | Ruta | Descripción |
|--------|------|-------------|
| `POST` | `/pacientes` | Registrar paciente. |
| `GET` | `/pacientes` | Listar pacientes. |
| `GET` | `/pacientes/busqueda?q=` | Buscar por cédula / HCL / nombre. |
| `GET` | `/pacientes/{id}` | Detalle de paciente. |
| `PUT` | `/pacientes/{id}` | Editar datos. |
| `GET` | `/pacientes/{id}/resumen` | Resumen clínico completo. |
| `GET` | `/pacientes/{id}/contexto-agendamiento` | Episodios abiertos + planes activos + sesiones restantes. Alimenta el combobox de la agenda. |
| `GET` | `/pacientes/{id}/contexto-planes` | ★ NUEVO — Episodios ABIERTO/ADMITIDO con planes e indicador de avance. Panel central del dashboard de planes. |
| `POST` | `/pacientes/{id}/ficha-familiar` | Registrar / actualizar ficha familiar. |
| `GET` | `/pacientes/{id}/ficha-familiar` | Obtener ficha familiar. |
| `POST` | `/pacientes/archivo/actualizar` | Marcar pasivos (5 años sin atención). Solo ADMIN. |

---

## Módulo de Planes de Tratamiento — Vista Global — /planes-tratamiento

| Método | Ruta | Descripción |
|--------|------|-------------|
| `GET` | `/planes-tratamiento/estadisticas-dashboard` | ★ NUEVO — Contadores para filtros: `total`, `enRiesgo` (alarma NARANJA/ROJO), `finalizando` (≥80% sesiones). |
| `GET` | `/planes-tratamiento/resumen-pacientes` | ★ NUEVO — Columna izquierda del dashboard: pacientes con planes activos, `peorAlarma`, conteo. |
| `GET` | `/planes-tratamiento/{planId}/timeline` | ★ NUEVO — Línea de tiempo unificada: `SESION_SOAP` y `SEGUIMIENTO_PLAN` ordenados por fecha. |
| `POST` | `/planes-tratamiento/{planId}/iniciar-sesion-directa` | ★ NUEVO — Sesión sin agenda previa. Crea cita fantasma REALIZADA. Solo FISIOTERAPEUTA. |

Ver guía completa: [modulo-planes-tratamiento-frontend.md](./modulo-planes-tratamiento-frontend.md)

---

## Flujo de datos completo

```
POST /citas          →  guarda planTratamientoId
PATCH /.../atender   →  crea Sesion copiando planId + retorna planResumen
PUT  .../nota-soap   →  edita borrador SOAP
PATCH .../firmar     →  bloquea nota + sesionesRealizadas++
```

---

## Notas de seguridad

- Todos los endpoints requieren JWT (`Bearer`).
- `FISIOTERAPEUTA` solo gestiona su propia agenda.
- Sesión firmada es inmutable (hash SHA-256).
