# Suite de Pruebas - Sistema de Gestión de Quejas ISP

**Fecha:** 04/03/2026
**Rol:** Senior QA Test Engineer
**Alcance:** HU-01, HU-06, HU-13 (Refinadas)

---

## 1. HU-01: Visualización de Listado de Tickets con Paginación

### Pruebas de Navegación y Visualización

**TC-001-HU-01 - Validación de carga inicial y paginación por defecto**
*   **Técnica:** Verificación de Estado Inicial
*   **Gherkin:**
    **Dado que** existen 50 tickets registrados en el sistema con estado "RECEIVED"
    **Y** la configuración por defecto es de 10 items por página
    **Cuando** el operador accede al dashboard principal
    **Entonces** el sistema muestra una tabla con exactamente 10 filas
    **Y** el componente de paginación indica "Página 1 de 5"
    **Y** los tickets están ordenados por fecha de creación descendente.

**TC-002-HU-01 - Cambio de densidad de información (Items por página)**
*   **Técnica:** Partición de Equivalencia
*   **Gherkin:**
    **Dado que** el operador visualiza la primera página con 10 resultados
    **Cuando** selecciona "20" en el dropdown de "Items por página"
    **Entonces** la tabla se actualiza mostrando 20 registros
    **Y** el indicador de paginación cambia a "Página 1 de 3" (Total 50 items).

**TC-003-HU-01 - Persistencia del estado de navegación**
*   **Técnica:** Pruebas de Flujo de Usuario
*   **Gherkin:**
    **Dado que** el operador se encuentra en la "Página 2" del listado
    **Cuando** hace clic en un ticket para ver su detalle
    **Y** presiona el botón "Atrás" del navegador o de la aplicación
    **Entonces** el sistema carga nuevamente el listado en la "Página 2"
    **Y** se mantiene el ordenamiento previamente seleccionado.

**TC-004-HU-01 - Visualización de estado vacío (Zero State)**
*   **Técnica:** Análisis de Valores Límite (0 items)
*   **Gherkin:**
    **Dado que** se aplica un filtro que no retorna resultados (o la BD está vacía)
    **Cuando** el operador visualiza el listado
    **Entonces** se muestra el mensaje "No se encontraron tickets"
    **Y** los botones de "Siguiente" y "Anterior" están deshabilitados.

**TC-005-HU-01 - Rendimiento de carga de página (No Funcional)**
*   **Técnica:** Prueba de Performance
*   **Gherkin:**
    **Dado que** existen 10,000 tickets en la base de datos
    **Cuando** el operador solicita la página 500
    **Entonces** el tiempo de renderizado de la tabla es menor a 1000ms (Time to Interactive).

---

## 2. HU-06: Búsqueda Unitaria de Ticket por ID

### Pruebas de Búsqueda y Validación

**TC-006-HU-06 - Búsqueda exitosa por ID exacto**
*   **Técnica:** Partición de Equivalencia (Valor Válido)
*   **Gherkin:**
    **Dado que** existe el ticket con ID "550e8400-e29b-41d4-a716-446655440000"
    **Cuando** el operador ingresa "550e8400-e29b-41d4-a716-446655440000" en el buscador y presiona Enter
    **Entonces** el sistema redirige a la vista de detalle del ticket.

**TC-007-HU-06 - Validación de formato UUID inválido en Frontend**
*   **Técnica:** Validación de Formato / Fail Fast
*   **Gherkin:**
    **Dado que** el operador se encuentra en el campo de búsqueda
    **Cuando** ingresa el texto "ticket-1234" (No UUID) y presiona buscar
    **Entonces** aparece un mensaje de error "Formato de ID inválido" debajo del campo
    **Y** no se registra ninguna petición HTTP saliente en la pestaña de red del navegador.

**TC-008-HU-06 - Búsqueda de ID inexistente (404)**
*   **Técnica:** Manejo de Errores
*   **Gherkin:**
    **Dado que** el ID "00000000-0000-0000-0000-000000000000" es un UUID válido pero no existe en BD
    **Cuando** el operador realiza la búsqueda
    **Entonces** el sistema muestra una notificación o pantalla de "Ticket no encontrado".

**TC-009-HU-06 - Inyección de caracteres especiales (Seguridad)**
*   **Técnica:** Pruebas de Seguridad (Sanitización)
*   **Gherkin:**
    **Dado que** el operador intenta realizar una búsqueda
    **Cuando** ingresa "<script>alert('XSS')</script>" en el campo de ID
    **Entonces** el sistema valida el formato inválido
    **Y** no ejecuta el script en el navegador.

---

## 3. HU-13: Gestión de Cambio de Estado de Tickets

### Pruebas de Transición de Estados y Reglas de Negocio

**TC-010-HU-13 - Transición exitosa RECEIVED -> IN_PROGRESS**
*   **Técnica:** Transición de Estados (Happy Path)
*   **Gherkin:**
    **Dado que** el ticket "A-100" está en estado "RECEIVED"
    **Cuando** el administrador selecciona "Cambiar Estado" -> "IN_PROGRESS" y confirma en el modal
    **Entonces** el sistema muestra la notificación "Estado actualizado exitosamente"
    **Y** la etiqueta de estado en la fila del ticket cambia a "IN_PROGRESS" sin recargar la página.

**TC-011-HU-13 - Cancelación de cambio de estado**
*   **Técnica:** Flujo Alterno
*   **Gherkin:**
    **Dado que** el modal de confirmación está abierto con la opción "IN_PROGRESS" seleccionada
    **Cuando** el administrador presiona el botón "Cancelar"
    **Entonces** el modal desaparece
    **Y** el ticket "A-100" permanece en estado "RECEIVED".

**TC-012-HU-13 - Validación de Idempotencia**
*   **Técnica:** Robustez / Regla de Negocio
*   **Gherkin:**
    **Dado que** el ticket "A-100" ya fue actualizado a "IN_PROGRESS" por otro administrador hace un instante
    **Cuando** el administrador actual intenta cambiarlo nuevamente a "IN_PROGRESS"
    **Entonces** el sistema cierra el modal exitosamente (o muestra aviso informativo)
    **Y** el estado final se mantiene en "IN_PROGRESS" sin generar error técnico.

**TC-013-HU-13 - Manejo de Error de Servidor (500)**
*   **Técnica:** Manejo de Excepciones
*   **Gherkin:**
    **Dado que** el servicio de base de datos está momentáneamente no disponible
    **Cuando** el administrador confirma el cambio de estado
    **Entonces** el modal permanece abierto
    **Y** se muestra un mensaje de error "No se pudo actualizar el ticket. Intente nuevamente".

**TC-014-HU-13 - Intento de transición prohibida (Negativo)**
*   **Técnica:** Tabla de Decisión / Lógica de Negocio
*   **Gherkin:**
    **Dado que** un ticket se encuentra hipotéticamente en estado "CLOSED" (Estado final)
    **Cuando** el administrador intenta abrir el modal de cambio de estado
    **Entonces** la opción de cambio de estado aparece deshabilitada o bloqueada
    **Y** no permite seleccionar ningún nuevo estado.

---

## 4. Matriz de Trazabilidad de Pruebas (RTM)

| ID HU | Criterio de Aceptación (Refinado) | ID Caso de Prueba | Tipo | Cobertura |
| :--- | :--- | :--- | :--- | :--- |
| **HU-01** | Escenario 1: Navegación básica | TC-001-HU-01 | Positiva | ✅ Completa |
| **HU-01** | Escenario 2: Cambio de tamaño | TC-002-HU-01 | Positiva | ✅ Completa |
| **HU-01** | Escenario 3: Lista vacía | TC-004-HU-01 | Negativa | ✅ Completa |
| **HU-01** | Nota: Persistencia | TC-003-HU-01 | Usabilidad | ✅ Completa |
| **HU-01** | Nota: Rendimiento | TC-005-HU-01 | Performance | ✅ Completa |
| **HU-06** | Escenario 1: Búsqueda exitosa | TC-006-HU-06 | Positiva | ✅ Completa |
| **HU-06** | Escenario 2: Ticket no encontrado | TC-008-HU-06 | Negativa | ✅ Completa |
| **HU-06** | Escenario 3: Formato inválido | TC-007-HU-06 | Negativa | ✅ Completa |
| **HU-06** | Nota: Seguridad | TC-009-HU-06 | Seguridad | ✅ Completa |
| **HU-13** | Escenario 1: Cambio exitoso | TC-010-HU-13 | Positiva | ✅ Completa |
| **HU-13** | Escenario 2: Cancelación | TC-011-HU-13 | Alterna | ✅ Completa |
| **HU-13** | Escenario 3: Transición inválida | TC-014-HU-13 | Negativa | ✅ Completa |
| **HU-13** | Escenario 4: Idempotencia | TC-012-HU-13 | Robustez | ✅ Completa |
| **HU-13** | Escenario 5: Error servidor | TC-013-HU-13 | Excepción | ✅ Completa |

---

## 5. Tabla de Ajustes y Justificación Técnica

Como QA Expert, he realizado los siguientes ajustes sobre los criterios originales para garantizar la robustez del producto final:

| ID Ajuste | HU Afectada | Descripción del Ajuste | Justificación Técnica (QA) |
| :--- | :--- | :--- | :--- |
| **AJ-01** | HU-01 | Adición de **TC-003 (Persistencia)** | Es un defecto común en SPAs (Single Page Applications) perder el estado de la paginación al navegar "atrás". Es crítico para la UX del operador. |
| **AJ-02** | HU-01 | Adición de **TC-005 (Performance)** | La HU mencionaba "sin perder rendimiento". Se cuantificó a <1000ms para tener un criterio de aceptación binario (Pasa/Falla). |
| **AJ-03** | HU-06 | Adición de **TC-009 (Seguridad XSS)** | Los campos de búsqueda son vectores de ataque comunes. Aunque se valide UUID, se debe asegurar que inputs maliciosos no rompan el frontend. |
| **AJ-04** | HU-06 | Separación de validación Frontend/Backend | Se creó **TC-007** específico para validar que el frontend bloquee la petición ("Fail Fast"), ahorrando recursos del servidor. |
| **AJ-05** | HU-13 | Inclusión de **TC-012 (Idempotencia)** | En sistemas distribuidos (ver Contexto), es posible recibir doble confirmación. El sistema debe ser resiliente y no fallar si se intenta aplicar el mismo estado dos veces. |
| **AJ-06** | HU-13 | Bloqueo de UI en estados finales (**TC-014**) | Prevenir el error es mejor que validarlo. Si el ticket está cerrado, la UI no debería ni siquiera permitir abrir el modal. |