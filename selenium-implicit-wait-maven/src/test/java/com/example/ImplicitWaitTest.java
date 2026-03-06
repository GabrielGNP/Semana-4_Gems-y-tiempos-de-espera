package demo;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

public class ImplicitWaitTest {

  @Test
  void implicitWait_findsLateElements() throws Exception {
    ChromeOptions options = new ChromeOptions();
    // En contenedores suele ser necesario:
    // options.addArguments("--headless=new"); //<-------- IMPORTANTE: sin esto, el test no encuentra los elementos dinámicos
    options.addArguments("--no-sandbox");
    options.addArguments("--disable-dev-shm-usage");

    // Selenium server (Grid standalone) vive en el servicio "selenium" del compose:
    WebDriver driver = new RemoteWebDriver(
        new java.net.URL("http://selenium:4444/wd/hub"),
        options
    );

    try {
      // Espera implícita: Selenium reintenta findElement/findElements hasta este tiempo
      driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));

      // Sitio web servido por Nginx en el servicio "web"
      driver.get("http://web:80/index.html");

      // Estos elementos NO existen al inicio; aparecen tras ~3s por JS.
      WebElement username = driver.findElement(By.id("username"));
      username.sendKeys("Luis");

      WebElement submit = driver.findElement(By.id("submit"));
      submit.click();

      // Validación simple: el texto de status cambia después del click
      String statusText = driver.findElement(By.id("status")).getText();
      assertTrue(statusText.contains("enviado -> Luis"), "No se reflejó el envío en el status.");
    } finally {
      Thread.sleep(4000); // Para ver el resultado antes de cerrar el navegador
      driver.quit();
    }
  }
}
