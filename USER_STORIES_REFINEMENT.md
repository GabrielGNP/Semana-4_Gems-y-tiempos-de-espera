# Historias de Usuario Refinadas (Análisis INVEST - Gema A (L))

Este documento presenta las Historias de Usuario del Sistema de Gestión de Quejas ISP, refinadas para cumplir con los principios INVEST y alcanzar el "Definition of Ready".

---

## HU-01: Visualización Paginada del Listado de Tickets

### Evaluación de Calidad (INVEST)
- **Análisis:** La historia original era valiosa pero carecía de criterios testeables y estimables debido a la vaguedad de "sin perder rendimiento".
- **Acción de Refinamiento:** Se han definido escenarios concretos y se ha cuantificado el rendimiento en una nota técnica para hacerlo medible.

### Propuesta Refinada

**Como** Operador del Centro de Servicios,
**Quiero** ver el listado de tickets de forma paginada,
**Para** poder navegar grandes volúmenes de datos de manera controlada y eficiente.

#### Criterios de Aceptación

**Escenario 1: Visualización de la primera página**
**Dado que** existen más tickets que el tamaño de página configurado (ej. 50 tickets y tamaño 10)
**Cuando** accedo al listado de tickets
**Entonces** veo los primeros 10 tickets ordenados por fecha de creación descendente
**Y** se me informa que estoy en la "Página 1 de 5".

**Escenario 2: Navegación entre páginas**
**Dado que** estoy en la "Página 1" del listado de tickets
**Cuando** hago clic en el control para ir a la "Página 2"
**Entonces** la lista se actualiza para mostrar los siguientes 10 tickets
**Y** el ordenamiento se mantiene consistente.

**Escenario 3: Visualización sin resultados**
**Dado que** no hay tickets registrados en el sistema
**Cuando** accedo al listado de tickets
**Entonces** veo un mensaje claro que indica "No se encontraron tickets"
**Y** los controles de paginación están deshabilitados.

#### Notas de Implementación/QA
- **Rendimiento:** La carga de cualquier página debe completarse en menos de 1 segundo.
- **Configuración:** El tamaño de la página debe ser un valor configurable en el frontend (ej. 10, 25, 50).

---

## HU-06: Búsqueda de un Ticket por su Identificador Único

### Evaluación de Calidad (INVEST)
- **Análisis:** La historia original mezclaba criterios de aceptación con detalles de implementación del backend (nombres de servicios y errores).
- **Acción de Refinamiento:** Se ha separado el "qué" (comportamiento observable por el usuario) del "cómo" (detalles técnicos). Los criterios ahora se centran en la interacción del usuario.

### Propuesta Refinada

**Como** Operador del Centro de Servicios,
**Quiero** buscar un ticket específico usando su ID exacto,
**Para** acceder directamente a los detalles de un caso sin tener que buscarlo en la lista.

#### Criterios de Aceptación

**Escenario 1: Búsqueda exitosa**
**Dado que** existe un ticket con el ID "550e8400-e29b-41d4-a716-446655440000"
**Cuando** ingreso ese ID en el campo de búsqueda y confirmo
**Entonces** soy redirigido a la página de detalle de dicho ticket.

**Escenario 2: Búsqueda con ID de formato inválido**
**Dado que** ingreso el texto "ticket-123" en el campo de búsqueda
**Cuando** intento realizar la búsqueda
**Entonces** se muestra un mensaje de error "Formato de ID inválido" junto al campo
**Y** la búsqueda no se ejecuta.

**Escenario 3: Búsqueda de un ticket que no existe**
**Dado que** ingreso un ID con formato válido que no corresponde a ningún ticket existente
**Cuando** realizo la búsqueda
**Entonces** se muestra un mensaje informativo "Ticket no encontrado".

---

## HU-13: Cambio de Estado de un Ticket desde el Listado

### Evaluación de Calidad (INVEST)
- **Análisis:** La "historia" original era en realidad un documento de especificación de requisitos (SRS), lo que la hacía demasiado grande (No "Small"), poco negociable y difícil de estimar como una sola unidad de trabajo ágil.
- **Acción de Refinamiento:** Se ha extraído la esencia del valor para el usuario en una narrativa concisa. Los requerimientos funcionales y no funcionales se han destilado en Criterios de Aceptación claros y notas técnicas, eliminando el ruido.

### Propuesta Refinada

**Como** Administrador del Sistema,
**Quiero** cambiar el estado de un ticket directamente desde la fila correspondiente en el listado,
**Para** agilizar la gestión y actualización del progreso de los casos.

#### Criterios de Aceptación

**Escenario 1: Cambio de estado exitoso**
**Dado que** un ticket con estado "RECEIVED" es visible en la lista
**Cuando** selecciono la acción para cambiar su estado y elijo "IN_PROGRESS" en un modal de confirmación
**Entonces** el estado del ticket en la lista se actualiza visualmente a "IN_PROGRESS"
**Y** recibo una notificación de éxito.

**Escenario 2: Cancelación de la operación**
**Dado que** he abierto el modal para cambiar el estado de un ticket
**Cuando** presiono el botón "Cancelar"
**Entonces** el modal se cierra y el estado del ticket no sufre ninguna modificación.

**Escenario 3: Intento de cambio a un estado no permitido**
**Dado que** las reglas de negocio solo permiten cambiar de "RECEIVED" a "IN_PROGRESS"
**Cuando** abro el modal para un ticket en estado "IN_PROGRESS"
**Entonces** la única opción de cambio disponible es "RECEIVED".

**Escenario 4: Error durante la actualización**
**Dado que** se produce un error en el servidor al intentar confirmar el cambio
**Cuando** confirmo el nuevo estado en el modal
**Entonces** el modal permanece abierto y muestra un mensaje de error descriptivo.

#### Notas de Implementación/QA
- **Endpoint:** La interacción se realizará contra `PATCH /api/tickets/:ticketId/status`.
- **Idempotencia:** Si se intenta cambiar un ticket al estado que ya posee, la operación debe completarse exitosamente sin generar cambios ni errores.
- **UX:** La acción de cambio debe ser clara y requerir una confirmación para evitar acciones accidentales.