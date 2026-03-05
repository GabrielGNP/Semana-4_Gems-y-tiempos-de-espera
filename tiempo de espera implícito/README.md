# Demostración de Tiempos de Espera Implícitos en Selenium

Proyecto de **Java + Selenium + Gradle** completamente **dockerizado** que demuestra las **CAPACIDADES y LIMITACIONES** de los **tiempos de espera implícitos** (Implicit Waits) en Selenium WebDriver con 4 casos de uso claros.

## ¿Qué es el Implicit Wait?

El **Implicit Wait** le indica al WebDriver que espere un tiempo determinado cuando intenta encontrar un elemento que no está disponible inmediatamente en el DOM. Una vez configurado, aplica **globalmente** a todas las llamadas `findElement()` y `findElements()`.

```java
// Configurar implicit wait de 10 segundos
driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));

// Ahora todas las búsquedas de elementos esperarán hasta 10 segundos
WebElement element = driver.findElement(By.id("mi-elemento"));
```

**IMPORTANTE:** El Implicit Wait **SOLO** espera a que el elemento **exista en el DOM**. NO verifica visibilidad, accesibilidad, cambios de contenido, ni estado habilitado.

## Requisitos

- **Docker** y **Docker Compose** instalados
- No se necesita Java, Gradle ni ningún navegador instalado localmente

## Ejecución Rápida

```bash
# Ejecutar tests en modo headless (sin visualización)
docker compose --profile tests run -it --rm --no-deps tests

# Ejecutar tests visibles en noVNC (localhost:7900)
docker compose up -d web chrome
docker compose --profile tests run -it --rm --no-deps -e TEST_HEADLESS=false tests

docker compose down
```

## Ver Selenium en vivo (modo noVNC)

**Opción 1: Tests con visualización en tiempo real**

```bash
# 1) Asegúrate de que web y chrome estén corriendo
docker compose up -d web chrome

# 2) Abre noVNC en tu navegador
# http://localhost:7900
# Contraseña: secret

# 3) Ejecuta los tests sin headless
docker compose --profile tests run -it --rm --no-deps -e TEST_HEADLESS=false tests

# 4) Observa los tests en tiempo real en la ventana noVNC

# 5) Apaga los contenedores al finalizar
docker compose down
```

**Opción 2: Solo navegador (sin tests)**

```bash
# Dejar web + chrome corriendo indefinidamente
docker compose up -d web chrome

# Acceder a:
# - noVNC (navegador): http://localhost:7900 (password: secret)
# - Selenium Grid:     http://localhost:4444

# Ejecuta tests manualmente o desde otra terminal
```

## Estructura del Proyecto

```
tiempo de espera implícito/
├── build.gradle                 # Configuración de Gradle y dependencias
├── settings.gradle              # Nombre del proyecto
├── Dockerfile                   # Imagen Docker para ejecutar los tests
├── docker-compose.yml           # Orquestación de contenedores (3 servicios)
├── nginx.conf                   # Configuración del servidor web (IPv4 + IPv6)
├── README.md                    # Este archivo
├── test-pages/
│   └── index.html               # Página HTML con 4 casos de demostración
└── src/test/java/com/example/
    └── ImplicitWaitTest.java    # Tests JUnit 5 con Selenium 4
```

## Los 4 Casos de Demostración

| Caso | Descripción | Resultado | Por qué |
|------|-------------|-----------|----------|
| **1** | Elemento que NO existe → aparece en 3 seg | ✅ **PASA** | Implicit wait lo espera y lo encuentra |
| **2** | Elemento oculto (display:none) → clic | ❌ **FALLA** | `findElement()` lo encuentra, pero `click()` falla (no visible) |
| **3** | Elemento existe con texto "inicial" → cambia a "actualizado" en 3 seg | ❌ **FALLA** | Implicit wait obtiene el texto **antes** de que cambie |
| **4** | Elemento deshabilitado (disabled) → escribir en él | ❌ **FALLA** | `findElement()` lo encuentra, pero `sendKeys()` falla (no interactuable) |

Cada caso es activado por un **botón** que inicia un temporizador JavaScript de 3 segundos.

## Características Principales

✅ **Proyecto 100% Dockerizado**
- Sin dependencias locales (Java, Gradle, Chrome)
- Contiene web server, navegador y tests

✅ **Visualización en Tiempo Real**
- noVNC integrado en puerto 7900
- Ver los tests ejecutándose live
- Un solo clic por navegador (sin recargas de página)

✅ **Single Browser Session**
- Los 4 tests usan la MISMA sesión de navegador
- La página se carga UNA SOLA VEZ en `@BeforeAll`
- Cada test solo hace clic en su botón correspondiente

✅ **Arquitectura Clara**
```
Gradle Test Runner (contenedor tests)
        ↓ (conexión remota)
Selenium Grid (localhost:4444)
        ↓ 
Chrome Headless/Visible
        ↓ (solicita página)
Nginx Web Server → test-pages/index.html
```

## Variables de Entorno

| Variable | Valor | Descripción |
|----------|-------|-------------|
| `SELENIUM_REMOTE_URL` | `http://chrome:4444/wd/hub` | URL del Selenium Grid (interno) |
| `TEST_PAGE_URL` | `http://web:80` | URL de la página de prueba |
| `TEST_HEADLESS` | `true` (default) \| `false` | Mostrar navegador en noVNC |

## Servicios Docker

1. **web** (nginx:alpine)
   - Puerto: 80 → sirve `test-pages/index.html`
   - Health check: Verifica conectividad HTTP
   - Restart policy: `unless-stopped`

2. **chrome** (selenium/standalone-chrome:latest)
   - Puerto 4444: Selenium Grid API
   - Puerto 7900: noVNC (visualización del navegador)
   - Health check: Verifica Selenium Hub status
   - Restart policy: `unless-stopped`

3. **tests** (gradle:8.5-jdk17)
   - Profile: `tests` (se ejecuta solo con `--profile tests`)
   - Depende de: `web` y `chrome` (healthy)
   - Ejecuta: `gradle test --no-daemon --info`

## Notas de Uso

### Ejecutar tests sin reconstruir imagen
```bash
docker compose --profile tests run  -it --rm --no-deps tests
```

### Reconstruir imagen de tests
```bash
docker compose --profile tests build --no-cache tests
```

### Ver logs en vivo
```bash
docker compose logs -f web
docker compose logs -f chrome
```

### Limpiar todo (incluyendo volúmenes)
```bash
docker compose down --volumes --remove-orphans
```

## Conceptos Demostrados

- ✅ Implicit Wait para búsqueda de elementos
- ❌ Limitaciones: NO espera visibilidad, accesibilidad, cambios de contenido
- 🔄 RemoteWebDriver con Selenium Grid
- 📱 Single browser session con `@TestInstance(PER_CLASS)`
- 🎯 Button-triggered timers en lugar de auto-start
- 🎨 Interfaz HTML responsiva con explicaciones por caso
