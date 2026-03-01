package com.tus.ui;

import com.tus.db.models.*;
import com.tus.db.models.Priority;
import com.tus.db.repos.AppUserRepository;
import com.tus.db.repos.MaintenanceRequestRepository;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.*;
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
class AdminRequestsSeleniumTest {

    private static final String DB_NAME = "admin_ui_" + UUID.randomUUID();

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
        ensureAdminAndRequestsExist();

        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--window-size=1200,800");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");

        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    private void ensureAdminAndRequestsExist() {
        AppUser admin = userRepo.findByUsername("admin").orElseGet(AppUser::new);
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setRole(UserRole.ADMIN);
        admin.setUnit(null);
        userRepo.save(admin);

        if (requestRepo.count() == 0) {
            MaintenanceRequest r = new MaintenanceRequest();
            r.setTask("Seeded admin UI request");
            r.setCategory(RequestCategory.PLUMBING);
            r.setDescription("Created by Selenium admin test setup");
            r.setUnit("A1");
            r.setStatus(RequestStatus.NEW);
            r.setPriority(Priority.MEDIUM);
            requestRepo.save(r);
        }
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

        WebElement username = wait.until(d -> d.findElement(By.name("username")));
        WebElement password = driver.findElement(By.name("password"));

        username.sendKeys("admin");
        password.sendKeys("admin123");
        password.submit();

        wait.until(d -> d.findElements(By.cssSelector("#requestsTable")).size() > 0);

        wait.until(d -> d.findElements(By.cssSelector("#requestsTable tbody tr")).size() > 0);

        int rowCount = driver.findElements(By.cssSelector("#requestsTable tbody tr")).size();
        assertTrue(rowCount > 0, "Expected DataTable to show at least 1 request row");
    }
}