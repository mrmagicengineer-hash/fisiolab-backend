# Implementaciones Backend — FisioLab System

> Base URL: `http://localhost:8080/api/v1`  
> Autenticación: Bearer JWT requerido en todos los endpoints.

---

## 1. Evaluación Clínica Rápida — CRUD

**Propósito:** Evaluación simplificada de consulta (distinta de la evaluación física compleja con goniometría/escalas funcionales).

### Archivos creados

| Tipo | Archivo |
|------|---------|
| Entity | `model/EvaluacionClinica.java` → tabla `evaluaciones_clinicas` |
| Repository | `repository/EvaluacionClinicaRepository.java` |
| DTO request (crear) | `dto/CreateEvaluacionClinicaRequest.java` |
| DTO request (actualizar) | `dto/UpdateEvaluacionClinicaRequest.java` |
| DTO nested | `dto/SignosVitalesDto.java` |
| DTO response | `dto/EvaluacionClinicaResponse.java` |
| Service | `service/EvaluacionClinicaService.java` |
| Controller | `controller/EvaluacionClinicaController.java` |

### Endpoints

**Base:** `/episodios-clinicos/{episodioId}/evaluaciones-clinicas`

| Método | URL | Roles | Descripción |
|--------|-----|-------|-------------|
| `POST` | `/` | ADMIN, FISIOTERAPEUTA | Crear evaluación clínica |
| `GET` | `/` | ADMIN, FISIOTERAPEUTA, MEDICO | Listar por episodio (orden cronológico) |
| `GET` | `/{evaluacionId}` | ADMIN, FISIOTERAPEUTA, MEDICO | Obtener por ID |
| `PUT` | `/{evaluacionId}` | ADMIN, FISIOTERAPEUTA | Actualizar (PATCH parcial) |
| `DELETE` | `/{evaluacionId}` | ADMIN | Eliminar |

### Request body — POST/PUT

```json
{
  "fechaEvaluacion": "2026-04-19T10:30:00",
  "fisioterapeutaId": "FT-001",
  "signosVitales": {
    "pa": "120/80",
    "fc": 75
  },
  "motivoConsulta": "Dolor punzante en zona lumbar y limitación en tobillo",
  "observacionGeneral": "Marcha claudicante, evita apoyo en MID",
  "hallazgosPrincipales": [
    "Edema en maleolo externo izquierdo",
    "Test de cajón anterior positivo"
  ],
  "escalaEva": 8,
  "impresionDiagnostica": "Esguince grado II tobillo izquierdo + lumbalgia mecánica",
  "planInicial": "Crioterapia, AINES, reposo relativo 48h. Reevaluación en 3 días"
}
```

> `PUT` acepta todos los campos como opcionales (solo actualiza los que se envíen).

### Validaciones

| Campo | Regla |
|--
| `signosVitales.pa` | NotBlank, max 20 chars |
| `signosVitales.fc` | NotNull, 30–250 |
| `escalaEva` | NotNull, 0–10 |
| `hallazgosPrincipales` | NotEmpty, cada ítem max 300 chars |
| `motivoConsulta` | NotBlank, max 500 |
| `impresionDiagnostica` | NotBlank, max 500 |
| `planInicial` | NotBlank, max 1000 |
| `episodio` (validación service) | Debe existir y no estar CERRADO |

### Modelo de datos — Entity

```
evaluaciones_clinicas
├── id                       BIGINT PK AUTO_INCREMENT
├── episodio_clinico_id      BIGINT FK NOT NULL
├── fecha_evaluacion         DATETIME NOT NULL
├── fisioterapeuta_id        VARCHAR(50)
├── presion_arterial         VARCHAR(20)       ← signosVitales.pa
├── frecuencia_cardiaca      INT               ← signosVitales.fc
├── motivo_consulta          VARCHAR(500)
├── observacion_general      VARCHAR(500)
├── hallazgos_principales_json LONGTEXT        ← JSON array of strings
├── escala_eva               INT NOT NULL
├── impresion_diagnostica    VARCHAR(500)
├── plan_inicial             VARCHAR(1000)
└── creado_en                DATETIME NOT NULL
```

---

## 2. Cambios en Episodio Clínico y Plan de Tratamiento

### 2.1 CreateEpisodioClinicoRequest — campos renombrados

**Archivo:** `dto/CreateEpisodioClinicoRequest.java`

| Campo anterior | Campo nuevo | Notas |
|----------------|-------------|-------|
| `numeroHcl` | `pacienteId` | Sigue buscando HistoriaClinica por este valor |
| `motivoConsulta` | `motivo` | Mismo comportamiento |
| _(no existía)_ | `estado` | Opcional, informativo — siempre se crea como ABIERTO |

**Endpoint afectado:** `POST /episodios-clinicos`

```json
{
  "pacienteId": "EP-400",
  "motivo": "Caída en moto",
  "estado": "ABIERTO"
}
```

> `estado` se ignora en el servicio; el episodio siempre se crea con `EstadoEpisodioClinico.ABIERTO`.

### 2.2 CreatePlanTratamientoRequest — simplificado

**Archivo:** `dto/CreatePlanTratamientoRequest.java`

| Campo anterior | Campo nuevo | Notas |
|----------------|-------------|-------|
| `objetivoGeneral` + `objetivosEspecificos` | `objetivos` (String) | Se guarda en `objetivoGeneral`; `objetivosEspecificosJson = []` |
| `fechaInicio` | _(eliminado)_ | Default: `LocalDate.now()` |
| `fechaFinEstimada` | _(eliminado)_ | Default: `LocalDate.now().plusMonths(1)` |
| `codigoAlarma` | _(eliminado)_ | Default: `CodigoAlarma.VERDE` |
| `indicacionesEducativas` | _(eliminado)_ | Default: `null` |
| _(no existía)_ | `costoSesion` (BigDecimal) | Opcional, > 0 si se envía |

**Endpoint afectado:** `POST /episodios-clinicos/{episodioId}/problemas/{problemaId}/plan`

```json
{
  "objetivos": "Bajar EVA de 8 a 2, recuperar marcha",
  "sesionesPlanificadas": 10,
  "costoSesion": 25.00
}
```

### 2.3 Cambios en entidad PlanTratamiento

**Archivo:** `model/PlanTratamiento.java`

Campo añadido:

```java
@Column(precision = 10, scale = 2)
private BigDecimal costoSesion;
```

Columna en BD: `costo_sesion DECIMAL(10,2) NULL`

### 2.4 DTOs de respuesta actualizados

| Archivo | Cambio |
|---------|--------|
| `PlanTratamientoResponse.java` | Campo `costoSesion` añadido al final |
| `PlanTratamientoConSeguimientosResponse.java` | Campo `costoSesion` añadido |
| `EpisodioClinicoService.convertirPlanConSeguimientos()` | Pasa `planResponse.costoSesion()` al constructor |

---

## 3. Módulo 7 — Sesiones de Terapia y Notas SOAP

### Archivos creados

| Tipo | Archivo |
|------|---------|
| Enum | `model/EstadoSesionTerapia.java` |
| Entity | `model/SesionTerapia.java` → tabla `sesiones_terapia` |
| Entity | `model/NotaSOAP.java` → tabla `notas_soap` |
| Repository | `repository/SesionTerapiaRepository.java` |
| Repository | `repository/NotaSOAPRepository.java` |
| DTO response | `dto/SesionTerapiaResponse.java` |
| DTO response | `dto/NotaSOAPResponse.java` |
| DTO request | `dto/ActualizarNotaSOAPRequest.java` |
| Service | `service/SesionTerapiaService.java` |
| Controller | `controller/SesionTerapiaController.java` |

### Endpoints

| Método | URL | Roles | Descripción |
|--------|-----|-------|-------------|
| `PATCH` | `/citas/{citaId}/atender` | ADMIN, FISIOTERAPEUTA | **Trigger principal.** Convierte cita PROGRAMADA → REALIZADA, crea SesionTerapia + NotaSOAP en borrador |
| `GET` | `/sesiones/{sesionId}` | ADMIN, FISIOTERAPEUTA, MEDICO | Obtener sesión con nota SOAP embebida |
| `GET` | `/episodios/{episodioId}/historial` | ADMIN, FISIOTERAPEUTA, MEDICO | Historial cronológico de sesiones + notas SOAP del episodio |
| `PUT` | `/sesiones/{sesionId}/nota-soap` | ADMIN, FISIOTERAPEUTA | Actualizar campos S/O/A/P (solo en borrador) |
| `PATCH` | `/sesiones/{sesionId}/firmar` | ADMIN, FISIOTERAPEUTA | Firma, genera hash SHA-256, bloquea edición |

### Flujo completo

```
1. POST  /citas                        → Crear cita (estado: PROGRAMADA)
2. PATCH /citas/{id}/atender           → Iniciar sesión
      ├── cita.estado = REALIZADA
      ├── SesionTerapia creada (EN_PROGRESO)
      │     ├── hereda paciente, episodio, costo del PlanTratamiento
      │     └── numero_sesion_en_plan = max_anterior + 1
      ├── NotaSOAP creada (modo_borrador = true)
      └── cita.sesion_generada_id = sesion.id (trazabilidad)

3. PUT  /sesiones/{id}/nota-soap       → Rellenar S, O, A, P (borrador)
4. PATCH /sesiones/{id}/firmar         → Firma definitiva
      ├── Valida S/O/A/P completos
      ├── modo_borrador = false
      ├── hash_integridad = SHA-256(S|O|A|P|firmadoEn)
      ├── sesion.estado = FIRMADA
      └── Registro inmutable desde aquí
```

### Request — actualizar nota SOAP

```json
{
  "subjetivo": "Paciente refiere dolor lumbar 8/10, dificultad para caminar",
  "objetivo": "Edema maleolo externo, ROM dorsiflexión 5°, test cajón anterior +",
  "analisis": "Esguince grado II tobillo izquierdo + lumbalgia mecánica aguda",
  "plan": "Crioterapia 15min, AINES, reposo relativo 48h, próxima sesión jueves"
}
```

> Todos los campos son opcionales en PUT (actualización parcial).

### Modelo de datos — SesionTerapia

```
sesiones_terapia
├── id                    BIGINT PK AUTO_INCREMENT
├── cita_id               BIGINT FK NOT NULL UNIQUE    ← relación 1:1 forzada por UK
├── plan_tratamiento_id   BIGINT FK NULL
├── paciente_id           BIGINT FK NOT NULL
├── episodio_clinico_id   BIGINT FK NULL
├── profesional_id        BIGINT FK NOT NULL
├── costo_sesion          DECIMAL(10,2)
├── numero_sesion_en_plan INT
├── fecha_hora_inicio     DATETIME NOT NULL
├── estado                VARCHAR(20) NOT NULL         ← EN_PROGRESO | FINALIZADA | FIRMADA
├── firmado_por_id        BIGINT FK NULL
├── firmado_en            DATETIME NULL
├── hash_integridad       VARCHAR(64) NULL             ← SHA-256 hex
└── fecha_creacion        DATETIME NOT NULL
```

### Modelo de datos — NotaSOAP

```
notas_soap
├── id                  BIGINT PK AUTO_INCREMENT
├── sesion_terapia_id   BIGINT FK NOT NULL UNIQUE
├── subjetivo           TEXT NULL
├── objetivo            TEXT NULL
├── analisis            TEXT NULL
├── plan                TEXT NULL
├── modo_borrador       BOOLEAN NOT NULL DEFAULT TRUE
├── firmado_por_id      BIGINT FK NULL
├── firmado_en          DATETIME NULL
├── hash_integridad     VARCHAR(64) NULL
├── fecha_creacion      DATETIME NOT NULL
└── fecha_modificacion  DATETIME NOT NULL
```

### Garantías implementadas

| Garantía | Mecanismo |
|----------|-----------|
| Cita cancelada no genera sesión | `validarTransicionEstado` en `CitaService` bloquea CANCELADA/REALIZADA. `iniciarSesion` verifica `estado == PROGRAMADA` antes de proceder |
| Idempotencia en `/atender` | `sesionRepository.existsByCitaId()` — si ya existe, retorna la sesión existente sin crear duplicado |
| Control de cupos | Service cuenta `findMaxNumeroSesionByPlanId()` vs `sesionesPlanificadas`. Si excede: crea igual pero audita evento `CUPO_EXCEDIDO` |
| SOAP inmutable post-firma | `!nota.isModoBorrador()` → lanza `IllegalStateException` en actualización y firma |
| Campos SOAP completos antes de firmar | `validarCamposSOAPCompletos()` — verifica S, O, A, P no nulos/vacíos |
| Seguridad por rol en firma | Solo `profesional == cita.profesional` o `ADMINISTRADOR` puede firmar. `AccessDeniedException` si no cumple |
| Trazabilidad factura→sesión→cita→fisio | `SesionTerapia.cita.profesional` + `costoSesion` + `paciente` + `episodioClinico` en un solo objeto |

### Event Listener (fallback)

`SesionTerapiaService` tiene `@EventListener` sobre `CitaRealizadaEvent`:

- Se activa si alguien cambia estado de cita a REALIZADA vía el endpoint genérico `PATCH /citas/{id}/estado`
- Es **idempotente**: verifica `existsByCitaId()` antes de crear
- Garantiza que la sesión siempre se crea independientemente del camino usado

### Seguridad por rol — resumen

| Acción | FISIOTERAPEUTA | MEDICO | ADMINISTRADOR |
|--------|:--------------:|:------:|:-------------:|
| Atender cita | ✅ (solo sus citas) | ❌ | ✅ |
| Ver sesión/historial | ✅ | ✅ | ✅ |
| Editar nota SOAP | ✅ (solo su sesión) | ❌ | ✅ |
| Firmar sesión | ✅ (solo su sesión) | ❌ | ✅ |
| Eliminar evaluación clínica | ❌ | ❌ | ✅ |

---

## Resumen de tablas nuevas en BD

| Tabla | Módulo | Descripción |
|-------|--------|-------------|
| `evaluaciones_clinicas` | Módulo 6 | Evaluación rápida de consulta |
| `sesiones_terapia` | Módulo 7 | Sesión clínica originada de una cita |
| `notas_soap` | Módulo 7 | Nota clínica estructurada S/O/A/P |

> Hibernate crea/actualiza estas tablas automáticamente (`ddl-auto=update`).

## Columnas añadidas a tablas existentes

| Tabla | Columna | Tipo | Módulo |
|-------|---------|------|--------|
| `planes_tratamiento` | `costo_sesion` | `DECIMAL(10,2)` | Módulo 5 |
