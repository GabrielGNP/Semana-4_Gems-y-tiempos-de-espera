package com.example;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Demostración de las CAPACIDADES y LIMITACIONES del Implicit Wait en Selenium.
 *
 * Caso 1: ✅ PASA  — El elemento aún no existe → implicit wait espera y lo encuentra.
 * Caso 2: ❌ FALLA — El elemento está oculto   → findElement lo halla, pero click() falla.
 * Caso 3: ❌ FALLA — El texto cambia después    → implicit wait obtiene el texto viejo.
 * Caso 4: ❌ FALLA — El elemento está disabled  → findElement lo halla, pero no puede escribir.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Implicit Wait — Capacidades y Limitaciones")@TestInstance(TestInstance.Lifecycle.PER_CLASS)public class ImplicitWaitTest {

    private WebDriver driver;
    private String baseUrl;
    private JavascriptExecutor js;

    @BeforeAll
    void setUp() throws MalformedURLException {
        String remoteUrl = System.getProperty("selenium.remote.url", "http://localhost:4444/wd/hub");
        baseUrl = System.getProperty("test.page.url", "http://localhost:8080");
        boolean headless = Boolean.parseBoolean(System.getProperty("test.headless", "true"));

        ChromeOptions options = new ChromeOptions();
        if (headless) {
            options.addArguments("--headless=new");
        }
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--window-size=1920,1080");

        driver = new RemoteWebDriver(new URL(remoteUrl), options);

        // Inicializar JavascriptExecutor
        js = (JavascriptExecutor) driver;

        // Configuramos implicit wait de 10 segundos para TODOS los tests
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));

        // Cargamos la página UNA SOLA VEZ
        driver.get(baseUrl);
    }

    @AfterAll
    void tearDown() {
        System.out.println("\n╔═════════════════════════════════════════════╗");
        System.out.println("║  ✅ TODOS LOS TESTS HAN FINALIZADO          ║");
        System.out.println("║  Esperando 24 segundos para inspeccionar     ║");
        System.out.println("║  los resultados en noVNC...                  ║");
        System.out.println("╚══════════════════════════════════════════════╝\n");
        System.out.flush();
        
        try {
            Thread.sleep(24000);  // Esperar 24 segundos para que el usuario inspeccione noVNC
        } catch (InterruptedException e) {
            System.out.println("⚠️  Espera interrumpida: " + e.getMessage());
        }
        
        if (driver != null) {
            System.out.println("Cerrando navegador...\n");
            driver.quit();
        }
    }

    /**
     * Marca un caso como in-progress, passed o failed
     */
    private void marcarCaso(int numero, String estado) {
        String clase = switch(estado.toLowerCase()) {
            case "in-progress" -> "in-progress";
            case "passed" -> "passed";
            case "failed" -> "failed";
            default -> "";
        };

        js.executeScript(String.format("""
            const casos = document.querySelectorAll('.caso');
            if (casos.length >= %d) {
                const caso = casos[%d];
                // Remover todas las clases de estado
                caso.classList.remove('in-progress', 'passed', 'failed');
                // Agregar la nueva clase
                if ('%s') {
                    caso.classList.add('%s');
                }
            }
            
            // Actualizar barra de progreso
            const completados = document.querySelectorAll('.caso.passed, .caso.failed').length;
            const total = document.querySelectorAll('.caso').length;
            const porcentaje = (completados / total) * 100;
            const progressBar = document.getElementById('progress-bar');
            if (progressBar) {
                progressBar.style.width = porcentaje + '%%';
                progressBar.textContent = Math.round(porcentaje) + '%%';
            }
            """, numero, numero - 1, clase, clase));
    }

    // ====================================================================
    // CASO 1: Elemento que aún NO existe → implicit wait lo encuentra ✅
    // ====================================================================
    @Test
    @Order(1)
    @DisplayName("Caso 1: Elemento que aún no existe → implicit wait ESPERA y lo encuentra ✅")
    void caso1_elementoQueAunNoExiste() {
        marcarCaso(1, "in-progress");
        
        System.out.println("══════════════════════════════════════════════════════════");
        System.out.println("  CASO 1: El elemento aún no existe en el DOM");
        System.out.println("  Se espera: ✅ PASA — implicit wait espera hasta encontrarlo");
        System.out.println("══════════════════════════════════════════════════════════");

        try {
            // Hacemos clic en el botón para iniciar el caso 1
            WebElement btnCaso1 = driver.findElement(By.id("btn-caso1"));
            System.out.println("  Clic en 'Iniciar Caso 1' — el elemento aparecerá en 3 segundos...");
            btnCaso1.click();

            // El elemento 'delayed-element' NO existe todavía en el DOM.
            // JavaScript lo creará después de 3 segundos.
            // implicit wait (10s) poleará el DOM hasta encontrarlo.
            System.out.println("  Buscando elemento que aparecerá en 3 segundos...");
            long inicio = System.currentTimeMillis();

            WebElement elemento = driver.findElement(By.id("delayed-element"));
            long tiempoEspera = System.currentTimeMillis() - inicio;

            assertNotNull(elemento);
            assertEquals("¡Aparecí después de 3 segundos!", elemento.getText());

            System.out.println("  ✅ Elemento encontrado después de ~" + tiempoEspera + " ms");
            System.out.println("  Texto: " + elemento.getText());
            System.out.println("  → Implicit wait FUNCIONÓ: esperó a que el elemento existiera en el DOM.");
            
            marcarCaso(1, "passed");
        } catch (Exception e) {
            marcarCaso(1, "failed");
            throw e;
        } finally {
            try {
                Thread.sleep(2000);  // Pausa para visualizar los cambios
            } catch (InterruptedException ie) {
                // Ignorar la interrupción
            }
        }
    }

    // ====================================================================
    // CASO 2: Elemento oculto (display:none) → no puede interactuar ❌
    // ====================================================================
    @Test
    @Order(2)
    @DisplayName("Caso 2: Elemento oculto → findElement lo halla pero click() FALLA ❌")
    void caso2_elementoOculto() {
        marcarCaso(2, "in-progress");
        
        System.out.println("══════════════════════════════════════════════════════════");
        System.out.println("  CASO 2: El elemento existe pero está oculto (display:none)");
        System.out.println("  Se espera: ❌ FALLA — no se puede hacer clic en él");
        System.out.println("══════════════════════════════════════════════════════════");

        try {
            // No necesitamos recargar — el botón oculto siempre está en el DOM

            // findElement() SÍ encuentra el botón oculto (está en el DOM).
            // Implicit wait busca existencia en el DOM, NO visibilidad.
            WebElement botonOculto = driver.findElement(By.id("hidden-btn"));
            System.out.println("  findElement() encontró el botón oculto (está en el DOM).");

            // Pero al hacer clic: ❌ ElementNotInteractableException
            // → Implicit wait NO verifica que el elemento sea visible o interactuable.
            System.out.println("  Intentando hacer clic en el botón oculto...");
            botonOculto.click();  // ❌ Esto lanzará ElementNotInteractableException
            
            // Si llegamos aquí, algo está mal
            marcarCaso(2, "failed");
            fail("El boton se pudo ejecutar, Se esperaba ElementNotInteractableException");
        } catch (ElementNotInteractableException e) {
            System.out.println("  ❌ ElementNotInteractableException: " + e.getMessage());
            System.out.println("  → Implicit wait NO verifica visibilidad o accesibilidad.");
            marcarCaso(2, "failed");
            throw e;
        } catch (Exception e) {
            marcarCaso(2, "failed");
            throw e;
        } finally {
            try {
                Thread.sleep(2000);  // Pausa para visualizar los cambios
            } catch (InterruptedException ie) {
                // Ignorar la interrupción
            }
        }
    }

    // ====================================================================
    // CASO 3: El texto cambia después → obtiene el texto viejo ❌
    // ====================================================================
    @Test
    @Order(3)
    @DisplayName("Caso 3: Texto cambia después → implicit wait obtiene el texto VIEJO ❌")
    void caso3_textoCambiaDespues() {
        marcarCaso(3, "in-progress");
        
        System.out.println("══════════════════════════════════════════════════════════");
        System.out.println("  CASO 3: El elemento existe con 'Texto inicial'");
        System.out.println("  JavaScript lo cambiará a 'Texto actualizado' en 3 seg");
        System.out.println("  Se espera: ❌ FALLA — obtiene el texto viejo, no el nuevo");
        System.out.println("══════════════════════════════════════════════════════════");

        try {
            // Hacemos clic en el botón para iniciar el caso 3
            WebElement btnCaso3 = driver.findElement(By.id("btn-caso3"));
            System.out.println("  Clic en 'Iniciar Caso 3' — el texto cambiará en 3 segundos...");
            btnCaso3.click();

            // El elemento 'changing-text' YA existe con "Texto inicial".
            // JavaScript cambiará su texto a "Texto actualizado" después de 3 seg.
            // Pero: findElement() lo encuentra INMEDIATAMENTE porque ya existe.
            // Implicit wait NO espera cambios de contenido, solo existencia en DOM.
            WebElement elemento = driver.findElement(By.id("changing-text"));
            String textoObtenido = elemento.getText();

            System.out.println("  Texto obtenido: '" + textoObtenido + "'");
            System.out.println("  Texto esperado: 'Texto actualizado'");

            // ❌ Esto FALLA: obtuvo "Texto inicial" en vez de "Texto actualizado"
            // → Implicit wait NO espera a que el contenido/texto de un elemento cambie.
            assertEquals("Texto actualizado", textoObtenido,
                    "Implicit wait NO espera cambios de texto. Se obtuvo: '" + textoObtenido + "'");
            
            marcarCaso(3, "passed");
        } catch (AssertionError e) {
            System.out.println("  ❌ assertion failed: " + e.getMessage());
            marcarCaso(3, "failed");
            throw e;
        } catch (Exception e) {
            marcarCaso(3, "failed");
            throw e;
        } finally {
            try {
                Thread.sleep(2000);  // Pausa para visualizar los cambios
            } catch (InterruptedException ie) {
                // Ignorar la interrupción
            }
        }
    }

    // ====================================================================
    // CASO 4: Elemento deshabilitado → no puede interactuar ❌
    // ====================================================================
    @Test
    @Order(4)
    @DisplayName("Caso 4: Elemento deshabilitado → findElement lo halla pero NO puede escribir ❌")
    void caso4_elementoDeshabilitado() {
        marcarCaso(4, "in-progress");
        
        System.out.println("══════════════════════════════════════════════════════════");
        System.out.println("  CASO 4: El input existe pero está deshabilitado (disabled)");
        System.out.println("  Se espera: ❌ FALLA — no se puede escribir en él");
        System.out.println("══════════════════════════════════════════════════════════");

        try {
            // No necesitamos recargar — el input deshabilitado siempre está en el DOM

            // findElement() SÍ encuentra el input deshabilitado (está en el DOM).
            WebElement inputDeshabilitado = driver.findElement(By.id("disabled-input"));
            System.out.println("  findElement() encontró el input deshabilitado.");
            System.out.println("  ¿Está habilitado? " + inputDeshabilitado.isEnabled());

            // Intentamos escribir en el campo deshabilitado
            System.out.println("  Intentando escribir 'texto de prueba'...");
            inputDeshabilitado.sendKeys("texto de prueba");

            // Verificamos si el texto se escribió
            String valorActual = inputDeshabilitado.getAttribute("value");
            System.out.println("  Valor del campo después de sendKeys: '" + valorActual + "'");

            // ❌ Esto FALLA: el texto no se escribió porque el campo está deshabilitado.
            // → Implicit wait NO verifica si el elemento está habilitado para interactuar.
            assertEquals("texto de prueba", valorActual,
                    "No se pudo escribir en el campo deshabilitado. Implicit wait NO espera habilitación.");
            
            marcarCaso(4, "passed");
        } catch (ElementNotInteractableException e) {
            System.out.println("  ❌ ElementNotInteractableException: " + e.getMessage());
            System.out.println("  → Implicit wait NO verifica habilitación o interactuabilidad.");
            marcarCaso(4, "failed");
            throw e;
        } catch (AssertionError e) {
            marcarCaso(4, "failed");
            throw e;
        } catch (Exception e) {
            marcarCaso(4, "failed");
            throw e;
        } finally {
            try {
                Thread.sleep(2000);  // Pausa para visualizar los cambios
            } catch (InterruptedException ie) {
                // Ignorar la interrupción
            }
        }
    }
}
