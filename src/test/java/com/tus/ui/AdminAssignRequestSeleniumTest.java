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
import org.openqa.selenium.support.ui.Select;
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
class AdminAssignRequestSeleniumTest {

    private static final String DB_NAME =
            "admin_assign_ui_" + UUID.randomUUID().toString().replace("-", "");

    private static final String REQUEST_TITLE = "Selenium admin assign request";

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
        options.addArguments("--window-size=1400,900");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");

        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(20));
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

        AppUser staff1 = new AppUser();
        staff1.setUsername("staff1");
        staff1.setPassword(passwordEncoder.encode("staff123"));
        staff1.setRole(UserRole.STAFF);
        staff1.setUnit(null);
        userRepo.save(staff1);

        MaintenanceRequest request = new MaintenanceRequest();
        request.setTask(REQUEST_TITLE);
        request.setCategory(RequestCategory.PLUMBING);
        request.setDescription("Created for admin assignment UI test");
        request.setUnit("Apt 12");
        request.setStatus(RequestStatus.NEW);
        request.setPriority(Priority.MEDIUM);
        requestRepo.saveAndFlush(request);
    }

    @AfterEach
    void teardown() {
        if (driver != null) {
            driver.quit();
            driver = null;
        }
    }

    @Test
    void adminCanAssignStaffFromRequestDetailsPage() {
        String baseUrl = "http://localhost:" + port;

        loginAsAdmin(baseUrl);

        WebElement requestRow = waitForRowContaining("#requestsTable tbody tr", REQUEST_TITLE);
        WebElement viewButton = requestRow.findElement(By.cssSelector("button.view-request"));
        wait.until(ExpectedConditions.elementToBeClickable(viewButton)).click();

        wait.until(ExpectedConditions.urlContains("/admin/requests/"));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("detailsCard")));

        WebElement staffSelect = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("staffSelect")));
        wait.until(d -> d.findElements(By.cssSelector("#staffSelect option")).size() > 1);

        new Select(staffSelect).selectByVisibleText("staff1");
        wait.until(ExpectedConditions.elementToBeClickable(By.id("assignBtn"))).click();

        wait.until(ExpectedConditions.textToBePresentInElementLocated(
                By.id("detailsMsg"),
                "Assignment updated successfully."
        ));
        wait.until(ExpectedConditions.textToBePresentInElementLocated(
                By.id("detailsCard"),
                "staff1"
        ));

        driver.navigate().refresh();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("detailsCard")));
        assertTrue(driver.findElement(By.id("detailsCard")).getText().contains("staff1"));
    }

    private void loginAsAdmin(String baseUrl) {
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
    }

    private WebElement waitForRowContaining(String rowCss, String text) {
        return wait.until(d -> d.findElements(By.cssSelector(rowCss)).stream()
                .filter(row -> row.getText() != null && row.getText().contains(text))
                .findFirst()
                .orElse(null));
    }
}