# Documentacion de Endpoints - Fisiolab System

## Informacion general
- Base URL local: `http://localhost:8080/api/v1`
- Formato de respuesta: `application/json`
- Autenticacion: `Bearer JWT` (header `Authorization`)

## Swagger
- UI: `http://localhost:8080/api/v1/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/api/v1/v3/api-docs`

## Autenticacion

### POST /auth/login
Inicia sesion y retorna un token JWT.

Request body:
```json
{
  "email": "admin@fisiolab.com",
  "password": "TuPassword123"
}
```

Validaciones:
- `email`: obligatorio, formato email
- `password`: obligatorio

Response 200:
```json
{
  "token": "eyJhbGciOiJI...",
  "tokenType": "Bearer",
  "expiresIn": 1800000
}
```

Posibles codigos:
- `200 OK`
- `401 Unauthorized`

cURL:
```bash
curl -X POST "http://localhost:8080/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@fisiolab.com",
    "password": "TuPassword123"
  }'
```

### POST /auth/change-password
Cambia la contrasena del usuario autenticado.

Requiere header:
- `Authorization: Bearer <token>`

Request body:
```json
{
  "currentPassword": "PasswordActual123",
  "newPassword": "PasswordNueva123"
}
```

Validaciones:
- `currentPassword`: obligatorio
- `newPassword`: obligatorio

Response 200:
```json
"Contrasena cambiada exitosamente"
```

Posibles codigos:
- `200 OK`
- `401 Unauthorized`

cURL:
```bash
curl -X POST "http://localhost:8080/api/v1/auth/change-password" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "currentPassword": "PasswordActual123",
    "newPassword": "PasswordNueva123"
  }'
```

## Administracion de usuarios

### POST /admin/usuarios
Crea un usuario profesional (solo rol ADMINISTRADOR).

Requiere header:
- `Authorization: Bearer <token>`

Request body:
```json
{
  "cedula": "1234567890",
  "email": "fisio@fisiolab.com",
  "name": "Juan",
  "lastName": "Perez",
  "password": "TuPassword123",
  "rol": "FISIOTERAPEUTA",
  "especialidad": "Rehabilitacion deportiva",
  "tipoProfesional": "Fisioterapeuta",
  "codigoRegistro": "COL-FT-001"
}
```

Validaciones:
- `cedula`: obligatorio, max 20
- `email`: obligatorio, email valido, max 120
- `name`: obligatorio, max 120
- `lastName`: obligatorio, max 120
- `password`: obligatorio
- `rol`: obligatorio
- `especialidad`: opcional, max 120
- `tipoProfesional`: opcional, max 120
- `codigoRegistro`: opcional, max 120

Valores permitidos en `rol`:
- `ADMINISTRADOR`
- `FISIOTERAPEUTA`
- `MEDICO`

Response 200:
```json
{
  "id": 12,
  "cedula": "1234567890",
  "email": "fisio@fisiolab.com",
  "name": "Juan",
  "lastName": "Perez",
  "rol": "FISIOTERAPEUTA",
  "activo": true,
  "especialidad": "Rehabilitacion deportiva",
  "tipoProfesional": "Fisioterapeuta",
  "codigoRegistro": "COL-FT-001"
}
```

Posibles codigos:
- `200 OK`
- `403 Forbidden`
- `401 Unauthorized`

cURL:
```bash
curl -X POST "http://localhost:8080/api/v1/admin/usuarios" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "cedula": "1234567890",
    "email": "fisio@fisiolab.com",
    "name": "Juan",
    "lastName": "Perez",
    "password": "TuPassword123",
    "rol": "FISIOTERAPEUTA",
    "especialidad": "Rehabilitacion deportiva",
    "tipoProfesional": "Fisioterapeuta",
    "codigoRegistro": "COL-FT-001"
  }'
```

### GET /admin/usuarios
Lista todos los usuarios (solo rol ADMINISTRADOR).

Requiere header:
- `Authorization: Bearer <token>`

Response 200:
```json
[
  {
    "id": 1,
    "cedula": "1234567890",
    "email": "admin@fisiolab.com",
    "name": "Admin",
    "lastName": "Principal",
    "rol": "ADMINISTRADOR",
    "activo": true,
    "especialidad": null,
    "tipoProfesional": null,
    "codigoRegistro": null
  }
]
```

Posibles codigos:
- `200 OK`
- `403 Forbidden`
- `401 Unauthorized`

cURL:
```bash
curl -X GET "http://localhost:8080/api/v1/admin/usuarios" \
  -H "Authorization: Bearer <token>"
```

### PATCH /admin/usuarios/{id}/desactivar-bloqueo
Desactiva el tiempo de bloqueo temporal de login para el usuario indicado y reinicia intentos fallidos.

Requiere header:
- `Authorization: Bearer <token>`

Response 200:
```json
{
  "id": 12,
  "cedula": "1234567890",
  "email": "fisio@fisiolab.com",
  "name": "Juan",
  "lastName": "Perez",
  "rol": "FISIOTERAPEUTA",
  "activo": true,
  "especialidad": "Rehabilitacion deportiva",
  "tipoProfesional": "Fisioterapeuta",
  "codigoRegistro": "COL-FT-001"
}
```

Posibles codigos:
- `200 OK`
- `400 Bad Request` (usuario no encontrado)
- `403 Forbidden`
- `401 Unauthorized`

cURL:
```bash
curl -X PATCH "http://localhost:8080/api/v1/admin/usuarios/12/desactivar-bloqueo" \
  -H "Authorization: Bearer <token>"
```

## Pruebas

### GET /test/token
Genera un token de prueba con un usuario hardcodeado.

Response 200:
```text
eyJhbGciOiJIUzI1NiJ9...
```

cURL:
```bash
curl -X GET "http://localhost:8080/api/v1/test/token"
```

## Header de autenticacion
Ejemplo de uso para endpoints protegidos:

```http
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

## Tarjetero Indice Automatizado (MSP Ecuador)

### POST /pacientes
RF-07 + RF-09. Registra un paciente nuevo en el tarjetero indice y genera automaticamente el numero HCL secuencial (`HC-AAAA-00001`).

Requiere:
- `Authorization: Bearer <token>`
- Rol `ADMINISTRADOR` o `FISIOTERAPEUTA`

Request body:
```json
{
  "cedula": "1712345678",
  "email": "maria@correo.com",
  "nombresCompletos": "Maria Fernanda Mena",
  "fechaNacimiento": "1992-06-15",
  "genero": "FEMENINO",
  "grupoCultural": "Mestizo",
  "estadoCivil": "Soltera",
  "ocupacion": "Docente",
  "regimenSeguridadSocial": "IESS",
  "tipoSangre": "O+",
  "telefonoPrincipal": "0999999999",
  "telefonoSecundario": "022222222",
  "direccion": "Quito, Ecuador"
}
```

### GET /pacientes/busqueda?q={texto}
RF-08. Busca pacientes por cedula, numero HCL o nombres completos.

Reglas:
- Busqueda minima: 3 caracteres
- Devuelve maximo 30 resultados por consulta

Ejemplo:
```bash
curl -X GET "http://localhost:8080/api/v1/pacientes/busqueda?q=171"
```

### GET /pacientes/{id}
Obtiene el detalle del paciente por id interno.

### GET /pacientes
Lista todos los pacientes registrados (ordenados por fecha de registro descendente) para poblar la tabla principal del dashboard.

Ejemplo:
```bash
curl -X GET "http://localhost:8080/api/v1/pacientes" \
  -H "Authorization: Bearer <token>"
```

### POST /pacientes/archivo/actualizar
RF-11. Ejecuta la actualizacion automatica de estado de archivo (`ACTIVO` a `PASIVO`) para pacientes sin atencion en 5 anos.

Requiere rol `ADMINISTRADOR`.

Respuesta ejemplo:
```json
{
  "pacientesActualizados": 4
}
```

### PUT /pacientes/{id}
RF-10. Permite editar datos del paciente (administrador y fisioterapeuta), registrando auditoria.

Request body:
```json
{
  "cedula": "1712345678",
  "email": "maria@correo.com",
  "nombresCompletos": "Maria Fernanda Mena",
  "fechaNacimiento": "1992-06-15",
  "genero": "FEMENINO",
  "grupoCultural": "Mestizo",
  "estadoCivil": "Casada",
  "ocupacion": "Docente",
  "regimenSeguridadSocial": "IESS",
  "tipoSangre": "O+",
  "telefonoPrincipal": "0988888888",
  "telefonoSecundario": "022222222",
  "direccion": "Quito, Ecuador"
}
```

### POST /pacientes/{id}/ficha-familiar
RF-12. Registra o actualiza la ficha familiar del paciente (rol FISIOTERAPEUTA).

Request body:
```json
{
  "jefeHogar": "Carlos Mena",
  "numeroMiembros": 4,
  "tipoVivienda": "Casa propia",
  "condicionesSanitarias": "Agua potable, alcantarillado, recoleccion de basura"
}
```

### GET /pacientes/{id}/ficha-familiar
Obtiene la ficha familiar registrada de un paciente.

## Modulo 3 - Historia clinica y antecedentes

### POST /historias-clinicas/{numeroHcl}/antecedentes/personales
RF-14. Registra antecedentes personales (patologicos, quirurgicos, traumaticos, alergicos, farmacologicos y gineco-obstetricos).

Roles permitidos:
- `FISIOTERAPEUTA`
- `MEDICO`

Request body:
```json
{
  "tipo": "PATOLOGICO",
  "descripcion": "Hipertension arterial diagnosticada en 2018",
  "codigoCie10": "I10",
  "estado": "ACTIVO"
}
```

### POST /historias-clinicas/{numeroHcl}/antecedentes/familiares
RF-15. Registra antecedentes familiares con parentesco y condicion.

Roles permitidos:
- `FISIOTERAPEUTA`
- `MEDICO`

Request body:
```json
{
  "parentesco": "PADRE",
  "condicion": "Diabetes mellitus tipo 2",
  "codigoCie10": "E11"
}
```

### GET /historias-clinicas/{numeroHcl}/completa
RF-16. Visualizacion completa de la HC en una sola respuesta.

Incluye:
- Resumen de HC y paciente
- Antecedentes personales
- Antecedentes familiares
- Problemas activos
- Episodios previos (estructura reservada)

### GET /historias-clinicas/por-paciente/{pacienteId}
Devuelve el resumen de la historia clinica abierta automaticamente (RF-13) para un paciente.

### GET /historias-clinicas/depuracion/candidatas
RF-17. Lista historias candidatas a depuracion (pasivo + 15 anios sin atencion).

Rol permitido:
- `ADMINISTRADOR`

### DELETE /historias-clinicas/{numeroHcl}/depuracion
RF-17. Ejecuta depuracion controlada para una HC candidata.

Rol permitido:
- `ADMINISTRADOR`
