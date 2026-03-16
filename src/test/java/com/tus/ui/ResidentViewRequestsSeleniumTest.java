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
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
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
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ResidentViewRequestsSeleniumTest {

    private static final String DB_NAME =
            "resident_ui_" + UUID.randomUUID().toString().replace("-", "");

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () ->
                "jdbc:h2:mem:" + DB_NAME + ";MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false"
        );
    }

    @LocalServerPort
    int port;

    @Autowired
    AppUserRepository userRepo;

    @Autowired
    MaintenanceRequestRepository requestRepo;

    @Autowired
    PasswordEncoder passwordEncoder;

    WebDriver driver;
    WebDriverWait wait;

    @BeforeEach
    void setup() {
        seedData();

        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;

        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--window-size=1400,1000");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");

        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(20));
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
            driver = null;
        }
    }

    @Test
    void residentCanOpenDetailsFromMyRequestsAndAddComment() {
        Long requestId = seedResidentRequest("Selenium resident details request");
        String comment = "Selenium comment " + UUID.randomUUID();

        openResidentRoute("/resident/my-requests");

        waitForRowContaining("#myRequestsTable tbody tr", "Selenium resident details request");
        wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("button.view-request[data-id='" + requestId + "']")
        )).click();

        wait.until(ExpectedConditions.urlContains("/resident/requests/" + requestId));
        wait.until(ExpectedConditions.textToBePresentInElementLocated(
                By.id("detailsCard"),
                "Selenium resident details request"
        ));

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("commentText"))).sendKeys(comment);
        driver.findElement(By.cssSelector("#commentForm button[type='submit']")).click();

        wait.until(ExpectedConditions.textToBePresentInElementLocated(
                By.id("detailsMsg"),
                "Comment added successfully."
        ));
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.id("commentsList"), comment));

        assertTrue(driver.findElement(By.id("detailsCard")).getText().contains("Apt 12"));
    }

    private void seedData() {
        requestRepo.deleteAll();
        userRepo.deleteAll();

        AppUser resident = new AppUser();
        resident.setUsername("resident");
        resident.setPassword(passwordEncoder.encode("resident123"));
        resident.setRole(UserRole.RESIDENT);
        resident.setUnit("Apt 12");
        userRepo.saveAndFlush(resident);
    }

    private Long seedResidentRequest(String task) {
        AppUser resident = userRepo.findByUsername("resident").orElseThrow();

        MaintenanceRequest request = new MaintenanceRequest();
        request.setTask(task);
        request.setCategory(RequestCategory.PLUMBING);
        request.setDescription("Created by resident Selenium test setup");
        request.setStatus(RequestStatus.NEW);
        request.setPriority(Priority.MEDIUM);
        request.setUnit("Apt 12");
        request.setCreatedOn(LocalDate.of(2026, 3, 16));
        request.setCreatedBy(resident);

        return requestRepo.saveAndFlush(request).getId();
    }

    private void openResidentRoute(String path) {
        String token = loginResidentAndGetToken();
        String baseUrl = baseUrl();

        driver.get(baseUrl + "/login.html");
        waitForDocumentReady();

        ((JavascriptExecutor) driver).executeScript(
                "window.localStorage.clear(); window.sessionStorage.clear();"
        );
        ((JavascriptExecutor) driver).executeScript(
                "window.localStorage.setItem('token', arguments[0]);",
                token
        );

        driver.get(baseUrl + path);

        waitForDocumentReady();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("mainView")));
        wait.until(ExpectedConditions.textToBePresentInElementLocated(
                By.id("userInfo"),
                "resident (RESIDENT, Apt 12)"
        ));
    }

    private String loginResidentAndGetToken() {
        return given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "username", "resident",
                        "password", "resident123"
                ))
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getString("token");
    }

    private WebElement waitForRowContaining(String rowCss, String text) {
        return wait.until(driver -> driver.findElements(By.cssSelector(rowCss)).stream()
                .filter(row -> row.getText() != null && row.getText().contains(text))
                .findFirst()
                .orElse(null));
    }

    private void waitForDocumentReady() {
        wait.until(driver -> "complete".equals(
                ((JavascriptExecutor) driver).executeScript("return document.readyState")
        ));
    }

    private String baseUrl() {
        return "http://localhost:" + port;
    }
}
