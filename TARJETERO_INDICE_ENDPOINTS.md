# Tarjetero Indice - Endpoints detallados

## Informacion general
- Base URL local: `http://localhost:8080/api/v1`
- Prefijo del modulo: `/pacientes`
- Formato: `application/json`
- Autenticacion: JWT Bearer en header `Authorization`

Header de ejemplo:
```http
Authorization: Bearer <token>
```

## Modelo de datos principal

### PacienteResponse
```json
{
  "id": 12,
  "numeroHcl": "HC-2026-00001",
  "cedula": "1712345678",
  "email": "paciente@correo.com",
  "nombresCompletos": "Maria Fernanda Mena",
  "fechaNacimiento": "1994-03-10",
  "genero": "FEMENINO",
  "grupoCultural": "Mestizo",
  "estadoCivil": "SOLTERA",
  "ocupacion": "Docente",
  "regimenSeguridadSocial": "IESS",
  "tipoSangre": "O+",
  "telefonoPrincipal": "0999999999",
  "telefonoSecundario": "022345678",
  "direccion": "Quito, Pichincha",
  "estadoArchivo": "ACTIVO",
  "fechaRegistro": "2026-04-07T09:30:00",
  "fechaUltimaAtencion": "2026-04-07T11:00:00"
}
```

### FichaFamiliarResponse
```json
{
  "id": 4,
  "pacienteId": 12,
  "jefeHogar": "Carlos Mena",
  "numeroMiembros": 4,
  "tipoVivienda": "Casa propia",
  "condicionesSanitarias": "Agua potable y alcantarillado",
  "fechaActualizacion": "2026-04-07T11:20:00"
}
```

## Errores comunes
Las validaciones y reglas de negocio retornan errores con esta estructura:

```json
{
  "timestamp": "2026-04-07T17:35:42.123Z",
  "status": 400,
  "error": "Bad Request",
  "message": "La busqueda requiere minimo 3 caracteres.",
  "path": "/api/v1/pacientes/busqueda"
}
```

Codigos frecuentes:
- `200 OK`: operacion exitosa
- `400 Bad Request`: validacion o regla de negocio incumplida
- `401 Unauthorized`: token ausente o invalido
- `403 Forbidden`: rol sin permisos para la accion

---

## 1) POST /pacientes
Registra un paciente nuevo y genera automaticamente su numero HCL.

Permisos:
- `ADMINISTRADOR`
- `FISIOTERAPEUTA`

Body (CreatePacienteRequest):
```json
{
  "cedula": "1712345678",
  "email": "paciente@correo.com",
  "nombresCompletos": "Maria Fernanda Mena",
  "fechaNacimiento": "1994-03-10",
  "genero": "FEMENINO",
  "grupoCultural": "Mestizo",
  "estadoCivil": "SOLTERA",
  "ocupacion": "Docente",
  "regimenSeguridadSocial": "IESS",
  "tipoSangre": "O+",
  "telefonoPrincipal": "0999999999",
  "telefonoSecundario": "022345678",
  "direccion": "Quito, Pichincha"
}
```

Validaciones:
- `cedula`: obligatorio, max 20
- `email`: obligatorio, formato email, max 120
- `nombresCompletos`: obligatorio, max 160
- `fechaNacimiento`: obligatoria, debe ser fecha pasada
- `genero`: obligatorio, max 30
- `grupoCultural`: opcional, max 80
- `estadoCivil`: opcional, max 60
- `ocupacion`: opcional, max 100
- `regimenSeguridadSocial`: opcional, max 100
- `tipoSangre`: opcional, max 10
- `telefonoPrincipal`: obligatorio, max 20
- `telefonoSecundario`: opcional, max 20
- `direccion`: opcional, max 255

Reglas de negocio:
- Cedula unica
- Correo unico
- Se abre automaticamente la historia clinica del paciente
- `estadoArchivo` inicial: `ACTIVO`

Response 200:
- `PacienteResponse`

cURL:
```bash
curl -X POST "http://localhost:8080/api/v1/pacientes" \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "cedula": "1712345678",
    "email": "paciente@correo.com",
    "nombresCompletos": "Maria Fernanda Mena",
    "fechaNacimiento": "1994-03-10",
    "genero": "FEMENINO",
    "grupoCultural": "Mestizo",
    "estadoCivil": "SOLTERA",
    "ocupacion": "Docente",
    "regimenSeguridadSocial": "IESS",
    "tipoSangre": "O+",
    "telefonoPrincipal": "0999999999",
    "telefonoSecundario": "022345678",
    "direccion": "Quito, Pichincha"
  }'
```

---

## 2) GET /pacientes/busqueda?q={texto}
Busca pacientes por cedula, numero HCL o nombres completos.

Permisos:
- Usuario autenticado (cualquier rol)

Query params:
- `q` (obligatorio): texto de busqueda

Reglas de negocio:
- `q` debe tener minimo 3 caracteres
- Retorna maximo 30 resultados

Response 200:
```json
[
  {
    "id": 12,
    "numeroHcl": "HC-2026-00001",
    "cedula": "1712345678",
    "email": "paciente@correo.com",
    "nombresCompletos": "Maria Fernanda Mena",
    "fechaNacimiento": "1994-03-10",
    "genero": "FEMENINO",
    "grupoCultural": "Mestizo",
    "estadoCivil": "SOLTERA",
    "ocupacion": "Docente",
    "regimenSeguridadSocial": "IESS",
    "tipoSangre": "O+",
    "telefonoPrincipal": "0999999999",
    "telefonoSecundario": null,
    "direccion": "Quito, Pichincha",
    "estadoArchivo": "ACTIVO",
    "fechaRegistro": "2026-04-07T09:30:00",
    "fechaUltimaAtencion": "2026-04-07T11:00:00"
  }
]
```

cURL:
```bash
curl -X GET "http://localhost:8080/api/v1/pacientes/busqueda?q=maria" \
  -H "Authorization: Bearer <token>"
```

---

## 3) GET /pacientes
Lista todos los pacientes registrados en orden descendente por fecha de registro.

Permisos:
- Usuario autenticado (cualquier rol)

Response 200:
- `List<PacienteResponse>`

cURL:
```bash
curl -X GET "http://localhost:8080/api/v1/pacientes" \
  -H "Authorization: Bearer <token>"
```

---

## 4) GET /pacientes/{id}
Obtiene el detalle de un paciente por su id interno.

Permisos:
- Usuario autenticado (cualquier rol)

Path params:
- `id` (obligatorio): id numerico del paciente

Response 200:
- `PacienteResponse`

Errores frecuentes:
- `400`: `Paciente no encontrado: {id}`

cURL:
```bash
curl -X GET "http://localhost:8080/api/v1/pacientes/12" \
  -H "Authorization: Bearer <token>"
```

---

## 5) PUT /pacientes/{id}
Edita los datos de la ficha del paciente.

Permisos:
- `ADMINISTRADOR`
- `FISIOTERAPEUTA`

Path params:
- `id` (obligatorio): id numerico del paciente

Body (UpdatePacienteRequest):
```json
{
  "cedula": "1712345678",
  "email": "paciente@correo.com",
  "nombresCompletos": "Maria Fernanda Mena Actualizada",
  "fechaNacimiento": "1994-03-10",
  "genero": "FEMENINO",
  "grupoCultural": "Mestizo",
  "estadoCivil": "CASADA",
  "ocupacion": "Docente",
  "regimenSeguridadSocial": "IESS",
  "tipoSangre": "O+",
  "telefonoPrincipal": "0999999999",
  "telefonoSecundario": "022345678",
  "direccion": "Quito, Pichincha"
}
```

Validaciones:
- Mismas de `CreatePacienteRequest`

Reglas de negocio:
- El paciente debe existir
- Cedula y correo deben seguir siendo unicos (no pueden duplicar a otro paciente)

Response 200:
- `PacienteResponse`

cURL:
```bash
curl -X PUT "http://localhost:8080/api/v1/pacientes/12" \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "cedula": "1712345678",
    "email": "paciente@correo.com",
    "nombresCompletos": "Maria Fernanda Mena Actualizada",
    "fechaNacimiento": "1994-03-10",
    "genero": "FEMENINO",
    "grupoCultural": "Mestizo",
    "estadoCivil": "CASADA",
    "ocupacion": "Docente",
    "regimenSeguridadSocial": "IESS",
    "tipoSangre": "O+",
    "telefonoPrincipal": "0999999999",
    "telefonoSecundario": "022345678",
    "direccion": "Quito, Pichincha"
  }'
```

---

## 6) POST /pacientes/{id}/ficha-familiar
Registra o actualiza la ficha familiar del paciente.

Permisos:
- `FISIOTERAPEUTA`

Path params:
- `id` (obligatorio): id numerico del paciente

Body (FichaFamiliarRequest):
```json
{
  "jefeHogar": "Carlos Mena",
  "numeroMiembros": 4,
  "tipoVivienda": "Casa propia",
  "condicionesSanitarias": "Agua potable y alcantarillado"
}
```

Validaciones:
- `jefeHogar`: obligatorio, max 160
- `numeroMiembros`: obligatorio, entero positivo
- `tipoVivienda`: obligatorio, max 80
- `condicionesSanitarias`: obligatorio, max 255

Reglas de negocio:
- Si el paciente no tiene ficha, se crea
- Si ya existe, se actualiza

Response 200:
- `FichaFamiliarResponse`

cURL:
```bash
curl -X POST "http://localhost:8080/api/v1/pacientes/12/ficha-familiar" \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "jefeHogar": "Carlos Mena",
    "numeroMiembros": 4,
    "tipoVivienda": "Casa propia",
    "condicionesSanitarias": "Agua potable y alcantarillado"
  }'
```

---

## 7) GET /pacientes/{id}/ficha-familiar
Obtiene la ficha familiar registrada de un paciente.

Permisos:
- Usuario autenticado (cualquier rol)

Path params:
- `id` (obligatorio): id numerico del paciente

Response 200:
- `FichaFamiliarResponse`

Errores frecuentes:
- `400`: `Ficha familiar no encontrada para paciente: {id}`

cURL:
```bash
curl -X GET "http://localhost:8080/api/v1/pacientes/12/ficha-familiar" \
  -H "Authorization: Bearer <token>"
```

---

## 8) POST /pacientes/archivo/actualizar
Ejecuta el cambio automatico de pacientes de `ACTIVO` a `PASIVO` por inactividad.

Permisos:
- `ADMINISTRADOR`

Regla de negocio:
- Marca como `PASIVO` pacientes con `fechaUltimaAtencion` mayor o igual a 5 anos de antiguedad.

Response 200:
```json
{
  "pacientesActualizados": 3
}
```

cURL:
```bash
curl -X POST "http://localhost:8080/api/v1/pacientes/archivo/actualizar" \
  -H "Authorization: Bearer <token>"
```

---

## Notas tecnicas
- El endpoint de busqueda usa una pagina fija de 30 resultados.
- La normalizacion aplicada en backend hace `trim` a textos y `lowercase` al email.
- El numero HCL se genera con formato: `HC-{ANIO}-{SECUENCIA_5_DIGITOS}`.
