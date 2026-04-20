# Dashboard UX - Pacientes y HC

## Objetivo UX
- Encontrar paciente en segundos.
- Registrar paciente nuevo con HCL automatico.
- Ver y actualizar estado clinico y ficha familiar sin friccion.

## Arquitectura de pantalla
- Header fijo: busqueda global, boton nuevo paciente, rol activo.
- Sidebar: Inicio, Pacientes, Archivo pasivo, Auditoria, Reportes.
- Panel central: tabla/lista, ficha o formularios.
- Panel derecho contextual: resumen rapido del paciente seleccionado.

## Tabla principal de pacientes
Fuente de datos backend:
- Endpoint: `GET /api/v1/pacientes`

Columnas:
- HCL
- Cedula
- Nombres completos
- Estado archivo (badge)
- Ultima atencion
- Fecha registro
- Acciones (Ver, Editar, HC, Ficha familiar)

Comportamiento:
- Fila clickeable para abrir panel derecho.
- Orden por fecha de registro descendente (backend ya ordena).
- Busqueda en tiempo real con `GET /api/v1/pacientes/busqueda?q=`.
- Debounce 300 ms.
- Estados de UI: loading skeleton, vacio, error accionable.

## Flujo 1 - Busqueda en tiempo real (RF-08)
- Input global: "Buscar por cedula, HCL o nombres".
- Minimo 3 caracteres.
- Enter abre primer resultado.
- Flechas navegan resultados.

## Flujo 2 - Registro rapido (RF-07 + RF-09)
- Drawer lateral ancho.
- Bloques: Identificacion, Datos personales, Contacto, Sociocultural.
- Guardado exitoso: toast + chip con HCL generado.
- CTA: "Ir a ficha del paciente".

## Flujo 3 - Ficha editable (RF-10)
- Header: nombre, HCL destacado, estado ACTIVO/PASIVO.
- Tabs: Resumen, Datos personales, Ficha familiar, Historial HC.
- Modo lectura por defecto, editar explicitamente.
- Confirmacion para cambios criticos (cedula, nombre, telefono).

## Flujo 4 - Ficha familiar (RF-12)
- Campos: jefe hogar, numero miembros, tipo vivienda, condiciones sanitarias.
- Botones: Guardar y continuar, Guardar y volver.
- Mostrar ultima actualizacion.

## Flujo 5 - Estado de archivo (RF-11)
- Badge con tooltip explicativo en ficha.
- Vista admin para accion manual de actualizacion de pasivos.
- Mostrar resumen de afectados al ejecutar.

## Flujo 6 - Modulo 3 HC y antecedentes
- Apertura HC automatica al registrar paciente (RF-13).
- Registro de antecedentes personales/familiares desde la ficha HC (RF-14 y RF-15).
- Vista completa HC con problemas activos (RF-16).
- Depuracion controlada para admin (RF-17).

## Sistema visual recomendado
- Tipografia: Sora (titulos) + Source Sans 3 (contenido).
- Paleta: base clara, azul petroleo, verde clinico, acentos ambar.
- Jerarquia: HCL como pill visual protagonista.
- Motion: transiciones de 180ms y entrada escalonada de resultados.

## Microcopy
- "Escribe al menos 3 caracteres"
- "Paciente creado. HCL asignada: HC-2026-00021"
- "Este paciente esta en archivo pasivo por inactividad mayor a 5 anos"
