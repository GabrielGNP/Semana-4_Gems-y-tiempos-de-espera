package com.example;

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
    // options.addArguments("--headless=new")
    // ChromeDriver local (Selenium Manager descarga chromedriver automáticamente)
    WebDriver driver = new ChromeDriver(options);
    
    options.addArguments("--no-sandbox");
    options.addArguments("--disable-dev-shm-usage");

    WebDriver driver = new RemoteWebDriver(
        new java.net.URL("http://selenium:4444/wd/hub"),
        options
    );

    try {
      // Espera implícita: Selenium reintenta findElement/findElements hasta este tiempo
      driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(1));

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
      Thread.sleep(60000);
    } finally {

      Thread.sleep(4000); // Para ver el resultado antes de cerrar el navegador (opcional)

      driver.quit();
    }
  }
}
