package com.tus.ui;

import com.tus.db.models.AppUser;
import com.tus.db.models.MaintenanceRequest;
import com.tus.db.models.Priority;
import com.tus.db.models.RequestCategory;
import com.tus.db.models.RequestStatus;
import com.tus.db.models.UserRole;
import com.tus.db.repos.AppUserRepository;
import com.tus.db.repos.MaintenanceRequestRepository;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.time.Duration;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ResidentViewRequestDetailsSeleniumTest {

    private static final String DB_NAME =
            "resident_view_ui_" + UUID.randomUUID().toString().replace("-", "");

    private static final String REQUEST_TITLE = "Leaking kitchen sink";

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () ->
                "jdbc:h2:mem:" + DB_NAME + ";MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false"
        );
    }

    @LocalServerPort
    int port;

    WebDriver driver;
    WebDriverWait wait;

    @Autowired
    AppUserRepository userRepo;

    @Autowired
    MaintenanceRequestRepository requestRepo;

    @Autowired
    PasswordEncoder passwordEncoder;

    @BeforeEach
    void setup() {
        seedData();

        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--window-size=1400,900");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");

        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(20));
    }

    private void seedData() {
        requestRepo.deleteAll();
        userRepo.deleteAll();

        AppUser resident = new AppUser();
        resident.setUsername("resident");
        resident.setPassword(passwordEncoder.encode("resident123"));
        resident.setRole(UserRole.RESIDENT);
        resident.setUnit("Apt 12");
        resident = userRepo.save(resident);

        MaintenanceRequest request = new MaintenanceRequest();
        request.setTask(REQUEST_TITLE);
        request.setCategory(RequestCategory.PLUMBING);
        request.setDescription("Water leaking under sink");
        request.setUnit("Apt 12");
        request.setStatus(RequestStatus.NEW);
        request.setPriority(Priority.MEDIUM);
        request.setCreatedBy(resident);
        requestRepo.save(request);
    }

    @AfterEach
    void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    void residentCanOpenMyRequestsAndViewRequestDetails() {
        String baseUrl = "http://localhost:" + port;

        driver.get(baseUrl + "/login.html");

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username")))
                .sendKeys("resident");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("password")))
                .sendKeys("resident123");
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit']")))
                .click();

        wait.until(ExpectedConditions.textToBePresentInElementLocated(
                By.id("userInfo"),
                "resident (RESIDENT"
        ));

        wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("button.dropdown-toggle")
        )).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("roleMenu")));

        wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("#roleMenu a[href='/resident/my-requests']")
        )).click();

        wait.until(ExpectedConditions.urlContains("/resident/my-requests"));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("myRequestsTable")));
        wait.until(d -> d.findElements(By.cssSelector("#myRequestsTable tbody tr")).size() > 0);

        WebElement row = wait.until(d -> d.findElements(By.cssSelector("#myRequestsTable tbody tr")).stream()
                .filter(r -> r.getText().contains(REQUEST_TITLE))
                .findFirst()
                .orElse(null));

        row.findElement(By.cssSelector("button.view-request")).click();

        wait.until(ExpectedConditions.urlContains("/resident/requests/"));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("detailsCard")));

        String detailsText = driver.findElement(By.id("detailsCard")).getText();
        assertTrue(detailsText.contains(REQUEST_TITLE));
        assertTrue(detailsText.contains("PLUMBING"));
        assertTrue(detailsText.contains("Apt 12"));
    }
}