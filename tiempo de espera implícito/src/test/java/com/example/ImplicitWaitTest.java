package com.example;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Clase de pruebas que demuestra el funcionamiento del TIEMPO DE ESPERA IMPLÍCITO
 * (Implicit Wait) en Selenium WebDriver.
 *
 * El implicit wait le indica al WebDriver que espere un tiempo determinado
 * cuando intenta encontrar un elemento que no está disponible inmediatamente.
 * Una vez configurado, aplica globalmente a todas las llamadas findElement/findElements.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Demostración de Tiempos de Espera Implícitos en Selenium")
public class ImplicitWaitTest {

    private WebDriver driver;
    private String baseUrl;
    private long stepDelayMs;

    private void demoPause(String reason) {
        if (stepDelayMs <= 0) {
            return;
        }
        try {
            System.out.println("[DEMO] Pausa " + stepDelayMs + " ms: " + reason);
            Thread.sleep(stepDelayMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("La pausa de demostración fue interrumpida", e);
        }
    }

    @BeforeEach
    void setUp() throws MalformedURLException {
        String remoteUrl = System.getProperty("selenium.remote.url", "http://localhost:4444/wd/hub");
        baseUrl = System.getProperty("test.page.url", "http://localhost:8080");
        boolean headless = Boolean.parseBoolean(System.getProperty("test.headless", "true"));
        String stepDelayRaw = System.getProperty("test.step.delay.ms", "0");
        try {
            stepDelayMs = Long.parseLong(stepDelayRaw);
        } catch (NumberFormatException e) {
            stepDelayMs = 0;
            System.out.println("[CONFIG] test.step.delay.ms inválido ('" + stepDelayRaw + "'). Se usará 0.");
        }
        if (stepDelayMs < 0) {
            stepDelayMs = 0;
        }

        System.out.println("[CONFIG] headless=" + headless + ", stepDelayMs=" + stepDelayMs);

        ChromeOptions options = new ChromeOptions();
        if (headless) {
            options.addArguments("--headless=new");
        }
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,1080");

        driver = new RemoteWebDriver(new URL(remoteUrl), options);
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    // ====================================================================
    // TEST 1: Verificar que la página carga correctamente
    // ====================================================================
    @Test
    @Order(1)
    @DisplayName("1. Verificar que la página de prueba carga correctamente")
    void testPageLoadsCorrectly() {
        System.out.println("=== TEST 1: Verificando carga de la página ===");

        driver.get(baseUrl);
        demoPause("Página cargada");

        WebElement title = driver.findElement(By.id("page-title"));
        assertNotNull(title, "El título de la página debe existir");
        assertTrue(title.getText().contains("Tiempo de Espera Implícito"),
                "El título debe contener 'Tiempo de Espera Implícito'");

        System.out.println("✓ Página cargada correctamente. Título: " + title.getText());
    }

    // ====================================================================
    // TEST 2: Elemento inmediato SIN implicit wait (debe funcionar)
    // ====================================================================
    @Test
    @Order(2)
    @DisplayName("2. Encontrar elemento inmediato SIN implicit wait")
    void testFindImmediateElementWithoutImplicitWait() {
        System.out.println("=== TEST 2: Buscar elemento inmediato SIN implicit wait ===");

        driver.get(baseUrl);
        demoPause("Página cargada");

        // El implicit wait está en 0 (por defecto)
        // Este elemento está presente inmediatamente, así que no necesita esperas
        WebElement immediateElement = driver.findElement(By.id("immediate-element"));
        assertNotNull(immediateElement);
        System.out.println("✓ Elemento inmediato encontrado: " + immediateElement.getText());
    }

    // ====================================================================
    // TEST 3: Elemento retrasado SIN implicit wait (debe FALLAR)
    // ====================================================================
    @Test
    @Order(3)
    @DisplayName("3. Intentar encontrar elemento retrasado SIN implicit wait - DEBE FALLAR")
    void testFindDelayedElementWithoutImplicitWait() {
        System.out.println("=== TEST 3: Buscar elemento retrasado SIN implicit wait ===");
        System.out.println("El elemento 'auto-loaded-element' aparece después de 3 segundos.");
        System.out.println("Sin implicit wait, Selenium NO esperará y lanzará NoSuchElementException.");

        driver.get(baseUrl);
        demoPause("Página cargada antes de buscar elemento retrasado");

        // NO configuramos implicit wait (queda en 0 por defecto)
        // El elemento 'auto-loaded-element' aparece después de 3 segundos vía JavaScript
        // Como Selenium no espera, debería lanzar NoSuchElementException
        assertThrows(NoSuchElementException.class, () -> {
            driver.findElement(By.id("auto-loaded-element"));
        }, "Sin implicit wait, buscar un elemento retrasado debe lanzar NoSuchElementException");

        System.out.println("✓ Confirmado: Sin implicit wait, se lanza NoSuchElementException");
        System.out.println("  Esto demuestra que por defecto Selenium NO espera a que los elementos aparezcan.");
    }

    // ====================================================================
    // TEST 4: Elemento retrasado CON implicit wait (debe FUNCIONAR)
    // ====================================================================
    @Test
    @Order(4)
    @DisplayName("4. Encontrar elemento retrasado CON implicit wait - DEBE FUNCIONAR")
    void testFindDelayedElementWithImplicitWait() {
        System.out.println("=== TEST 4: Buscar elemento retrasado CON implicit wait ===");
        System.out.println("Configurando implicit wait en 10 segundos...");

        driver.get(baseUrl);
        demoPause("Página cargada antes de activar implicit wait");

        // Configuramos el implicit wait en 10 segundos
        // Esto le dice a Selenium: "cuando busques un elemento, si no está presente,
        // sigue intentando durante un máximo de 10 segundos antes de fallar"
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));

        System.out.println("Buscando 'auto-loaded-element' (aparece después de 3 seg)...");
        long startTime = System.currentTimeMillis();

        // Ahora SI encontrará el elemento porque el implicit wait esperará
        // hasta 10 segundos para que aparezca (aparece a los 3 segundos)
        WebElement delayedElement = driver.findElement(By.id("auto-loaded-element"));
        long elapsedTime = System.currentTimeMillis() - startTime;

        assertNotNull(delayedElement, "El elemento retrasado debe encontrarse con implicit wait");
        assertTrue(delayedElement.getText().contains("cargó automáticamente"),
                "El texto del elemento debe indicar que se cargó automáticamente");

        System.out.println("✓ Elemento encontrado después de ~" + elapsedTime + " ms");
        System.out.println("  Texto: " + delayedElement.getText());
        System.out.println("  El implicit wait permitió que Selenium esperara hasta que el elemento apareciera.");
    }

    // ====================================================================
    // TEST 5: Implicit wait con elemento que NO existe (debe fallar después del timeout)
    // ====================================================================
    @Test
    @Order(5)
    @DisplayName("5. Implicit wait con elemento inexistente - falla después del timeout")
    void testImplicitWaitWithNonExistentElement() {
        System.out.println("=== TEST 5: Implicit wait con elemento que NO existe ===");
        System.out.println("Configurando implicit wait en 5 segundos...");

        driver.get(baseUrl);
        demoPause("Página cargada antes de buscar elemento inexistente");

        // Configuramos implicit wait a 5 segundos
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));

        System.out.println("Buscando elemento inexistente...");
        long startTime = System.currentTimeMillis();

        // Buscamos un elemento que NUNCA existirá
        // El implicit wait esperará los 5 segundos completos antes de lanzar la excepción
        assertThrows(NoSuchElementException.class, () -> {
            driver.findElement(By.id("elemento-que-no-existe"));
        });

        long elapsedTime = System.currentTimeMillis() - startTime;

        System.out.println("✓ NoSuchElementException lanzada después de ~" + elapsedTime + " ms");
        System.out.println("  Selenium esperó aproximadamente 5 segundos antes de lanzar la excepción.");
        System.out.println("  Esto demuestra que el implicit wait define el tiempo MÁXIMO de espera.");

        // Verificamos que realmente esperó al menos 4 segundos (con margen)
        assertTrue(elapsedTime >= 4000,
                "Selenium debe haber esperado al menos 4 segundos (configurado en 5)");
    }

    // ====================================================================
    // TEST 6: Clic en botón + implicit wait para contenido dinámico
    // ====================================================================
    @Test
    @Order(6)
    @DisplayName("6. Clic en botón y esperar contenido dinámico con implicit wait")
    void testClickButtonAndWaitForDynamicContent() {
        System.out.println("=== TEST 6: Clic en botón + implicit wait para contenido dinámico ===");

        driver.get(baseUrl);
        demoPause("Página cargada antes del clic");

        // Configuramos implicit wait a 10 segundos
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));

        // Hacemos clic en el botón de carga dinámica
        WebElement loadButton = driver.findElement(By.id("load-btn"));
        System.out.println("Haciendo clic en el botón de carga...");
        demoPause("Antes de hacer clic en cargar contenido dinámico");
        loadButton.click();

        // El contenido dinámico aparece después de 4 segundos
        System.out.println("Esperando que aparezca el contenido dinámico (4 seg)...");
        long startTime = System.currentTimeMillis();

        WebElement dynamicElement = driver.findElement(By.id("dynamic-element"));
        long elapsedTime = System.currentTimeMillis() - startTime;

        assertNotNull(dynamicElement, "El elemento dinámico debe encontrarse");
        assertTrue(dynamicElement.getText().contains("Contenido dinámico cargado"),
                "El texto debe indicar que el contenido se cargó");

        System.out.println("✓ Contenido dinámico encontrado después de ~" + elapsedTime + " ms");
        System.out.println("  Texto: " + dynamicElement.getText());
    }

    // ====================================================================
    // TEST 7: Implicit wait con lista progresiva
    // ====================================================================
    @Test
    @Order(7)
    @DisplayName("7. Implicit wait con lista que se llena progresivamente")
    void testImplicitWaitWithProgressiveList() {
        System.out.println("=== TEST 7: Implicit wait con lista progresiva ===");

        driver.get(baseUrl);
        demoPause("Página cargada antes de iniciar la lista progresiva");

        // Configuramos implicit wait a 15 segundos (la lista tarda ~6 seg en llenarse)
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(15));

        // Clic en botón para iniciar la carga de la lista
        WebElement startButton = driver.findElement(By.id("start-list-btn"));
        System.out.println("Iniciando carga de lista progresiva...");
        demoPause("Antes de hacer clic para iniciar la lista");
        startButton.click();

        // Esperamos al primer elemento (aparece a los 2 seg)
        long startTime = System.currentTimeMillis();
        WebElement item1 = driver.findElement(By.id("list-item-1"));
        long elapsed1 = System.currentTimeMillis() - startTime;
        System.out.println("✓ Item 1 encontrado en ~" + elapsed1 + " ms: " + item1.getText());

        // Esperamos al segundo elemento (aparece 2 seg después del primero)
        startTime = System.currentTimeMillis();
        WebElement item2 = driver.findElement(By.id("list-item-2"));
        long elapsed2 = System.currentTimeMillis() - startTime;
        System.out.println("✓ Item 2 encontrado en ~" + elapsed2 + " ms: " + item2.getText());

        // Esperamos al tercer elemento (aparece 2 seg después del segundo)
        startTime = System.currentTimeMillis();
        WebElement item3 = driver.findElement(By.id("list-item-3"));
        long elapsed3 = System.currentTimeMillis() - startTime;
        System.out.println("✓ Item 3 encontrado en ~" + elapsed3 + " ms: " + item3.getText());

        assertEquals("Primer elemento de la lista", item1.getText());
        assertEquals("Segundo elemento de la lista", item2.getText());
        assertEquals("Tercer elemento de la lista", item3.getText());

        System.out.println("  El implicit wait permitió encontrar cada elemento a medida que aparecía.");
    }

    // ====================================================================
    // TEST 8: Comparación de tiempos con diferentes valores de implicit wait
    // ====================================================================
    @Test
    @Order(8)
    @DisplayName("8. Comparar comportamiento con implicit wait corto vs largo")
    void testCompareShortVsLongImplicitWait() {
        System.out.println("=== TEST 8: Comparación implicit wait corto vs largo ===");

        driver.get(baseUrl);
        demoPause("Página cargada para comparar wait corto vs largo");

        // Caso A: Implicit wait de 1 segundo (menor que los 3 seg del elemento)
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(1));

        System.out.println("Caso A: Implicit wait = 1 segundo (elemento aparece a los 3 seg)");
        long startTime = System.currentTimeMillis();

        assertThrows(NoSuchElementException.class, () -> {
            driver.findElement(By.id("auto-loaded-element"));
        }, "Con 1 segundo de implicit wait, no debe encontrar un elemento que tarda 3 seg");

        long elapsed = System.currentTimeMillis() - startTime;
        System.out.println("✓ Caso A falló después de ~" + elapsed + " ms (esperado: ~1000ms)");
        System.out.println("  El implicit wait de 1 seg es INSUFICIENTE para un elemento que tarda 3 seg.");

        // Recargamos la página para reiniciar el timer del JavaScript
        driver.get(baseUrl);
        demoPause("Página recargada para caso B");

        // Caso B: Implicit wait de 10 segundos (mayor que los 3 seg del elemento)
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));

        System.out.println("\nCaso B: Implicit wait = 10 segundos (elemento aparece a los 3 seg)");
        startTime = System.currentTimeMillis();

        WebElement element = driver.findElement(By.id("auto-loaded-element"));
        elapsed = System.currentTimeMillis() - startTime;

        assertNotNull(element);
        System.out.println("✓ Caso B encontró el elemento en ~" + elapsed + " ms (esperado: ~3000ms)");
        System.out.println("  El implicit wait de 10 seg fue SUFICIENTE y solo esperó lo necesario (~3 seg).");
        System.out.println("\n  CONCLUSIÓN: El implicit wait define el tiempo MÁXIMO de espera,");
        System.out.println("  pero Selenium deja de esperar tan pronto como encuentra el elemento.");
    }
}
