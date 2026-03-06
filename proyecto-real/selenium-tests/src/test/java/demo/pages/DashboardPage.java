package demo.pages;

import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

public class DashboardPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    @FindBy(id = "filter-status")
    private WebElement statusSelect;

    @FindBy(id = "filter-priority")
    private WebElement prioritySelect;

    @FindBy(id = "filter-type")
    private WebElement typeSelect;

    @FindBy(id = "filter-date-from")
    private WebElement dateFromInput;

    public DashboardPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        PageFactory.initElements(driver, this);
    }

    public DashboardPage navigate(String baseUrl) {
        driver.get(baseUrl + "/dashboard");
        return this;
    }

    public DashboardPage filterByStatus(String value) {
        new Select(statusSelect).selectByValue(value);
        return this;
    }

    public DashboardPage filterByPriority(String value) {
        new Select(prioritySelect).selectByValue(value);
        return this;
    }

    public DashboardPage filterByType(String value) {
        new Select(typeSelect).selectByValue(value);
        return this;
    }

    public DashboardPage filterByDateFrom(String date) {
        dateFromInput.sendKeys(date);
        return this;
    }

    public ChangeStateModal clickTicket(String ticketId) {
        WebElement ticketRow = wait.until(
            ExpectedConditions.presenceOfElementLocated(By.id("ticket-" + ticketId)));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", ticketRow);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", ticketRow);
        return new ChangeStateModal(driver);
    }

    public String getSelectedStatus() {
        return new Select(statusSelect).getFirstSelectedOption().getAttribute("value");
    }

    public String getSelectedPriority() {
        return new Select(prioritySelect).getFirstSelectedOption().getAttribute("value");
    }

    public String getSelectedType() {
        return new Select(typeSelect).getFirstSelectedOption().getAttribute("value");
    }

    public String getDateFromValue() {
        return dateFromInput.getAttribute("value");
    }
}
