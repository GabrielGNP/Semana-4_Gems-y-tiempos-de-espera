# Contexto de Negocio - Sistema de Gestión de Quejas ISP
 
## 1. Descripción del Proyecto
### Nombre del Proyecto
 
Sistema Distribuido de Gestión de Quejas ISP
 
### Objetivo del Proyecto
 
Implementar una plataforma distribuida y escalable para la gestión eficiente de quejas y reportes de clientes de un proveedor de servicios de internet. El sistema busca:
 
- **Capturar** incidentes reportados por usuarios de manera rápida y confiable.
- **Procesar** quejas de forma asíncrona para garantizar que la recepción no se vea afectada por el volumen.
- **Priorizar** tickets automáticamente según la criticidad del incidente.
- **Visualizar** datos operativos en tiempo real para operadores y administradores.
- **Escalar** sin comprometer la calidad del servicio, soportando picos de carga mediante arquitectura orientada a eventos.
### Contexto de Negocio
 
El ISP requiere mecanismos robustos para gestionar quejas de clientes relacionadas con:
 
- Cortes de servicio (NO_SERVICE)
- Conexión intermitente (INTERMITTENT_SERVICE)
- Lentitud en la conexión (SLOW_CONNECTION)
- Problemas con routers (ROUTER_ISSUE)
- Consultas sobre facturación (BILLING_QUESTION)
- Otros incidentes (OTHER)
 
---
## 2. Flujos Críticos del Negocio
### Principales Flujos de Trabajo
#### 2.1 Flujo de Recepción, Validación, Procesamiento y almacenamiento
 
```
Usuario reporta incidente
    ↓
sistema recibe petición
    ↓
Valida campos obligatorios
    ↓
Genera Ticket ID único
    ↓
Aplica lógica de priorización según el tipo de incidente
    ↓
Determina estado del ciclo de vida (RECEIVED → VALIDATED → QUEUED → PRIORITIZED → IN_PROGRESS)
    ↓
Persiste ticket en repositorio
```
 
#### 2.2 Flujo de Consulta y Visualización de tickets
```
Usuario accede a dashboard
    ↓
Sistema recupera la información almacenada para poder visualizarla
    ↓
Retorna tickets con paginación, filtros aplicados y métricas
    ↓
Se visualizan los datos en el dashboard
```
#### 2.3 Flujo de cambio de estados
 
```

 
Usuario accede a dashboard
    ↓
Sistema recupera la información almacenada para poder visualizarla
    ↓
Retorna tickets con paginación, filtros aplicados y métricas
    ↓
Se visualizan los datos en el dashboard
    ↓
Usuario busca ticket específico
    ↓
Se cambia el estado del ticket
```


### Módulos o Funcionalidades Críticas
| Módulo | Descripción | Criticidad |
|--------|-------------|-----------|
| **Carga de Incidentes** | Interfaz de captura de incidentes y dashboard de visualización | Alta |
| **Procesamiento de Tickets** | Recepción, validación, Procesamiento, priorización y persistencia de tickets | Alta |
| **Consultar Tickets** | Consultas de tickets en el dashboard | Media |
| **Cambio de Estado** | Cambio manual del estado de un ticket en el dashboard | Media |
| **Base de Datos** | Persistencia de tickets y auditoría | Alta |
---
## 3. Reglas de Negocio y Restricciones
### Reglas de Negocio Relevantes
#### 3.1 Ciclo de Vida de la Queja
Todo ticket progresa a través de los siguientes estados:
 
1. **RECEIVED**: Queja ingresada en el sistema
2. **VALIDATED**: Datos validados correctamente
3. **QUEUED**: Encolada esperando procesamiento
4. **PRIORITIZED**: Prioridad asignada
5. **IN_PROGRESS**: Listo para atención de operador
#### 3.2 Matriz de Priorización por Tipo de Incidente
 
| Tipo de Incidente | Prioridad | Tiempo Respuesta Esperado |
|-------------------|-----------|--------------------------|
| NO_SERVICE | **ALTA** | < 2 horas |
| INTERMITTENT_SERVICE | **MEDIA** | < 6 horas |
| SLOW_CONNECTION | **MEDIA** | < 6 horas |
| ROUTER_ISSUE | **MEDIA** | < 6 horas |
| BILLING_QUESTION | **BAJA** | < 24 horas |
| OTHER | **BAJA** | < 24 horas |
 
#### 3.3 Validación de Campos
 
- **Campos obligatorios**: `lineNumber`, `email`, `incidentType`
- **Campo condicional**: `description` es **REQUERIDO SOLO** cuando `incidentType === "OTHER"`
- **Manejo de null**: Si `description` es `null` y el tipo NO es `OTHER`, se procesa normalmente sin errores
- **Formato email**: Debe ser válido según estándar RFC 5322
- **Número de línea**: Debe ser un número entero positivo
 
#### 3.4 Restricciones de Operación
- El dashboard es **solo lectura**: sin edición ni eliminación de tickets
- No se permite modificar la prioridad manualmente (es determinada por algoritmo)
- Los tickets mantienen su Ticket ID único e inmutable
- Cada línea de cliente puede tener múltiples tickets activos
 
### Regulaciones o Normativas
 
- **Protección de Datos**: Se deben cumplir normativas locales de protección de datos personales (email, número de línea)
- **Auditoría**: Todos los tickets procesados deben quedar registrados con timestamp de creación y procesamiento
- **Reporte**: El sistema debe permitir export de datos para auditoría externa (CSV/JSON)
- **Cumplimiento SLA**: Se deben monitorear y reportar tiempos de respuesta según matriz de priorización
 
---
## 4. Perfiles de Usuario y Roles
### Perfiles o Roles de Usuario en el Sistema
#### 4.1 Cliente Final
**Descripción**: Usuario que reporta un incidente a través del Frontend.
**Responsabilidades**:
- Reportar incidentes relacionados con su servicio de internet
- Proporcionar información de contacto (email, número de línea)
- Especificar tipo de incidente acorde a la situación
 
#### 4.2 Operador de Centro de Servicios
**Descripción**: Personal técnico que atiende tickets priorizados desde el dashboard.
**Responsabilidades**:
- Visualizar tickets asignados o en progreso
- Filtrar por prioridad, estado, tipo de incidente
- Consultar detalles del ticket (número de línea, email, descripción)
- Monitorear métricas de volumen
- Cambiar estado del ticket a "Resuelto" o "Pendiente" según avance de atención
**Permisos**:
- Lectura de todos los tickets
- Acceso a filtros y búsquedas
- Visualización de dashboard con gráficas
- Export de reportes (CSV/JSON)
- Cambiar estado de tickets a "Resuelto" o "Pendiente"
**Restricciones**:
- No puede modificar tickets
- No puede cambiar prioridades
- No puede eliminar registros
 
#### 4.4 Administrador del Sistema
**Descripción**: Responsable de configuración, mantención y escalabilidad del sistema.
**Responsabilidades**:
- Monitorear salud del sistema (APIs, RabbitMQ, BD)
- Gestionar instancias del Consumer
- Analizar logs y errores
- Realizar backups y mantenimiento
**Permisos**:
- Acceso a métricas técnicas
- Visibilidad de errores y excepciones
- Control de instancias de servicio (escalar/reducir)
 
---
 
## 5. Condiciones del Entorno Técnico
### Plataformas Soportadas
- **Plataforma**: Aplicación web responsive
- **Dispositivos**: Desktop y Tablet (responsive design)
 
### Tecnologías o Integraciones Clave
#### Message Broker
- **RabbitMQ**: Se utiliza como intermediario para desacoplar la recepción de tickets del procesamiento pesado. Permite manejar picos de carga sin perder mensajes y garantiza la entrega a los consumidores.
---
 
## 6. Casos Especiales o Excepciones
### 6.1 Manejo de Errores
- **Validación fallida**: El servicio notifica con detalles del error de por que falló la validación
- **RabbitMQ no disponible**: El servicio debe entra en retry automático y mantiene la petición pendiente hasta lograr conectarse
- **servicio de guardado caído**: Los mensajes se retienen en la cola hasta que el servicio se recupera
- **Base de datos no disponible**: Consumer reintenta la persistencia
 
### 6.2 Casos Especiales de Negocio
#### 6.2.1 Incidente de Tipo "OTHER"
- **Requerimiento**: `description` es obligatorio
- **Validación**: El Producer rechaza solicitud si falta descripción para tipo OTHER
- **Procesamiento**: El Consumer prioriza como BAJA por defecto
 
#### 6.2.2 Múltiples Reportes de Mismo Número de Línea
- **Detección**: Dashboard agrupa por lineNumber
- **Análisis**: Permite identificar patrones de problemática recurrente
- **Regla**: No se consolidan tickets, se mantienen separados para auditoría
 
#### 6.2.3 Congestión de Cola (Picos de Carga)
- **Manejo**: RabbitMQ mantiene persistencia
- **Escalado**: El servicio puede dividirse en múltiples instancias
- **Garantía**: Ningún mensaje se pierde
- **Monitoreo**: Dashboard muestra profundidad de cola
 
### 6.3 Restricciones de Concurrencia
 
- **Race Condition Prevention**: Cada Ticket ID es único (UUID v4)
- **Idempotencia**: Si un mensaje se procesa dos veces, el resultado es idéntico
- **Estado Consistente**: Base de datos mantiene transaccionalidad