# Demostración de Tiempos de Espera Implícitos en Selenium

Proyecto de **Java + Selenium + Gradle** completamente **dockerizado** que demuestra el funcionamiento de los **tiempos de espera implícitos** (Implicit Waits) en Selenium WebDriver.

## ¿Qué es el Implicit Wait?

El **Implicit Wait** le indica al WebDriver que espere un tiempo determinado cuando intenta encontrar un elemento que no está disponible inmediatamente en el DOM. Una vez configurado, aplica **globalmente** a todas las llamadas `findElement()` y `findElements()`.

```java
// Configurar implicit wait de 10 segundos
driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));

// Ahora todas las búsquedas de elementos esperarán hasta 10 segundos
WebElement element = driver.findElement(By.id("mi-elemento"));
```

## Requisitos

- **Docker** y **Docker Compose** instalados
- No se necesita Java, Gradle ni ningún navegador instalado localmente

## Ejecución

```bash
# Ejecutar todos los tests
docker compose up --build

# Ejecutar y ver los resultados, luego limpiar
docker compose up --build --abort-on-container-exit
docker compose down
```

## Ver Selenium en vivo (modo demo)

Si los tests terminan muy rápido, en `localhost:7900` puede verse poco o casi nada.
Para visualizar mejor, ejecuta el navegador sin headless y con pausas entre pasos:

```bash
# 1) Levantar solo web + chrome y dejarlos corriendo
docker compose up -d --build web chrome

# 2) Abrir visores
# noVNC (navegador en vivo): http://localhost:7900   (password: secret)
# Selenium Grid:           http://localhost:4444

# 3) Ejecutar tests en modo visual con pausa por paso (1200 ms)
docker compose run --rm --no-deps \
    -e TEST_HEADLESS=false \
    -e TEST_STEP_DELAY_MS=1200 \
    tests

# 4) Apagar todo al finalizar
docker compose down
```

Variables útiles para demo:
- `TEST_HEADLESS=false`: muestra el navegador en noVNC.
- `TEST_STEP_DELAY_MS=1200`: agrega una pausa visible entre acciones.

## Estructura del Proyecto

```
tiempo de espera implícito/
├── build.gradle                 # Configuración de Gradle y dependencias
├── settings.gradle              # Nombre del proyecto
├── Dockerfile                   # Imagen Docker para ejecutar los tests
├── docker-compose.yml           # Orquestación de contenedores
├── nginx.conf                   # Configuración del servidor web
├── test-pages/
│   └── index.html               # Página HTML con elementos dinámicos
└── src/test/java/com/example/
    └── ImplicitWaitTest.java    # Tests de Selenium
```

## Tests Incluidos

| # | Test | Descripción |
|---|------|-------------|
| 1 | Carga de página | Verifica que la página de prueba carga correctamente |
| 2 | Elemento inmediato sin wait | Encuentra un elemento presente inmediatamente (sin implicit wait) |
| 3 | Elemento retrasado sin wait | **FALLA** al buscar un elemento que tarda 3 seg sin implicit wait |
| 4 | Elemento retrasado con wait | **FUNCIONA** gracias al implicit wait de 10 seg |
| 5 | Elemento inexistente con wait | Espera el tiempo completo del timeout y luego falla |
| 6 | Clic + contenido dinámico | Clic en botón y espera contenido que aparece a los 4 seg |
| 7 | Lista progresiva | Encuentra elementos que aparecen progresivamente cada 2 seg |
| 8 | Comparación corto vs largo | Demuestra la diferencia entre un wait corto (insuficiente) y uno largo (suficiente) |

## Arquitectura Docker

El proyecto utiliza 3 contenedores:

1. **web** (nginx:alpine): Sirve las páginas HTML de prueba
2. **chrome** (selenium/standalone-chrome): Navegador Chrome con Selenium Grid
3. **tests** (gradle:8.5-jdk17): Ejecuta los tests de Selenium conectándose remotamente al Chrome
