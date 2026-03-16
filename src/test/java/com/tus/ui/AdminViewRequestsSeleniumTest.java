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
class AdminViewRequestsSeleniumTest {

    private static final String DB_NAME = "admin_ui_" + UUID.randomUUID();
    private static final String SEEDED_TITLE = "Seeded admin UI request";

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", () ->
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
        options.addArguments("--window-size=1200,800");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");

        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    private void seedData() {
        requestRepo.deleteAll();
        userRepo.deleteAll();

        AppUser admin = new AppUser();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setRole(UserRole.ADMIN);
        admin.setUnit(null);
        userRepo.save(admin);

        MaintenanceRequest request = new MaintenanceRequest();
        request.setTask(SEEDED_TITLE);
        request.setCategory(RequestCategory.PLUMBING);
        request.setDescription("Created by Selenium admin test setup");
        request.setUnit("A1");
        request.setStatus(RequestStatus.NEW);
        request.setPriority(Priority.MEDIUM);
        requestRepo.save(request);
    }

    @AfterEach
    void teardown() {
        if (driver != null) {
            driver.quit();
            driver = null;
        }
    }

    @Test
    void adminCanViewAllRequestsInDataTable() {
        String baseUrl = "http://localhost:" + port;

        driver.get(baseUrl + "/login.html");

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username"))).sendKeys("admin");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("password"))).sendKeys("admin123");
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit']"))).click();

        wait.until(ExpectedConditions.textToBePresentInElementLocated(
                By.id("userInfo"),
                "admin (ADMIN"
        ));

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("requestsTable")));
        wait.until(d -> d.findElements(By.cssSelector("#requestsTable tbody tr")).size() > 0);

        WebElement requestRow = waitForRowContaining("#requestsTable tbody tr", SEEDED_TITLE);
        assertTrue(requestRow.getText().contains(SEEDED_TITLE));
    }

    private WebElement waitForRowContaining(String rowCss, String text) {
        return wait.until(d -> d.findElements(By.cssSelector(rowCss)).stream()
                .filter(row -> row.getText() != null && row.getText().contains(text))
                .findFirst()
                .orElse(null));
    }
}