package demo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import demo.pages.ChangeStateModal;
import demo.pages.DashboardPage;

public class DashboardFilterTest {

  private WebDriver driver;

  @BeforeEach
  void setUp() throws InterruptedException {
    ChromeOptions options = new ChromeOptions();
    boolean headless = !"false".equalsIgnoreCase(System.getenv("HEADLESS"));
    // options.addArguments("--headless=new");
    options.addArguments("--no-sandbox");
    options.addArguments("--disable-dev-shm-usage");

    driver = new ChromeDriver(options);
    driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
  }

  @AfterEach
  void tearDown() throws InterruptedException {
    Thread.sleep(10000); 
    if (driver != null) {
      driver.quit();
    }
  }

  @Test
  void filterByEstadoRecibido_PrioridadAlta_TipoSinServicio() throws InterruptedException {
    String baseUrl = System.getenv("BASE_URL") != null ? System.getenv("BASE_URL") : "http://localhost:5173";

    DashboardPage dashboardPage = new DashboardPage(driver);
    dashboardPage.navigate(baseUrl);

    Thread.sleep(10000);

    // 1-4. Aplicar filtros
    dashboardPage
        .filterByStatus("RECEIVED")
        .filterByPriority("HIGH")
        .filterByType("NO_SERVICE")
        .filterByDateFrom("06-03-2026");

    Thread.sleep(2000);

    // 5. Hacer click en el ticket para abrir el modal de cambio de estado
    String ticketId = "fe78659d-2e5c-41f8-94a5-77c808749fb8";
    ChangeStateModal modal = dashboardPage.clickTicket(ticketId);

    Thread.sleep(2000);

    // 6. Seleccionar "En Progreso" en el modal
    modal.selectState("IN_PROGRESS");

    Thread.sleep(2000);

    // Verificar filtros
    assertEquals("RECEIVED", dashboardPage.getSelectedStatus(),
        "El filtro de estado debería ser RECEIVED");
    assertEquals("HIGH", dashboardPage.getSelectedPriority(),
        "El filtro de prioridad debería ser HIGH");
    assertEquals("NO_SERVICE", dashboardPage.getSelectedType(),
        "El filtro de tipo debería ser NO_SERVICE");
    assertEquals("2026-03-06", dashboardPage.getDateFromValue(),
        "El filtro de fecha desde debería ser 2026-03-06");

    // Verificar cambio de estado en el modal
    assertTrue(modal.isDisplayed(), "El modal debería estar visible");
    assertEquals("IN_PROGRESS", modal.getSelectedState(),
        "El estado del ticket debería ser IN_PROGRESS");
  }
}
