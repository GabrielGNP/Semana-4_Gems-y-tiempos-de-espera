# Suite de Pruebas - Sistema de Gestión de Quejas ISP

**Fecha:** 04/03/2026
**Rol:** Senior QA Test Engineer
**Alcance:** HU-01, HU-06, HU-13 (Refinadas con Gema A (L))

---

## 1. HU-01: Visualización Paginada del Listado de Tickets

### Pruebas de Navegación y Visualización

**TC-001-HU-01 - Carga inicial y paginación por defecto**
*   **Técnica:** Verificación de Estado Inicial
*   **Gherkin:**
    **Dado que** existen 50 tickets registrados en el sistema
    **Y** la configuración por defecto es de 10 items por página
    **Cuando** el operador accede al dashboard principal
    **Entonces** el sistema muestra una tabla con exactamente 10 filas
    **Y** el componente de paginación indica "Página 1 de 5"
    **Y** los tickets están ordenados por fecha de creación descendente.

**TC-002-HU-01 - Navegación a la segunda página**
*   **Técnica:** Partición de Equivalencia
*   **Gherkin:**
    **Dado que** el operador visualiza la primera página con 10 resultados
    **Cuando** hace clic en el control para ir a la "Página 2"
    **Entonces** la tabla se actualiza mostrando los siguientes 10 tickets
    **Y** el ordenamiento se mantiene consistente.

**TC-003-HU-01 - Visualización de estado vacío (Zero State)**
*   **Técnica:** Análisis de Valores Límite (0 items)
*   **Gherkin:**
    **Dado que** no hay tickets registrados en el sistema
    **Cuando** el operador accede al listado
    **Entonces** se muestra el mensaje "No se encontraron tickets"
    **Y** los controles de paginación están deshabilitados.

**TC-004-HU-01 - Rendimiento de carga de página (No Funcional)**
*   **Técnica:** Prueba de Performance
*   **Gherkin:**
    **Dado que** existen 10,000 tickets en la base de datos
    **Cuando** el operador solicita la página 500
    **Entonces** el tiempo de renderizado de la tabla es menor a 1000ms (Time to Interactive).

---

## 2. HU-06: Búsqueda de un Ticket por su Identificador Único

### Pruebas de Búsqueda y Validación

**TC-005-HU-06 - Búsqueda exitosa por ID exacto**
*   **Técnica:** Partición de Equivalencia (Valor Válido)
*   **Gherkin:**
    **Dado que** existe el ticket con ID "550e8400-e29b-41d4-a716-446655440000"
    **Cuando** el operador ingresa "550e8400-e29b-41d4-a716-446655440000" en el buscador y presiona Enter
    **Entonces** el sistema redirige a la página de detalle del ticket.

**TC-006-HU-06 - Validación de formato UUID inválido**
*   **Técnica:** Validación de Formato / Fail Fast
*   **Gherkin:**
    **Dado que** el operador se encuentra en el campo de búsqueda
    **Cuando** ingresa el texto "ticket-1234" (No UUID) y presiona buscar
    **Entonces** aparece un mensaje de error "Formato de ID inválido" junto al campo
    **Y** la búsqueda no se ejecuta.

**TC-007-HU-06 - Búsqueda de ID inexistente (404)**
*   **Técnica:** Manejo de Errores
*   **Gherkin:**
    **Dado que** el ID "00000000-0000-0000-0000-000000000000" es un UUID válido pero no existe en BD
    **Cuando** el operador realiza la búsqueda
    **Entonces** el sistema muestra una notificación o pantalla de "Ticket no encontrado".

---

## 3. HU-13: Cambio de Estado de un Ticket desde el Listado

### Pruebas de Transición de Estados y Reglas de Negocio

**TC-008-HU-13 - Transición exitosa RECEIVED -> IN_PROGRESS**
*   **Técnica:** Transición de Estados (Happy Path)
*   **Gherkin:**
    **Dado que** el ticket "A-100" está en estado "RECEIVED"
    **Cuando** el administrador selecciona la acción para cambiar su estado y elige "IN_PROGRESS" en un modal de confirmación
    **Entonces** el estado del ticket en la lista se actualiza visualmente a "IN_PROGRESS"
    **Y** recibe una notificación de éxito.

**TC-009-HU-13 - Cancelación de cambio de estado**
*   **Técnica:** Flujo Alterno
*   **Gherkin:**
    **Dado que** el modal de confirmación está abierto con la opción "IN_PROGRESS" seleccionada
    **Cuando** el administrador presiona el botón "Cancelar"
    **Entonces** el modal se cierra
    **Y** el ticket "A-100" permanece en estado "RECEIVED".

**TC-010-HU-13 - Intento de cambio a un estado no permitido**
*   **Técnica:** Tabla de Decisión / Lógica de Negocio
*   **Gherkin:**
    **Dado que** las reglas de negocio solo permiten cambiar de "RECEIVED" a "IN_PROGRESS"
    **Cuando** el administrador abre el modal para un ticket en estado "IN_PROGRESS"
    **Entonces** la única opción de cambio disponible es "RECEIVED".

**TC-011-HU-13 - Manejo de Error de Servidor**
*   **Técnica:** Manejo de Excepciones
*   **Gherkin:**
    **Dado que** se produce un error en el servidor al intentar confirmar el cambio
    **Cuando** el administrador confirma el nuevo estado en el modal
    **Entonces** el modal permanece abierto
    **Y** muestra un mensaje de error descriptivo.

---

## 4. Matriz de Trazabilidad de Pruebas (RTM)

| ID HU | Criterio de Aceptación (Refinado) | ID Caso de Prueba | Tipo | Técnica Aplicada |
| :--- | :--- | :--- | :--- | :--- |
| **HU-01** | Escenario 1: Visualización de la primera página | TC-001-HU-01 | Positiva | Verificación de Estado Inicial |
| **HU-01** | Escenario 2: Navegación entre páginas | TC-002-HU-01 | Positiva | Partición de Equivalencia |
| **HU-01** | Escenario 3: Visualización sin resultados | TC-003-HU-01 | Negativa | Análisis de Valores Límite |
| **HU-06** | Escenario 1: Búsqueda exitosa | TC-005-HU-06 | Positiva | Partición de Equivalencia |
| **HU-06** | Escenario 2: Búsqueda con ID de formato inválido | TC-006-HU-06 | Negativa | Validación de Formato |
| **HU-06** | Escenario 3: Búsqueda de un ticket que no existe | TC-007-HU-06 | Negativa | Manejo de Errores |
| **HU-13** | Escenario 1: Cambio de estado exitoso | TC-008-HU-13 | Positiva | Transición de Estados |
| **HU-13** | Escenario 2: Cancelación de la operación | TC-009-HU-13 | Alterna | Flujo Alterno |
| **HU-13** | Escenario 3: Intento de cambio a un estado no permitido | TC-010-HU-13 | Negativa | Tabla de Decisión |
| **HU-13** | Escenario 4: Error durante la actualización | TC-011-HU-13 | Excepción | Manejo de Excepciones |

---

## 5. Tabla de Ajustes y Justificación Técnica

Como QA Expert, he realizado los siguientes ajustes sobre los criterios originales para garantizar la robustez del producto final:

| ID Ajuste | HU Afectada | Descripción del Ajuste | Justificación Técnica (QA) |
| :--- | :--- | :--- | :--- |
| **AJ-01** | HU-01 | Adición de **TC-004 (Performance)** | Se debe garantizar que la carga de la página sea rápida para una buena experiencia del usuario. |
| **AJ-02** | HU-13 | Simplificación de escenarios | Se han simplificado los escenarios para que sean más fáciles de entender y probar. |