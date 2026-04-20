# 📊 Documentación de Endpoints: Evaluaciones Físicas y Planes de Tratamiento

## Tabla de Contenidos
- [Evaluaciones Físicas](#evaluaciones-físicas)
- [Planes de Tratamiento](#planes-de-tratamiento)
- [Notas Importantes](#notas-importantes)

---

## EVALUACIONES FÍSICAS

### 1️⃣ RF-23 a RF-26: Registrar Evaluación Física

**Endpoint:** `POST /episodios-clinicos/{episodioId}/evaluaciones`

**Descripción:** Registra una evaluación física (inicial, intermedia o de alta) con medidas clínicas, goniometría, fuerza muscular y escalas funcionales.

**Autenticación:** Bearer Token (Requerido)  
**Rol Requerido:** `FISIOTERAPEUTA`

#### Parámetros Path
- `episodioId` (Long): ID del episodio clínico

#### Body (JSON)
```json
{
  "tipoEvaluacion": "INICIAL",
  "fechaEvaluacion": "2026-04-14T10:30:00",
  "frecuenciaCardiaca": "72 bpm",
  "frecuenciaRespiratoria": "16 rpm",
  "presionArterial": "120/80 mmHg",
  "saturacionOxigeno": "98%",
  "tallaCm": 175.5,
  "pesoKg": 75.0,
  "eva": 5,
  "localizacionDolor": "Región lumbar baja",
  "tipoDolor": "Dolor mecánico, punzante",
  "examenFisicoSegmentario": "Limitación en flexión anterior del tronco, tensión muscular en región lumbar baja",
  "diagnosticosPresuntivos": "Lumbago crónico, contractura muscular",
  "goniometria": [
    {
      "articulacion": "Cadera derecha",
      "plano": "Flexión",
      "rangoMovimientoGrados": 110.0
    },
    {
      "articulacion": "Cadera izquierda",
      "plano": "Flexión",
      "rangoMovimientoGrados": 115.0
    }
  ],
  "fuerzaMuscular": [
    {
      "grupoMuscular": "Cuádriceps derecho",
      "puntajeDaniels": 4
    },
    {
      "grupoMuscular": "Cuádriceps izquierdo",
      "puntajeDaniels": 4
    }
  ],
  "escalasFuncionales": [
    {
      "nombreEscala": "TIMED UP AND GO (TUG)",
      "puntajeObtenido": 14.5,
      "puntajeMaximo": 30.0
    },
    {
      "nombreEscala": "OSWESTRY DISABILITY INDEX",
      "puntajeObtenido": 35.0,
      "puntajeMaximo": 100.0
    }
  ],
  "pruebasEspeciales": [
    {
      "nombre": "Prueba de Lasegue",
      "resultado": "POSITIVO",
      "observacion": "Dolor radicular a 60 grados"
    },
    {
      "nombre": "Prueba de Schober",
      "resultado": "NEGATIVO",
      "observacion": "Flexibilidad espinal normal"
    }
  ]
}
```

#### Campos Principales

| Campo | Tipo | Requerido | Validación | Descripción |
|-------|------|-----------|-----------|-------------|
| `tipoEvaluacion` | Enum | ✅ | INICIAL, INTERMEDIA, ALTA | Tipo de evaluación |
| `fechaEvaluacion` | LocalDateTime | ❌ | - | Fecha y hora de la evaluación |
| `frecuenciaCardiaca` | String | ❌ | Max 60 chars | Ej: "72 bpm" |
| `frecuenciaRespiratoria` | String | ❌ | Max 60 chars | Ej: "16 rpm" |
| `presionArterial` | String | ❌ | Max 60 chars | Ej: "120/80 mmHg" |
| `saturacionOxigeno` | String | ❌ | Max 60 chars | Ej: "98%" |
| `tallaCm` | BigDecimal | ✅ | 30-250 | Talla en centímetros |
| `pesoKg` | BigDecimal | ✅ | 1-500 | Peso en kilogramos |
| `eva` | Integer | ✅ | 0-10 | Escala Visual Análoga (EVA) del dolor |
| `localizacionDolor` | String | ✅ | Max 255 chars | Ubicación del dolor |
| `tipoDolor` | String | ✅ | Max 120 chars | Tipo de dolor (mecánico, radicular, etc) |
| `examenFisicoSegmentario` | String | ✅ | Max 2000 chars | Descripción del examen físico |
| `diagnosticosPresuntivos` | String | ✅ | Max 2000 chars | Diagnósticos presuntivos |
| `goniometria` | Array | ✅ | Mínimo 1 | Mediciones de rango de movimiento |
| `fuerzaMuscular` | Array | ✅ | Mínimo 1 | Evaluación de fuerza muscular |
| `escalasFuncionales` | Array | ✅ | Mínimo 1 | Escalas funcionales aplicadas |
| `pruebasEspeciales` | Array | ✅ | Mínimo 1 | Pruebas especiales realizadas |

#### Sub-objetos

**Goniometría:**
```json
{
  "articulacion": "Nombre de la articulación",
  "plano": "Plano de movimiento (Flexión, Extensión, etc)",
  "rangoMovimientoGrados": 90.0
}
```

**Fuerza Muscular:**
```json
{
  "grupoMuscular": "Nombre del grupo muscular",
  "puntajeDaniels": 4
}
```
*Nota: Escala de Daniels (0-5)*

**Escalas Funcionales:**
```json
{
  "nombreEscala": "TUG, OSWESTRY, etc",
  "puntajeObtenido": 14.5,
  "puntajeMaximo": 30.0
}
```

**Pruebas Especiales:**
```json
{
  "nombre": "Nombre de la prueba",
  "resultado": "POSITIVO|NEGATIVO|DUDOSO",
  "observacion": "Observaciones adicionales"
}
```

#### Response (200 OK)
```json
{
  "id": 1,
  "episodioClinicoId": 5,
  "numeroEvaluacion": 1,
  "tipoEvaluacion": "INICIAL",
  "fechaEvaluacion": "2026-04-14T10:30:00",
  "frecuenciaCardiaca": "72 bpm",
  "frecuenciaRespiratoria": "16 rpm",
  "presionArterial": "120/80 mmHg",
  "saturacionOxigeno": "98%",
  "tallaCm": 175.5,
  "pesoKg": 75.0,
  "imc": 24.3,
  "eva": 5,
  "localizacionDolor": "Región lumbar baja",
  "tipoDolor": "Dolor mecánico, punzante",
  "examenFisicoSegmentario": "Limitación en flexión anterior del tronco",
  "diagnosticosPresuntivos": "Lumbago crónico",
  "goniometria": [...],
  "fuerzaMuscular": [...],
  "escalasFuncionales": [...],
  "pruebasEspeciales": [...],
  "puntajeFuncionalPromedio": 24.5,
  "interpretacionFuncional": "Funcionalidad moderadamente limitada"
}
```

#### Códigos de Error
- `400` - Datos inválidos, episodio no encontrado
- `403` - Acceso denegado (requiere rol FISIOTERAPEUTA)

---

### 2️⃣ Listar Evaluaciones del Episodio

**Endpoint:** `GET /episodios-clinicos/{episodioId}/evaluaciones`

**Descripción:** Devuelve todas las evaluaciones físicas del episodio en orden cronológico.

**Rol Permitido:** `ADMINISTRADOR`, `FISIOTERAPEUTA`, `MEDICO`

#### Response (200 OK)
```json
[
  {
    "id": 1,
    "episodioClinicoId": 5,
    "numeroEvaluacion": 1,
    "tipoEvaluacion": "INICIAL",
    "fechaEvaluacion": "2026-04-14T10:30:00",
    ...
  }
]
```

---

### 3️⃣ Obtener Evaluación Física

**Endpoint:** `GET /episodios-clinicos/{episodioId}/evaluaciones/{evaluacionId}`

**Descripción:** Obtiene el detalle completo de una evaluación física.

**Parámetros Path:**
- `episodioId` (Long): ID del episodio clínico
- `evaluacionId` (Long): ID de la evaluación

**Rol Permitido:** `ADMINISTRADOR`, `FISIOTERAPEUTA`, `MEDICO`

#### Response (200 OK)
Igual al response de crear evaluación.

---

### 4️⃣ RF-27: Comparativa con Evaluación Inicial

**Endpoint:** `GET /episodios-clinicos/{episodioId}/evaluaciones/{evaluacionId}/comparativa`

**Descripción:** Calcula la comparativa de EVA y escalas funcionales respecto a la evaluación inicial.

**Parámetros Path:**
- `episodioId` (Long): ID del episodio clínico
- `evaluacionId` (Long): ID de la evaluación a comparar

**Rol Permitido:** `ADMINISTRADOR`, `FISIOTERAPEUTA`, `MEDICO`

#### Response (200 OK)
```json
{
  "evaluacionIdInicial": 1,
  "evaluacionIdActual": 3,
  "diferenciasEVA": -2,
  "diferenciasEscales": 5.5,
  "mejoro": true,
  "porcentajeMejora": 15.5
}
```

---

### 5️⃣ RF-28: Obtener Puntos de Progreso

**Endpoint:** `GET /episodios-clinicos/{episodioId}/evaluaciones/progreso`

**Descripción:** Devuelve la serie temporal de EVA y puntaje funcional para graficar la evolución.

**Parámetros Path:**
- `episodioId` (Long): ID del episodio clínico

**Rol Permitido:** `ADMINISTRADOR`, `FISIOTERAPEUTA`, `MEDICO`

#### Response (200 OK)
```json
[
  {
    "numeroEvaluacion": 1,
    "fechaEvaluacion": "2026-04-14T10:30:00",
    "eva": 5,
    "puntajeFuncional": 24.5
  },
  {
    "numeroEvaluacion": 2,
    "fechaEvaluacion": "2026-05-14T14:00:00",
    "eva": 3,
    "puntajeFuncional": 28.5
  }
]
```

---

## PLANES DE TRATAMIENTO

### 1️⃣ RF-31: Crear Plan de Tratamiento

**Endpoint:** `POST /episodios-clinicos/{episodioId}/problemas/{problemaId}/plan`

**Descripción:** Crea un plan de tratamiento para un problema activo del episodio.

**Autenticación:** Bearer Token (Requerido)  
**Rol Requerido:** `FISIOTERAPEUTA`

#### Parámetros Path
- `episodioId` (Long): ID del episodio clínico
- `problemaId` (Long): ID del problema activo

#### Body (JSON)
```json
{
  "objetivoGeneral": "Mejorar la movilidad articular y reducir el dolor lumbar crónico",
  "objetivosEspecificos": [
    "Aumentar rango de movimiento de cadera en 20 grados",
    "Fortalecer musculatura estabilizadora lumbar",
    "Reducir EVA de 5 a 2 en 4 semanas",
    "Mejorar funcionalidad en actividades diarias"
  ],
  "fechaInicio": "2026-04-14",
  "fechaFinEstimada": "2026-06-14",
  "sesionesPlanificadas": 12,
  "indicacionesEducativas": "Realizar ejercicios en casa 3 veces por semana, mantener postura correcta, evitar cargas pesadas",
  "codigoAlarma": "VERDE"
}
```

#### Campos

| Campo | Tipo | Requerido | Validación | Descripción |
|-------|------|-----------|-----------|-------------|
| `objetivoGeneral` | String | ✅ | Max 500 chars | Objetivo general del tratamiento |
| `objetivosEspecificos` | Array | ✅ | Mínimo 1 | Lista de objetivos específicos |
| `fechaInicio` | LocalDate | ✅ | - | Fecha de inicio del plan |
| `fechaFinEstimada` | LocalDate | ✅ | - | Fecha estimada de finalización |
| `sesionesPlanificadas` | Integer | ✅ | ≥ 1 | Número de sesiones planificadas |
| `indicacionesEducativas` | String | ❌ | - | Indicaciones educativas al paciente |
| `codigoAlarma` | Enum | ✅ | VERDE, AMARILLO, ROJO | Código de alarma (semáforo) |

#### Response (200 OK)
```json
{
  "id": 5,
  "episodioClinicoId": 10,
  "problemaId": 3,
  "objetivoGeneral": "Mejorar la movilidad articular y reducir el dolor lumbar crónico",
  "objetivosEspecificos": [
    "Aumentar rango de movimiento de cadera en 20 grados",
    "Fortalecer musculatura estabilizadora lumbar",
    "Reducir EVA de 5 a 2 en 4 semanas",
    "Mejorar funcionalidad en actividades diarias"
  ],
  "fechaInicio": "2026-04-14",
  "fechaFinEstimada": "2026-06-14",
  "sesionesPlanificadas": 12,
  "sesionesRealizadas": 0,
  "porcentajeAvance": 0,
  "indicacionesEducativas": "Realizar ejercicios...",
  "codigoAlarma": "VERDE",
  "estado": "ABIERTO",
  "fechaCreacion": "2026-04-14T10:30:00"
}
```

#### Códigos de Error
- `400` - Ya existe un plan activo, datos inválidos o problema no encontrado
- `403` - Acceso denegado

---

### 2️⃣ Obtener Plan de Tratamiento

**Endpoint:** `GET /episodios-clinicos/{episodioId}/problemas/{problemaId}/plan`

**Descripción:** Devuelve el plan de tratamiento con sesiones realizadas y porcentaje de avance.

**Parámetros Path:**
- `episodioId` (Long): ID del episodio clínico
- `problemaId` (Long): ID del problema

**Rol Permitido:** `FISIOTERAPEUTA`, `MEDICO`, `ADMINISTRADOR`

#### Response (200 OK)
Igual al response de crear plan.

---

### 3️⃣ RF-32: Registrar Seguimiento/Evaluación del Plan

**Endpoint:** `POST /episodios-clinicos/{episodioId}/problemas/{problemaId}/plan/seguimientos`

**Descripción:** Registra una evaluación periódica del plan indicando avance, resultados y ajustes.

**Autenticación:** Bearer Token (Requerido)  
**Rol Requerido:** `FISIOTERAPEUTA`

#### Parámetros Path
- `episodioId` (Long): ID del episodio clínico
- `problemaId` (Long): ID del problema

#### Body (JSON)
```json
{
  "fechaSeguimiento": "2026-04-21",
  "porcentajeAvance": 25,
  "resultadosObtenidos": "Paciente tolera bien las sesiones, mejora en rango de movimiento de cadera",
  "ajustes": "Aumentar intensidad de ejercicios de fortalecimiento",
  "resultadoGeneral": "MEJORA"
}
```

#### Campos

| Campo | Tipo | Requerido | Validación | Descripción |
|-------|------|-----------|-----------|-------------|
| `fechaSeguimiento` | LocalDate | ✅ | - | Fecha del seguimiento |
| `porcentajeAvance` | Integer | ✅ | 0-100 | Porcentaje de avance del plan |
| `resultadosObtenidos` | String | ✅ | No vacío | Resultados logrados |
| `ajustes` | String | ❌ | - | Ajustes al plan |
| `resultadoGeneral` | Enum | ✅ | MEJORA, ESTABLE, DETERIORO, ALTA, ABANDONO | Estado del plan |

#### Response (200 OK)
```json
{
  "id": 1,
  "planTratamientoId": 5,
  "numeroSesion": 1,
  "fechaSeguimiento": "2026-04-21",
  "porcentajeAvance": 25,
  "resultadosObtenidos": "Paciente tolera bien las sesiones",
  "ajustes": "Aumentar intensidad",
  "resultadoGeneral": "MEJORA",
  "fechaRegistro": "2026-04-21T10:00:00"
}
```

#### Códigos de Error
- `400` - Plan no encontrado o estado inválido
- `403` - Acceso denegado

---

### 4️⃣ Listar Seguimientos del Plan

**Endpoint:** `GET /episodios-clinicos/{episodioId}/problemas/{problemaId}/plan/seguimientos`

**Descripción:** Devuelve el historial de evaluaciones ordenadas por número de sesión.

**Parámetros Path:**
- `episodioId` (Long): ID del episodio clínico
- `problemaId` (Long): ID del problema

**Rol Permitido:** `FISIOTERAPEUTA`, `MEDICO`, `ADMINISTRADOR`

#### Response (200 OK)
```json
[
  {
    "id": 1,
    "planTratamientoId": 5,
    "numeroSesion": 1,
    "fechaSeguimiento": "2026-04-21",
    "porcentajeAvance": 25,
    "resultadosObtenidos": "Paciente tolera bien las sesiones",
    "ajustes": null,
    "resultadoGeneral": "MEJORA",
    "fechaRegistro": "2026-04-21T10:00:00"
  },
  {
    "id": 2,
    "planTratamientoId": 5,
    "numeroSesion": 2,
    "fechaSeguimiento": "2026-04-28",
    "porcentajeAvance": 50,
    "resultadosObtenidos": "Mejoría en rango de movimiento",
    "ajustes": "Aumentar intensidad de ejercicios",
    "resultadoGeneral": "MEJORA",
    "fechaRegistro": "2026-04-28T14:30:00"
  }
]
```

---

### 5️⃣ RF-33: Indicador de Avance de Sesiones

**Endpoint:** `GET /episodios-clinicos/{episodioId}/problemas/{problemaId}/plan/indicador`

**Descripción:** Devuelve el indicador visual del plan con sesiones realizadas vs planificadas.

**Parámetros Path:**
- `episodioId` (Long): ID del episodio clínico
- `problemaId` (Long): ID del problema

**Rol Permitido:** `FISIOTERAPEUTA`, `MEDICO`, `ADMINISTRADOR`

#### Response (200 OK)
```json
{
  "sesionesRealizadas": 3,
  "sesionesPlanificadas": 12,
  "indicador": "3/12",
  "porcentajeAvance": 25,
  "estado": "ABIERTO",
  "resultadoUltimo": "MEJORA"
}
```

---

## NOTAS IMPORTANTES

### Validaciones
✅ Todos los campos requeridos deben estar presentes  
✅ Las fechas deben ser válidas (formato ISO 8601)  
✅ El porcentaje de avance debe estar entre 0 y 100  
✅ EVA debe estar entre 0 y 10  
✅ La Escala de Daniels (fuerza muscular) va de 0 a 5  

### Comportamientos Especiales
⚠️ El plan se cierra automáticamente si el resultado es `ALTA` o `ABANDONO`  
⚠️ El IMC se calcula automáticamente en evaluaciones  
⚠️ El número de evaluación se asigna secuencialmente  
⚠️ El número de sesión en seguimientos se incrementa automáticamente  

### Autenticación
🔐 Todos los endpoints requieren Bearer Token válido  
🔐 Los permisos se validan según el rol del usuario  

### Tipos de Evaluación
- `INICIAL` - Evaluación inicial del episodio
- `INTERMEDIA` - Evaluación de seguimiento
- `ALTA` - Evaluación de alta/cierre

### Códigos de Alarma
- `VERDE` - Evolución favorable
- `AMARILLO` - Evolución moderada
- `ROJO` - Evolución desfavorable

### Resultados Generales del Plan
- `MEJORA` - Mejora evidente
- `ESTABLE` - Sin cambios significativos
- `DETERIORO` - Empeoramiento
- `ALTA` - Paciente dado de alta
- `ABANDONO` - Paciente abandonó el tratamiento

### Resultados de Pruebas Especiales
- `POSITIVO` - Prueba positiva
- `NEGATIVO` - Prueba negativa
- `DUDOSO` - Resultado dudoso/inconcluyente
