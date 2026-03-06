package demo.pages;

import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

public class ChangeStateModal {

    private final WebDriver driver;
    private final WebDriverWait wait;

    @FindBy(id = "change-state-select")
    private WebElement stateSelect;

    public ChangeStateModal(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(5));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[role='dialog']")));
        PageFactory.initElements(driver, this);
    }

    public ChangeStateModal selectState(String value) {
        new Select(stateSelect).selectByValue(value);
        return this;
    }

    public String getSelectedState() {
        return new Select(stateSelect).getFirstSelectedOption().getAttribute("value");
    }

    public boolean isDisplayed() {
        return driver.findElement(By.cssSelector("[role='dialog']")).isDisplayed();
    }
}
