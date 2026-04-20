# Módulo de Agendamiento de Citas — Guía para Frontend

Base URL: `http://localhost:8080/api/v1`  
Autenticación: `Authorization: Bearer <token>` en todas las peticiones.  
Formato de fechas: ISO 8601 — `"2025-04-20T10:00:00"`

---

## Flujo completo de agendamiento

```
1. Buscar paciente          →  GET /pacientes/busqueda?q=...
2. Cargar contexto clínico  →  GET /pacientes/{id}/contexto-agendamiento
3. Verificar disponibilidad →  GET /citas/disponibilidad?...
4. Crear la cita            →  POST /citas
5. Ver agenda               →  GET /citas/agenda/view?...
6. Atender cita             →  PATCH /citas/{id}/atender
```

---

## 1. Buscar paciente

```
GET /pacientes/busqueda?q={texto}
```

**Propósito:** Poblar el selector de paciente al abrir el formulario de nueva cita.  
**Mínimo:** 3 caracteres. Busca por nombre, cédula o número HCL.

**Respuesta exitosa `200`:**
```json
[
  {
    "id": 1,
    "numeroHcl": "HC-2025-00001",
    "cedula": "1712345678",
    "nombresCompletos": "Juan Pérez",
    "estadoArchivo": "ACTIVO"
  }
]
```

> ⚠️ Si `estadoArchivo = "PASIVO"`, el backend rechazará la cita con `400`. Mostrar advertencia antes de intentar agendar.

---

## 2. Cargar contexto clínico del paciente

```
GET /pacientes/{pacienteId}/contexto-agendamiento
```

**Propósito:** Obtener los episodios abiertos y los planes activos del paciente para poblar los combos "Episodio" y "Plan" del formulario.

**Respuesta exitosa `200`:**
```json
{
  "pacienteId": 1,
  "pacienteNombre": "Juan Pérez",
  "episodiosAbiertos": [
    {
      "episodioId": 5,
      "numeroEpisodio": "HC-2025-00001-EP02",
      "motivoConsulta": "Lumbalgia crónica",
      "estadoEpisodio": "ABIERTO",
      "planes": [
        {
          "planId": 3,
          "objetivoGeneral": "Reducir dolor y recuperar movilidad lumbar",
          "sesionesPlanificadas": 10,
          "sesionesRealizadas": 4,
          "sesionesRestantes": 6,
          "costoSesion": 25.00,
          "estadoPlan": "ACTIVO"
        }
      ]
    }
  ]
}
```

**Uso en UI:**
- Si `episodiosAbiertos` está vacío → mostrar aviso: "Este paciente no tiene episodios abiertos. La cita se creará sin contexto clínico."
- Cada episodio con sus planes → combo anidado o dos selects dependientes.
- Mostrar `sesionesRestantes` como badge junto al plan: `"6 / 10 sesiones restantes"`.
- Si `sesionesRestantes = 0` → advertir cupo agotado (se puede agendar igualmente).

---

## 3. Verificar disponibilidad del profesional

```
GET /citas/disponibilidad?profesionalId={id}&desde={datetime}&hasta={datetime}
```

**Propósito:** Validar en tiempo real que el profesional no tenga otra cita o bloqueo en el horario elegido. Llamar al cambiar la fecha/hora en el formulario (debounce recomendado: 400ms).

**Parámetros:**
| Param | Tipo | Ejemplo |
|-------|------|---------|
| `profesionalId` | Long | `2` |
| `desde` | ISO DateTime | `2025-04-20T10:00:00` |
| `hasta` | ISO DateTime | `2025-04-20T11:00:00` |

**Respuesta `200`:**
```json
{ "disponible": true }
```
o
```json
{ "disponible": false }
```

> Si `disponible = false` → deshabilitar botón "Guardar" y mostrar error inline en el campo de hora.

---

## 4. Crear cita

```
POST /citas
Content-Type: application/json
```

**Body:**
```json
{
  "pacienteId": 1,
  "profesionalId": 2,
  "fechaHoraInicio": "2025-04-20T10:00:00",
  "fechaHoraFin": "2025-04-20T11:00:00",
  "motivoConsulta": "Control lumbalgia",
  "codigoCie10Sugerido": "M54.5",
  "observaciones": "Paciente con historial de cirugía",
  "episodioClinicoId": 5,
  "planTratamientoId": 3
}
```

**Campos obligatorios:** `pacienteId`, `profesionalId`, `fechaHoraInicio`, `fechaHoraFin`, `motivoConsulta`.  
**Campos opcionales:** `codigoCie10Sugerido` (máx 10 chars), `observaciones` (máx 1000 chars), `episodioClinicoId`, `planTratamientoId`.

**Regla:** Si se envía `planTratamientoId`, también se debe enviar `episodioClinicoId`. El backend valida que el plan pertenezca al episodio.

**Respuesta exitosa `201`:**
```json
{
  "id": 42,
  "pacienteId": 1,
  "pacienteNombres": "Juan Pérez",
  "pacienteCedula": "1712345678",
  "profesionalId": 2,
  "profesionalNombre": "Andrés Rodríguez",
  "creadoPorId": 2,
  "creadoPorNombre": "Andrés Rodríguez",
  "fechaHoraInicio": "2025-04-20T10:00:00",
  "fechaHoraFin": "2025-04-20T11:00:00",
  "estado": "PROGRAMADA",
  "motivoConsulta": "Control lumbalgia",
  "codigoCie10Sugerido": "M54.5",
  "observaciones": "Paciente con historial de cirugía",
  "episodioClinicoId": 5,
  "planTratamientoId": 3,
  "sesionGeneradaId": null,
  "fechaCreacion": "2025-04-19T08:30:00",
  "fechaModificacion": "2025-04-19T08:30:00"
}
```

**Errores posibles:**
| Código | Causa |
|--------|-------|
| `400` | Solapamiento de horario, paciente PASIVO, plan no pertenece al episodio, fechas inválidas |
| `403` | FISIOTERAPEUTA intenta agendar en la agenda de otro profesional |

---

## 5. Ver agenda (FullCalendar)

```
GET /citas/agenda/view?desde={datetime}&hasta={datetime}&profesionalId={id}
```

**Propósito:** Alimentar el componente de calendario. Devuelve citas y bloqueos unificados.  
`profesionalId` es opcional para ADMINISTRADOR. FISIOTERAPEUTA siempre ve solo la suya.

**Parámetros:**
| Param | Tipo | Requerido |
|-------|------|-----------|
| `desde` | ISO DateTime | Sí |
| `hasta` | ISO DateTime | Sí |
| `profesionalId` | Long | No (ADMIN) / ignorado (FISIO) |

**Respuesta `200` — array de eventos:**
```json
[
  {
    "id": "cita-42",
    "title": "Juan Pérez — Control lumbalgia",
    "start": "2025-04-20T10:00:00",
    "end": "2025-04-20T11:00:00",
    "color": "#3B82F6",
    "tipo": "CITA",
    "extendedProps": {
      "estado": "PROGRAMADA",
      "pacienteId": 1,
      "profesionalNombre": "Andrés Rodríguez",
      "citaId": 42
    }
  },
  {
    "id": "bloqueo-7",
    "title": "VACACIONES — Andrés Rodríguez",
    "start": "2025-04-21T08:00:00",
    "end": "2025-04-21T18:00:00",
    "color": "#EF4444",
    "tipo": "BLOQUEO",
    "extendedProps": {
      "motivo": "VACACIONES",
      "profesionalId": 2,
      "descripcion": ""
    }
  }
]
```

**Colores por estado de cita:**
| Estado | Color |
|--------|-------|
| `PROGRAMADA` | `#3B82F6` (azul) |
| `REALIZADA` | `#10B981` (verde) |
| `CANCELADA` | `#6B7280` (gris) |
| `NO_ASISTIDA` | `#F59E0B` (amarillo) |
| `BLOQUEO` | `#EF4444` (rojo) |

**Uso con FullCalendar:**
- Al hacer click en evento `CITA` → leer `extendedProps.citaId` para abrir el panel lateral.
- Al hacer click en evento `BLOQUEO` → mostrar info de solo lectura.

---

## 6. Cambiar estado de cita

```
PATCH /citas/{citaId}/estado
Content-Type: application/json
```

**Body:**
```json
{
  "nuevoEstado": "CANCELADA",
  "observaciones": "Paciente solicitó cancelación"
}
```

**Estados válidos:** `PROGRAMADA`, `REALIZADA`, `CANCELADA`, `NO_ASISTIDA`

**Transiciones permitidas:**
```
PROGRAMADA  →  REALIZADA | CANCELADA | NO_ASISTIDA
NO_ASISTIDA →  PROGRAMADA  (reprogramar)
REALIZADA   →  ❌ inmutable
CANCELADA   →  ❌ inmutable
```

> Al transicionar a `REALIZADA` desde este endpoint, el backend crea automáticamente la `SesionTerapia` en segundo plano (evento interno). Preferir usar `PATCH /citas/{id}/atender` para el flujo clínico.

---

## 7. Atender cita → iniciar sesión clínica

```
PATCH /citas/{citaId}/atender
```

**Propósito:** Botón "Atender" en la agenda. Convierte la cita en sesión activa y abre el formulario SOAP.

**Sin body.**

**Respuesta `200`:**
```json
{
  "id": 15,
  "citaId": 42,
  "planTratamientoId": 3,
  "pacienteId": 1,
  "pacienteNombre": "Juan Pérez",
  "episodioClinicoId": 5,
  "profesionalId": 2,
  "profesionalNombre": "Andrés Rodríguez",
  "costoSesion": 25.00,
  "numeroSesionEnPlan": 5,
  "fechaHoraInicio": "2025-04-20T10:00:00",
  "estado": "EN_PROGRESO",
  "firmadoPorId": null,
  "firmadoEn": null,
  "hashIntegridad": null,
  "fechaCreacion": "2025-04-20T10:03:00",
  "notaSOAP": {
    "id": 20,
    "sesionTerapiaId": 15,
    "subjetivo": null,
    "objetivo": null,
    "analisis": null,
    "plan": null,
    "modoBorrador": true,
    "firmadoPorId": null,
    "firmadoPorNombre": null,
    "firmadoEn": null,
    "hashIntegridad": null
  },
  "planResumen": {
    "id": 3,
    "objetivoGeneral": "Reducir dolor y recuperar movilidad lumbar",
    "objetivosEspecificos": ["Flexión lumbar >60°", "EVA < 3"],
    "indicacionesEducativas": "Evitar cargas mayores a 5kg",
    "sesionesPlanificadas": 10,
    "sesionesRealizadas": 4,
    "costoSesion": 25.00,
    "estado": "ACTIVO"
  }
}
```

**Uso en UI:**
- Guardar `sesionId = response.id` para las siguientes llamadas SOAP.
- Pintar inmediatamente la barra lateral con `planResumen` (sin petición extra).
- Mostrar `"Sesión ${numeroSesionEnPlan} de ${planResumen.sesionesPlanificadas}"`.
- `notaSOAP.modoBorrador = true` → formulario SOAP editable.

---

## Listar citas

```
GET /citas
```
Retorna array de `CitaResponse`. FISIOTERAPEUTA solo ve las suyas, ADMINISTRADOR las ve todas.

```
GET /citas/{id}
```
Retorna `CitaResponse` de una cita específica.

---

## Enums de referencia

```
EstadoCita:     PROGRAMADA | REALIZADA | CANCELADA | NO_ASISTIDA
EstadoPlan:     ACTIVO | COMPLETADO | ABANDONADO
EstadoEpisodio: ABIERTO | ADMITIDO | CERRADO
EstadoSesion:   EN_PROGRESO | FIRMADA
```

---

## Errores globales

| Código | Significado | Acción sugerida en UI |
|--------|-------------|----------------------|
| `400` | Validación o regla de negocio violada | Mostrar `message` del body |
| `401` | Token expirado o ausente | Redirigir a login |
| `403` | Sin permisos | Mostrar "Acceso denegado" |
| `404` | Recurso no encontrado | Mostrar "No encontrado" |
| `500` | Error interno | Mostrar mensaje genérico + log |

**Formato de error:**
```json
{
  "timestamp": "2025-04-19T08:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "El profesional ya tiene una cita programada en ese horario."
}
```
