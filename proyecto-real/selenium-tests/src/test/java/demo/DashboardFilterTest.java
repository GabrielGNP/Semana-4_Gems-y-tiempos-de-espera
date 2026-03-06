package demo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

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
    // El frontend corre en Docker en el puerto 80
    String baseUrl = System.getenv("BASE_URL") != null ? System.getenv("BASE_URL") : "http://localhost:5173";
    driver.get(baseUrl + "/dashboard");

    Thread.sleep(10000); 

    // 1. Seleccionar Estado: Recibida (value="RECEIVED")
    Select statusSelect = new Select(driver.findElement(By.id("filter-status")));
    statusSelect.selectByValue("RECEIVED");
    Thread.sleep(2000); 

    // 2. Seleccionar Prioridad: Alta (value="HIGH")
    Select prioritySelect = new Select(driver.findElement(By.id("filter-priority")));
    prioritySelect.selectByValue("HIGH");
    Thread.sleep(2000); 

    // 3. Seleccionar Tipo de Incidente: Sin servicio (value="NO_SERVICE")
    Select typeSelect = new Select(driver.findElement(By.id("filter-type")));
    typeSelect.selectByValue("NO_SERVICE");
    Thread.sleep(2000);

    // 4. Ingresar fecha desde: 06-03-2026 (formato requerido por input[type=date]: yyyy-MM-dd)
    WebElement dateFromInput = driver.findElement(By.id("filter-date-from"));
    dateFromInput.sendKeys("06-03-2026");
    Thread.sleep(2000);

    // 5. Hacer click en el ticket para abrir el modal de cambio de estado
    String ticketId = "fe78659d-2e5c-41f8-94a5-77c808749fb8"; 
    WebElement ticketRow = driver.findElement(By.id("ticket-" + ticketId));
    ticketRow.click();

    // 6. Seleccionar "En Progreso" (value="IN_PROGRESS") en el select de cambio de estado
    WebElement changeStateSelect = driver.findElement(By.id("change-state-select"));
    Select stateSelect = new Select(changeStateSelect);
    stateSelect.selectByValue("IN_PROGRESS");
    Thread.sleep(1000);

    
    assertEquals("RECEIVED", statusSelect.getFirstSelectedOption().getAttribute("value"),
        "El filtro de estado debería ser RECEIVED");
    assertEquals("HIGH", prioritySelect.getFirstSelectedOption().getAttribute("value"),
        "El filtro de prioridad debería ser HIGH");
    assertEquals("NO_SERVICE", typeSelect.getFirstSelectedOption().getAttribute("value"),
        "El filtro de tipo debería ser NO_SERVICE");
    assertEquals("2026-03-06", dateFromInput.getAttribute("value"),
        "El filtro de fecha desde debería ser 2026-03-06");
    assertEquals("IN_PROGRESS", stateSelect.getFirstSelectedOption().getAttribute("value"),
        "El estado del ticket debería ser IN_PROGRESS");
  }
}
