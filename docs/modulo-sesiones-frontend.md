# Módulo de Sesiones de Terapia — Guía para Frontend

Base URL: `http://localhost:8080/api/v1`  
Autenticación: `Authorization: Bearer <token>` en todas las peticiones.  
Formato de fechas: ISO 8601 — `"2025-04-20T10:00:00"`

---

## Flujo completo de una sesión clínica

```
1. Atender cita         →  PATCH /citas/{id}/atender
2. Consultar progreso   →  GET  /sesiones/{id}/resumen-progreso
3. Editar nota SOAP     →  PUT  /sesiones/{id}/nota-soap
4. Subir adjuntos       →  POST /sesiones/{id}/adjuntos
5. Firmar sesión        →  PATCH /sesiones/{id}/firmar
6. Descargar PDF        →  GET  /sesiones/{id}/pdf
```

---

## 1. Atender cita → iniciar sesión

```
PATCH /citas/{citaId}/atender
```

**Sin body.** Convierte cita `PROGRAMADA` → sesión activa con nota SOAP en borrador.  
Es idempotente: si ya existe sesión para esa cita, retorna la existente.

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
- Guardar `sesionId = response.id` — se usará en todos los endpoints siguientes.
- Pintar barra lateral con `planResumen` inmediatamente (sin petición extra).
- Mostrar `"Sesión ${numeroSesionEnPlan} de ${planResumen.sesionesPlanificadas}"`.
- `notaSOAP.modoBorrador = true` → formulario SOAP habilitado para edición.

**Errores:**
| Código | Causa |
|--------|-------|
| `400` | Cita no está en estado `PROGRAMADA` |
| `403` | No es el fisioterapeuta asignado |

---

## 2. Obtener sesión

```
GET /sesiones/{sesionId}
```

Retorna la sesión completa con nota SOAP y resumen del plan embebidos. Mismo esquema que la respuesta de `/atender`.

**Roles:** `ADMINISTRADOR`, `FISIOTERAPEUTA`, `MEDICO`

---

## 3. Resumen de progreso

```
GET /sesiones/{sesionId}/resumen-progreso
```

**Propósito:** Mostrar al fisioterapeuta la comparativa de progreso antes de llenar la sección "A — Análisis" del SOAP.

**Roles:** `ADMINISTRADOR`, `FISIOTERAPEUTA`, `MEDICO`

**Respuesta `200`:**
```json
{
  "sesionId": 15,
  "numeroSesionActual": 5,
  "totalSesionesPlan": 10,
  "sesionesRealizadas": 4,
  "sesionesRestantes": 6,
  "evaluacionInicial": {
    "evaluacionId": 1,
    "fecha": "2025-03-01T09:00:00",
    "eva": 8,
    "puntajeFuncionalPromedio": 45.50,
    "interpretacionFuncional": "Limitación funcional moderada",
    "tipo": "INICIAL"
  },
  "evaluacionReciente": {
    "evaluacionId": 3,
    "fecha": "2025-04-10T10:00:00",
    "eva": 4,
    "puntajeFuncionalPromedio": 68.00,
    "interpretacionFuncional": "Mejoría significativa",
    "tipo": "INTERMEDIA"
  },
  "deltaEva": 4,
  "historialSesiones": [
    { "sesionId": 10, "numeroSesion": 1, "fecha": "2025-03-05T10:00:00", "estado": "FIRMADA" },
    { "sesionId": 11, "numeroSesion": 2, "fecha": "2025-03-10T10:00:00", "estado": "FIRMADA" },
    { "sesionId": 15, "numeroSesion": 5, "fecha": "2025-04-20T10:00:00", "estado": "EN_PROGRESO" }
  ]
}
```

**Campos clave:**
| Campo | Descripción |
|-------|-------------|
| `deltaEva` | EVA inicial − EVA reciente. **Positivo = mejoría**, negativo = deterioro, `null` si solo hay una evaluación |
| `evaluacionInicial` | Primera `EvaluacionFisicaEpisodio` del episodio (tipo `INICIAL`) |
| `evaluacionReciente` | Última evaluación registrada (puede ser `INTERMEDIA` o `ALTA`) |
| `historialSesiones` | Todas las sesiones del plan ordenadas por número |

**Uso en UI:**
- Mostrar badge `"EVA: 8 → 4 (−4 pts)"` en la barra lateral.
- Colorear `deltaEva`: verde si > 0, rojo si < 0, gris si null.
- Tabla de historial permite al fisio ver tendencia antes de escribir el Análisis.
- Si `evaluacionInicial = null` → no hay evaluación física registrada aún.

---

## 4. Editar nota SOAP (borrador)

```
PUT /sesiones/{sesionId}/nota-soap
Content-Type: application/json
```

**Solo disponible mientras `modoBorrador = true` (antes de firmar).**  
Campos son todos opcionales — enviar solo los que cambian.

**Body:**
```json
{
  "subjetivo": "Paciente refiere dolor lumbar 4/10, mejora con reposo",
  "objetivo": "ROM lumbar: flexión 55°, extensión 20°. EVA 4/10",
  "analisis": "Mejoría progresiva desde EVA 8 inicial. Alcanzando objetivos del plan",
  "plan": "Continuar con termoterapia + ejercicios de estabilización. Próxima sesión en 3 días"
}
```

**Respuesta `200`:**
```json
{
  "id": 20,
  "sesionTerapiaId": 15,
  "subjetivo": "Paciente refiere dolor lumbar 4/10, mejora con reposo",
  "objetivo": "ROM lumbar: flexión 55°, extensión 20°. EVA 4/10",
  "analisis": "Mejoría progresiva desde EVA 8 inicial. Alcanzando objetivos del plan",
  "plan": "Continuar con termoterapia + ejercicios de estabilización. Próxima sesión en 3 días",
  "modoBorrador": true,
  "firmadoPorId": null,
  "firmadoPorNombre": null,
  "firmadoEn": null,
  "hashIntegridad": null,
  "fechaCreacion": "2025-04-20T10:03:00",
  "fechaModificacion": "2025-04-20T10:45:00"
}
```

**Errores:**
| Código | Causa |
|--------|-------|
| `400` | Sesión ya firmada — inmutable |
| `403` | No es el fisioterapeuta asignado |

---

## 5. Gestión de adjuntos

### 5a. Subir adjunto

```
POST /sesiones/{sesionId}/adjuntos
Content-Type: multipart/form-data
```

**Form field:** `archivo` (el archivo)

**Tipos permitidos:** `application/pdf`, `image/jpeg`, `image/png`, `image/webp`  
**Tamaño máximo:** 10 MB  
**Restricción:** Solo mientras la sesión no esté firmada.

**Respuesta `201`:**
```json
{
  "id": 7,
  "sesionTerapiaId": 15,
  "nombreOriginal": "radiografia_lumbar.pdf",
  "tipoMime": "application/pdf",
  "tamanoBytes": 524288,
  "subidoPorId": 2,
  "subidoPorNombre": "Andrés Rodríguez",
  "fechaSubida": "2025-04-20T10:30:00",
  "urlDescarga": "/sesiones/15/adjuntos/7/descargar"
}
```

**Errores:**
| Código | Causa |
|--------|-------|
| `400` | Tipo MIME no permitido, archivo vacío, tamaño > 10 MB, sesión firmada |
| `403` | No es el fisioterapeuta asignado |

---

### 5b. Listar adjuntos

```
GET /sesiones/{sesionId}/adjuntos
```

**Roles:** `ADMINISTRADOR`, `FISIOTERAPEUTA`, `MEDICO`

**Respuesta `200`:** Array de `AdjuntoSesionResponse` (mismo esquema que el `201` de subida).

---

### 5c. Descargar adjunto

```
GET /sesiones/{sesionId}/adjuntos/{adjuntoId}/descargar
```

**Roles:** `ADMINISTRADOR`, `FISIOTERAPEUTA`, `MEDICO`  
**Respuesta:** Binario del archivo con `Content-Type` del archivo original y `Content-Disposition: attachment`.

**Uso en UI:**
```javascript
// Abrir descarga en nueva pestaña
window.open(`/api/v1/sesiones/${sesionId}/adjuntos/${adjuntoId}/descargar`)

// O para preview inline de imágenes: cambiar a GET y mostrar en <img src="...">
```

---

### 5d. Eliminar adjunto

```
DELETE /sesiones/{sesionId}/adjuntos/{adjuntoId}
```

**Elimina el archivo del disco y la base de datos.**  
Solo permitido antes de firmar.

**Respuesta:** `204 No Content`

**Errores:**
| Código | Causa |
|--------|-------|
| `400` | Sesión ya firmada |
| `403` | No es el fisioterapeuta asignado |
| `404` | Adjunto no encontrado en esa sesión |

---

## 6. Firmar sesión (cierre legal)

```
PATCH /sesiones/{sesionId}/firmar
```

**Sin body.**

**Prerequisitos — el backend valida:**
- Todos los campos SOAP completos (S, O, A, P no vacíos).
- Sesión en estado `EN_PROGRESO` (no `FIRMADA`).
- Actor es el fisioterapeuta asignado o `ADMINISTRADOR`.

**Efectos en cascada:**
1. `notaSOAP.modoBorrador` → `false` (inmutable para siempre)
2. `sesion.estado` → `FIRMADA`
3. `sesion.firmadoPorId` + `sesion.firmadoEn` → registrados
4. `sesion.hashIntegridad` → SHA-256 de `(S|O|A|P|firmadoEn)`
5. `planTratamiento.sesionesRealizadas` → `+1`

**Respuesta `200`:** Mismo esquema `SesionTerapiaResponse` con `estado: "FIRMADA"` y `hashIntegridad` populado.

**Errores:**
| Código | Causa |
|--------|-------|
| `400` | Campos SOAP incompletos, sesión ya firmada |
| `403` | No es el fisioterapeuta asignado |

**Uso en UI:**
- Deshabilitar botón "Firmar" si algún campo SOAP está vacío.
- Al recibir `200`: mostrar confirmación de firma, cambiar estado del formulario a solo lectura, habilitar botón "Descargar PDF".

---

## 7. Generar PDF

```
GET /sesiones/{sesionId}/pdf
```

**Solo disponible para sesiones en estado `FIRMADA`.**  
Retorna `application/pdf` con `Content-Disposition: attachment; filename="sesion-{id}.pdf"`.

**Contenido del PDF:**
- Encabezado con nombre de la clínica
- Datos del paciente (nombre, cédula)
- Datos del profesional y fecha de sesión
- Número de sesión en el plan
- Nota SOAP completa (S / O / A / P)
- Firma digital: nombre del firmante, fecha, hash SHA-256

**Uso en UI:**
```javascript
// Descarga directa
window.open(`/api/v1/sesiones/${sesionId}/pdf`)

// Fetch blob para preview
const res = await fetch(`/api/v1/sesiones/${sesionId}/pdf`, { headers: { Authorization: `Bearer ${token}` } })
const blob = await res.blob()
const url = URL.createObjectURL(blob)
window.open(url)
```

**Errores:**
| Código | Causa |
|--------|-------|
| `400` | Sesión aún no firmada |
| `404` | Sesión no encontrada |

---

## 8. Historial de sesiones del episodio

```
GET /episodios/{episodioId}/historial
```

**Roles:** `ADMINISTRADOR`, `FISIOTERAPEUTA`, `MEDICO`

Devuelve todas las sesiones del episodio ordenadas cronológicamente, cada una con su nota SOAP y `planResumen` embebidos.

**Uso en UI:** Panel de historial clínico lateral — timeline de sesiones del episodio.

---

## Enums de referencia

```
EstadoSesionTerapia:        EN_PROGRESO | FIRMADA
EstadoPlan:                 ACTIVO | COMPLETADO | ABANDONADO
TipoEvaluacionFisioterapia: INICIAL | INTERMEDIA | ALTA
```

---

## Reglas de negocio críticas

| Regla | Detalle |
|-------|---------|
| Inmutabilidad | Una vez `FIRMADA`, ningún campo de la nota SOAP puede editarse |
| Adjuntos bloqueados | No se pueden subir ni eliminar adjuntos en sesión `FIRMADA` |
| PDF solo firmado | `GET /pdf` lanza `400` si `estado != FIRMADA` |
| Firma requiere SOAP completo | Los 4 campos (S, O, A, P) deben tener contenido |
| `sesionesRealizadas++` | Solo al firmar, no al iniciar ni guardar borrador |
| Idempotencia en atender | Llamar `/atender` dos veces retorna la misma sesión |

---

## Errores globales

```json
{
  "timestamp": "2025-04-20T10:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "La nota SOAP debe tener todos los campos completos (S, O, A, P) antes de firmar."
}
```

| Código | Acción sugerida en UI |
|--------|----------------------|
| `400` | Mostrar `message` del body como toast de error |
| `401` | Redirigir a login |
| `403` | Mostrar "Sin permisos para esta acción" |
| `404` | Mostrar "Recurso no encontrado" |
| `500` | Mensaje genérico + reportar al soporte |
