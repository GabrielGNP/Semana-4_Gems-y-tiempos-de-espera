# Historias de Usuario Refinadas (INVEST)

Este documento contiene las Historias de Usuario del Sistema de Gestión de Quejas ISP, refinadas bajo estándares de calidad QA y metodologías ágiles, listas para el desarrollo (Definition of Ready).

---

## 1. HU-01: Visualización de Listado de Tickets con Paginación

### Narrativa Estándar
**Como** Operador del Centro de Servicios,
**Quiero** visualizar el listado de tickets de forma paginada y ordenada,
**Para** navegar eficientemente a través de grandes volúmenes de incidentes sin afectar el rendimiento del sistema.

### Criterios de Aceptación (Escenarios Gherkin)

**Escenario 1: Navegación básica (Happy Path)**
**Dado que** existen 50 tickets registrados en el sistema
**Y** la configuración de paginación es de 10 elementos por página
**Cuando** el operador accede al dashboard de tickets
**Entonces** el sistema muestra los primeros 10 tickets ordenados por fecha de creación descendente
**Y** se visualizan los controles de navegación indicando "Página 1 de 5".

**Escenario 2: Cambio de tamaño de página**
**Dado que** el operador está visualizando la lista de tickets
**Cuando** cambia el selector de "Items por página" de 10 a 20
**Entonces** la lista se actualiza mostrando 20 tickets
**Y** el número total de páginas se recalcula correctamente.

**Escenario 3: Lista vacía**
**Dado que** no existen tickets en el sistema o no hay coincidencias con los filtros actuales
**Cuando** el operador accede al listado
**Entonces** el sistema muestra un mensaje informativo "No se encontraron tickets"
**Y** los controles de paginación se deshabilitan.

### Notas de Implementación/QA
- **Rendimiento:** La carga de cada página no debe superar los 1000ms.
- **Persistencia:** Si el usuario navega al detalle de un ticket y regresa, debe volver a la misma página y ordenamiento donde estaba.
- **Accesibilidad:** Los controles de paginación deben ser navegables vía teclado (Tab).

---

## 2. HU-06: Búsqueda Unitaria de Ticket por ID

### Narrativa Estándar
**Como** Operador del Centro de Servicios,
**Quiero** buscar un ticket específico ingresando su ID exacto,
**Para** acceder rápidamente al detalle de un caso reportado por un cliente.

### Criterios de Aceptación (Escenarios Gherkin)

**Escenario 1: Búsqueda exitosa (Happy Path)**
**Dado que** existe un ticket con ID "550e8400-e29b-41d4-a716-446655440000"
**Cuando** el operador ingresa este ID en el campo de búsqueda y confirma
**Entonces** el sistema redirige a la vista de detalle de ese ticket específico.

**Escenario 2: Ticket no encontrado**
**Dado que** el operador ingresa un ID con formato válido (UUIDv4) que no existe en la base de datos
**Cuando** ejecuta la búsqueda
**Entonces** el sistema muestra un mensaje de error "Ticket no encontrado".

**Escenario 3: Validación de formato inválido**
**Dado que** el operador ingresa un texto que no cumple el formato UUIDv4 (ej. "ticket-123")
**Cuando** intenta ejecutar la búsqueda
**Entonces** el sistema muestra un error de validación "Formato de ID inválido"
**Y** no se realiza la petición al servidor (validación en frontend).

### Notas de Implementación/QA
- **Validación:** El ID debe cumplir estrictamente con el estándar UUID v4.
- **Seguridad:** Se debe sanitizar la entrada para evitar inyecciones, aunque sea una búsqueda por ID.

---

## 3. HU-13: Gestión de Cambio de Estado de Tickets

### Narrativa Estándar
**Como** Administrador del Sistema,
**Quiero** cambiar el estado de un ticket (entre RECEIVED e IN_PROGRESS) directamente desde el listado,
**Para** actualizar el progreso de la resolución de forma ágil sin navegar a pantallas adicionales.

### Criterios de Aceptación (Escenarios Gherkin)

**Escenario 1: Cambio de estado exitoso (Happy Path)**
**Dado que** el administrador visualiza un ticket con estado "RECEIVED" en la lista
**Cuando** selecciona la opción "Cambiar Estado" y elige "IN_PROGRESS" en el modal de confirmación
**Entonces** el sistema actualiza el estado del ticket a "IN_PROGRESS"
**Y** se muestra una notificación de éxito "Estado actualizado exitosamente"
**Y** la lista refleja el nuevo estado sin recargar toda la página.

**Escenario 2: Cancelación del cambio**
**Dado que** el modal de cambio de estado está abierto
**Cuando** el administrador hace clic en el botón "Cancelar"
**Entonces** el modal se cierra
**Y** no se envía ninguna solicitud de actualización al servidor.

**Escenario 3: Intento de transición inválida**
**Dado que** un ticket se encuentra en un estado final (ej. "CLOSED" si aplicara en el futuro)
**Cuando** el administrador intenta cambiar el estado
**Entonces** el sistema deshabilita la opción de cambio o muestra un error indicando que la transición no es permitida.

**Escenario 4: Idempotencia en el cambio**
**Dado que** un ticket ya tiene el estado "IN_PROGRESS"
**Cuando** el administrador intenta cambiarlo nuevamente a "IN_PROGRESS"
**Entonces** el sistema mantiene el estado actual
**Y** notifica que el ticket ya se encuentra en ese estado (o procesa la solicitud exitosamente sin cambios efectivos).

**Escenario 5: Error de servidor**
**Dado que** ocurre un fallo en la conexión con la base de datos
**Cuando** el administrador confirma el cambio de estado
**Entonces** el sistema mantiene el modal abierto
**Y** muestra un mensaje de error descriptivo "No se pudo actualizar el ticket. Intente nuevamente".

### Notas de Implementación/QA
- **Endpoint:** Se debe utilizar `PATCH /api/tickets/:ticketId/status`.
- **Performance:** La respuesta del endpoint debe ser menor a 500ms (Percentil 95).
- **UX:** El cambio de estado requiere confirmación explícita (Modal) para evitar clics accidentales.
- **Reglas de Negocio:** Solo se permiten transiciones entre `RECEIVED` <-> `IN_PROGRESS` para este alcance.