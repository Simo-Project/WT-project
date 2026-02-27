package com.tus.ui;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AdminRequestsSeleniumTest {

    @LocalServerPort
    int port;

    WebDriver driver;
    WebDriverWait wait;

    @BeforeEach
    void setup() {
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new"); // remove this if you want to see the browser
        options.addArguments("--window-size=1200,800");

        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(8));
    }

    @AfterEach
    void teardown() {
        if (driver != null) driver.quit();
    }

    @Test
    void adminCanViewAllRequestsInDataTable() {
        String baseUrl = "http://localhost:" + port;

        driver.get(baseUrl + "/");

        WebElement username = wait.until(d -> d.findElement(By.name("username")));
        WebElement password = driver.findElement(By.name("password"));

        username.sendKeys("admin");
        password.sendKeys("admin123");
        password.submit();

        WebElement viewBtn = wait.until(d -> d.findElement(By.id("viewAllBtn")));
        viewBtn.click();

        wait.until(d -> d.findElements(By.cssSelector("#requestsTable tbody tr")).size() > 0);

        int rowCount = driver.findElements(By.cssSelector("#requestsTable tbody tr")).size();
        assertTrue(rowCount > 0, "Expected DataTable to show at least 1 request row");
    }
}
