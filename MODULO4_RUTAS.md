# Modulo 4 - Rutas

Base URL: /api/v1

## Episodios clinicos y admision

- POST /episodios-clinicos
- POST /episodios-clinicos/{episodioId}/admision
- POST /episodios-clinicos/{episodioId}/egreso
- GET /episodios-clinicos/historial/{numeroHcl}
- POST /episodios-clinicos/{episodioId}/cierre

## JSON de ejemplo

### 1) POST /episodios-clinicos
Request:
```json
{
  "numeroHcl": "HC-2026-00001",
  "motivoConsulta": "Dolor lumbar agudo",
  "codigoCie10DiagnosticoPrincipal": "M54.5"
}
```

### 2) POST /episodios-clinicos/{episodioId}/admision
Request:
```json
{
  "fechaHoraAdmision": "2026-04-06T15:00:00",
  "tipoAtencion": "CONSULTA_EXTERNA",
  "motivoAtencion": "Evaluacion inicial",
  "profesionalAtiende": "Lic. Ana Perez"
}
```

### 3) POST /episodios-clinicos/{episodioId}/egreso
Request:
```json
{
  "fechaHoraEgreso": "2026-04-06T16:30:00",
  "condicionSalida": "Estable",
  "causaAlta": "Mejora clinica",
  "destinoPaciente": "Domicilio",
  "referidoOtraInstitucion": false
}
```

### 4) GET /episodios-clinicos/historial/{numeroHcl}
Response (200):
```json
[
  {
    "id": 12,
    "historiaClinicaId": 45,
    "numeroHcl": "HC-2026-00001",
    "numeroSecuencial": 2,
    "numeroEpisodio": "EP-0002",
    "motivoConsulta": "Dolor lumbar agudo",
    "codigoCie10DiagnosticoPrincipal": "M54.5",
    "fechaApertura": "2026-04-06T15:00:00",
    "fechaCierre": null,
    "estado": "ABIERTO",
    "estadoCierre": null,
    "observacionCierre": null
  }
]
```

### 5) POST /episodios-clinicos/{episodioId}/cierre
Request:
```json
{
  "estadoCierre": "COMPLETADO",
  "observacionCierre": "Paciente completa plan terapeutico"
}
```
