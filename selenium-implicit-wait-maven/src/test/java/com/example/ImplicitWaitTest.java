package demo;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class ImplicitWaitTest {

  @Test
  void implicitWait_findsLateElements() throws Exception {
    ChromeOptions options = new ChromeOptions();
    options.addArguments("--headless=new");
    options.addArguments("--no-sandbox");
    options.addArguments("--disable-dev-shm-usage");

    // ChromeDriver local (Selenium Manager descarga chromedriver automáticamente)
    WebDriver driver = new ChromeDriver(options);

    try {
      // Espera implícita: Selenium reintenta findElement/findElements hasta este tiempo
      driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));

      // Cargar el HTML local con file://
      Path htmlPath = Paths.get("web", "index.html").toAbsolutePath();
      driver.get(htmlPath.toUri().toString());

      // Estos elementos NO existen al inicio; aparecen tras ~3s por JS.
      WebElement username = driver.findElement(By.id("username"));
      username.sendKeys("Luis");

      WebElement submit = driver.findElement(By.id("submit"));
      submit.click();

      // Validación simple: el texto de status cambia después del click
      String statusText = driver.findElement(By.id("status")).getText();
      assertTrue(statusText.contains("enviado -> Luis"), "No se reflejó el envío en el status.");
    } finally {
      driver.quit();
    }
  }
}
