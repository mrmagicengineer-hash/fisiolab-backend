# Módulo de Planes de Tratamiento — Guía para Frontend

Base URL: `http://localhost:8080/api/v1`  
Autenticación: `Authorization: Bearer <token>` en todas las peticiones.  
Roles requeridos: `FISIOTERAPEUTA`, `MEDICO`, `ADMINISTRADOR` (salvo donde se indique).

---

## Flujo de la vista "Dashboard de Planes"

```
1. Cargar estadísticas (píldoras superiores)  →  GET /planes-tratamiento/estadisticas-dashboard
2. Cargar columna izquierda (lista pacientes) →  GET /planes-tratamiento/resumen-pacientes
3. Click en paciente → panel central          →  GET /pacientes/{id}/contexto-planes
4. Click en plan → línea de tiempo            →  GET /planes-tratamiento/{planId}/timeline
5. Atender paciente sin cita previa           →  POST /planes-tratamiento/{planId}/iniciar-sesion-directa
```

---

## 1. Estadísticas del Dashboard

```
GET /planes-tratamiento/estadisticas-dashboard
```

Contadores para los botones filtro superiores ("En Riesgo", "Finalizando").

**Respuesta:**

```json
{
  "total": 15,
  "enRiesgo": 3,
  "finalizando": 2
}
```

| Campo | Criterio |
|---|---|
| `total` | Planes con `estado = ACTIVO` |
| `enRiesgo` | Planes ACTIVO con `codigoAlarma` = `NARANJA` o `ROJO` |
| `finalizando` | Planes ACTIVO con `sesionesRealizadas / sesionesPlanificadas >= 0.80` |

---

## 2. Resumen por Paciente (columna izquierda)

```
GET /planes-tratamiento/resumen-pacientes
```

Lista de pacientes que tienen al menos un plan ACTIVO. Ordenado alfabéticamente.

**Respuesta:**

```json
[
  {
    "pacienteId": 12,
    "pacienteNombre": "María López",
    "hcl": "HCL-0042",
    "peorAlarma": "ROJO",
    "conteoPlanesActivos": 2
  },
  {
    "pacienteId": 7,
    "pacienteNombre": "Juan Pérez",
    "hcl": "HCL-0018",
    "peorAlarma": "VERDE",
    "conteoPlanesActivos": 1
  }
]
```

| Campo | Descripción |
|---|---|
| `peorAlarma` | Peor código entre todos sus planes activos: `VERDE` < `AMARILLO` < `NARANJA` < `ROJO` |
| `conteoPlanesActivos` | Cantidad de planes ACTIVO del paciente |

**Uso sugerido:** color del punto indicador = `peorAlarma`. Badge = `conteoPlanesActivos`.

---

## 3. Contexto de Planes del Paciente (panel central)

```
GET /pacientes/{id}/contexto-planes
```

Información maestra del paciente al hacer click en la columna izquierda. Devuelve episodios ABIERTO/ADMITIDO con sus planes e indicador de avance.

**Respuesta:**

```json
{
  "pacienteId": 12,
  "pacienteNombre": "María López",
  "hcl": "HCL-0042",
  "episodios": [
    {
      "episodioId": 5,
      "numeroEpisodio": "EP-0005",
      "motivoConsulta": "Dolor lumbar crónico",
      "estado": "ABIERTO",
      "fechaApertura": "2025-03-10T08:30:00",
      "planes": [
        {
          "planId": 8,
          "problemaId": 3,
          "problemaDescripcion": "Lumbalgia mecánica L4-L5",
          "codigoCie10": "M54.5",
          "objetivoGeneral": "Reducir dolor EVA < 3 y recuperar rango de movimiento",
          "sesionesPlanificadas": 12,
          "sesionesRealizadas": 5,
          "porcentajeAvance": 41,
          "codigoAlarma": "AMARILLO",
          "estado": "ACTIVO",
          "fechaInicio": "2025-03-15",
          "fechaFinEstimada": "2025-04-15"
        }
      ]
    }
  ]
}
```

**Notas:**
- Solo incluye episodios en estado `ABIERTO` o `ADMITIDO`.
- `sesionesRealizadas` se calcula contando `SeguimientoPlan` registrados.
- `porcentajeAvance` = `min(100, sesionesRealizadas * 100 / sesionesPlanificadas)`.

---

## 4. Línea de Tiempo del Plan

```
GET /planes-tratamiento/{planId}/timeline
```

Lista cronológica de todo lo que ha ocurrido en el plan: sesiones SOAP y seguimientos. Ordenada por `fecha` ascendente.

**Respuesta:**

```json
[
  {
    "tipo": "SESION_SOAP",
    "itemId": 101,
    "fecha": "2025-03-17T10:00:00",
    "numeroSesion": 1,
    "resumen": "Sesión #1 — EN_PROGRESO",
    "estadoSesion": "EN_PROGRESO",
    "notaFirmada": false,
    "resultadoGeneral": null,
    "porcentajeAvance": null
  },
  {
    "tipo": "SESION_SOAP",
    "itemId": 104,
    "fecha": "2025-03-20T10:00:00",
    "numeroSesion": 2,
    "resumen": "Sesión #2 — FIRMADA",
    "estadoSesion": "FIRMADA",
    "notaFirmada": true,
    "resultadoGeneral": null,
    "porcentajeAvance": null
  },
  {
    "tipo": "SEGUIMIENTO_PLAN",
    "itemId": 22,
    "fecha": "2025-03-25T00:00:00",
    "numeroSesion": 2,
    "resumen": "Seguimiento #2 — MEJORA",
    "estadoSesion": null,
    "notaFirmada": null,
    "resultadoGeneral": "MEJORA",
    "porcentajeAvance": 40
  }
]
```

| Campo `tipo` | Campos relevantes |
|---|---|
| `SESION_SOAP` | `estadoSesion` (`EN_PROGRESO`, `FINALIZADA`, `FIRMADA`), `notaFirmada` |
| `SEGUIMIENTO_PLAN` | `resultadoGeneral` (`MEJORA`, `ESTABLE`, `DETERIORO`, `ALTA`, `ABANDONO`), `porcentajeAvance` |

**Uso sugerido:** ícono diferente por `tipo`. Línea verde si `notaFirmada=true` o `resultadoGeneral=MEJORA`.

---

## 5. Iniciar Sesión Directa (sin agenda)

```
POST /planes-tratamiento/{planId}/iniciar-sesion-directa
```

Crea una sesión para un paciente que llega de imprevisto, sin pasar por el flujo de citas. Solo `FISIOTERAPEUTA`.

**Sin body.** El profesional se toma del JWT (`authentication.getName()`).

**Respuesta:** misma estructura que `PATCH /citas/{citaId}/atender` — `SesionTerapiaResponse` con nota SOAP en borrador.

```json
{
  "id": 115,
  "citaId": 88,
  "planTratamientoId": 8,
  "pacienteId": 12,
  "pacienteNombre": "María López",
  "episodioClinicoId": 5,
  "profesionalId": 3,
  "profesionalNombre": "Andrés García",
  "numeroSesionEnPlan": 6,
  "fechaHoraInicio": "2026-04-19T15:32:00",
  "estado": "EN_PROGRESO",
  "notaSOAP": {
    "id": 115,
    "sesionTerapiaId": 115,
    "subjetivo": null,
    "objetivo": null,
    "analisis": null,
    "plan": null,
    "modoBorrador": true,
    "firmadoPor": null
  }
}
```

**Errores:**

| Código | Motivo |
|---|---|
| `400` | Plan no encontrado o `estado != ACTIVO` |
| `403` | Usuario no tiene rol `FISIOTERAPEUTA` |

**Flujo post-creación:**
```
POST /.../iniciar-sesion-directa   →  obtener sesionId
PUT  /sesiones/{sesionId}/nota-soap →  escribir SOAP
PATCH /sesiones/{sesionId}/firmar   →  firmar y cerrar
```

---

## Valores de enumeraciones

### CodigoAlarma (semáforo del plan)

| Valor | Color sugerido | Significado |
|---|---|---|
| `VERDE` | #22c55e | Sin riesgo |
| `AMARILLO` | #eab308 | Atención moderada |
| `NARANJA` | #f97316 | Riesgo alto |
| `ROJO` | #ef4444 | Crítico |

### EstadoPlan

| Valor | Descripción |
|---|---|
| `ACTIVO` | Plan en curso |
| `COMPLETADO` | Cerrado con alta o todas las sesiones completadas |
| `ABANDONADO` | Cerrado con resultado ABANDONO |

### ResultadoGeneral (seguimientos)

| Valor | Descripción |
|---|---|
| `MEJORA` | Paciente mejora |
| `ESTABLE` | Sin cambios |
| `DETERIORO` | Paciente empeora |
| `ALTA` | Cierra el plan como COMPLETADO |
| `ABANDONO` | Cierra el plan como ABANDONADO |

---

## Notas de seguridad

- Todos los endpoints requieren JWT (`Bearer`).
- `iniciar-sesion-directa` solo accesible para `FISIOTERAPEUTA`.
- Los demás endpoints permiten `FISIOTERAPEUTA`, `MEDICO`, `ADMINISTRADOR`.
- Una `NotaSOAP` firmada (`modoBorrador=false`) es inmutable — hash SHA-256 garantiza integridad.
