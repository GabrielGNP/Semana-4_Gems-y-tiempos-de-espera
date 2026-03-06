# Demostración de Tiempos de Espera Implícitos en Selenium

Proyecto de **Java + Selenium + Gradle** completamente **dockerizado** que demuestra las **CAPACIDADES y LIMITACIONES** de los **tiempos de espera implícitos** (Implicit Waits) en Selenium WebDriver con 4 casos de uso claros.

## ¿Qué es el Implicit Wait?

El **Implicit Wait** le indica al WebDriver que espere un tiempo determinado cuando intenta encontrar un elemento que no está disponible inmediatamente en el DOM. Una vez configurado, aplica **globalmente** a todas las llamadas `findElement()` y `findElements()`.


|**Línea de Código**|**Descripción**|
|-------------------|---------------|
| import java.net.URL; | Para crear la URL del servidor Selenium remoto |
| import org.openqa.selenium.*; | Para WebDriver, By, WebElement, ChromeOptions |
| import org.openqa.selenium.chrome.ChromeOptions; | Para configurar opciones del navegador Chrome |
| import org.openqa.selenium.remote.RemoteWebDriver; | Para conectarse al Selenium Grid remoto (Docker) |
| private WebDriver driver; | Variable global para mantener la sesión entre tests |
| String remoteUrl = System.getProperty("selenium.remote.url", "http://localhost:4444/wd/hub"); | Obtener URL del servidor Selenium (configuración Docker) |
| driver = new RemoteWebDriver(new URL(remoteUrl), options); | CRUCIAL: Establece la conexión con Docker/Selenium Grid |
| driver.get(baseUrl); | CRUCIAL: Navega a la página web (en este caso está dockerizado) |
| driver.findElement(By.id(...)) | CRUCIAL: Busca elementos en la página |
| driver.quit(); | CRUCIAL: Cierra sesión y libera recursos |
|-------------------|---------------|

Líneas de código explícitos para IMPLICIT WAITS
|**Línea de Código**|**Descripción**|
|-------------------|---------------|
| import java.time.Duration; | Para especificar tiempos en formato manejable (segundos, milisegundos) |
| driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10)); | ESTA ES LA ÚNICA LÍNEA QUE ACTIVA LOS IMPLICIT WAITS - Sin esta, los findElement() fallan inmediatamente si el elemento no existe |


```java
// Configurar implicit wait de 10 segundos
driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));

// Ahora todas las búsquedas de elementos esperarán hasta 10 segundos
WebElement element = driver.findElement(By.id("mi-elemento"));
```

**IMPORTANTE:** El Implicit Wait **SOLO** espera a que el elemento **exista en el DOM**. NO verifica visibilidad, accesibilidad, cambios de contenido, ni estado habilitado.

## Flujo de funcionamiento
1. **JUnit controla el flujo de los tests**: @BeforeAll, @BeforeEach, @Test, @AfterEach, @AfterAll
2. **Selenium simula acciones del usuario**: findElement(), click(), sendKeys(), getText()
3. **Esto genera resultados**: El navegador responde a las acciones
4. **JUnit siempre evalúa resultados**
- Cada test puede tener validaciones assertions (assertEquals(), assertNotNull(), etc.)
- Si un assertion falla -> el test FALLA
- Si todos los assertion del test pasan -> el test PASA
- Si no hay assertions, se considera PASSED por completarse sin eerores
- Fail() proboca que el test falle, independientemente de los assertions
5. **JUnit genera reportes**: HTML, XML, COnsola

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



## Los 4 Casos de Demostración

| Caso | Descripción | Resultado | Por qué |
|------|-------------|-----------|----------|
| **1** | Elemento que NO existe → aparece en 3 seg | ✅ **PASA** | Implicit wait lo espera y lo encuentra |
| **2** | Elemento oculto (display:none) → clic | ❌ **FALLA** | `findElement()` lo encuentra, pero `click()` falla (no visible) |
| **3** | Elemento existe con texto "inicial" → cambia a "actualizado" en 3 seg | ❌ **FALLA** | Implicit wait obtiene el texto **antes** de que cambie |
| **4** | Elemento deshabilitado (disabled) → escribir en él | ❌ **FALLA** | `findElement()` lo encuentra, pero `sendKeys()` falla (no interactuable) |

Cada caso es activado por un **botón** que inicia un temporizador JavaScript de 3 segundos.



## Variables de Entorno (docker-compose)

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

