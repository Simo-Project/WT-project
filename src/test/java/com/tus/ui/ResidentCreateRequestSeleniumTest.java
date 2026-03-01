package com.tus.ui;

import com.tus.db.models.AppUser;
import com.tus.db.models.UserRole;
import com.tus.db.repos.AppUserRepository;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
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
class ResidentCreateRequestSeleniumTest {

    private static final String DB_NAME = "resident_ui_" + UUID.randomUUID().toString().replace("-", "");

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
    PasswordEncoder passwordEncoder;

    @BeforeEach
    void setup() {
        ensureTestUsersExist();

        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--window-size=1200,800");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");

        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(30));
    }

    private void ensureTestUsersExist() {
        AppUser resident = userRepo.findByUsername("resident").orElseGet(AppUser::new);
        resident.setUsername("resident");
        resident.setPassword(passwordEncoder.encode("resident123"));
        resident.setRole(UserRole.RESIDENT);
        resident.setUnit("Apt 12");
        userRepo.save(resident);

        AppUser admin = userRepo.findByUsername("admin").orElseGet(AppUser::new);
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setRole(UserRole.ADMIN);
        admin.setUnit(null);
        userRepo.save(admin);
    }

    @AfterEach
    void teardown() {
        if (driver != null) {
            driver.quit();
            driver = null;
        }
    }

    @Test
    void residentCanCreateRequestAndSeeItInMyRequestsTable() {
        String baseUrl = "http://localhost:" + port;
        String uniqueTitle = "Selenium request " + System.currentTimeMillis();

        try {
            driver.get(baseUrl + "/login.html");

            wait.until(ExpectedConditions.presenceOfElementLocated(By.name("username"))).sendKeys("resident");
            driver.findElement(By.name("password")).sendKeys("resident123");
            driver.findElement(By.cssSelector("button[type='submit']")).click();

            WebElement userInfo = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("userInfo")));
            wait.until(ExpectedConditions.textToBePresentInElement(userInfo, "resident"));

            wait.until(d -> {
                WebElement s = d.findElement(By.id("viewScript"));
                String src = s.getAttribute("src");
                return src != null && src.contains("/views/resident/create-request.js");
            });

            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("createRequestForm")));
            driver.findElement(By.id("title")).sendKeys(uniqueTitle);
            new Select(driver.findElement(By.id("category"))).selectByValue("PLUMBING");
            driver.findElement(By.id("description")).sendKeys("Created by Selenium UI test");
            driver.findElement(By.cssSelector("#createRequestForm button[type='submit']")).click();

            wait.until(ExpectedConditions.urlContains("/resident/my-requests"));
            wait.until(d -> {
                WebElement s = d.findElement(By.id("viewScript"));
                String src = s.getAttribute("src");
                return src != null && src.contains("/views/resident/my-requests.js");
            });

            wait.until(ExpectedConditions.presenceOfElementLocated(By.id("myRequestsTable_wrapper")));

            WebElement searchBox = wait.until(
                    ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#myRequestsTable_filter input"))
            );
            searchBox.clear();
            searchBox.sendKeys(uniqueTitle);

            wait.until(d -> d.findElements(By.cssSelector("#myRequestsTable tbody tr")).stream()
                    .anyMatch(tr -> tr.getText() != null && tr.getText().contains(uniqueTitle)));

            boolean found = driver.findElements(By.cssSelector("#myRequestsTable tbody tr")).stream()
                    .anyMatch(tr -> tr.getText() != null && tr.getText().contains(uniqueTitle));

            assertTrue(found, "Expected to find newly created request title in My Requests table");

        } catch (TimeoutException te) {
            System.out.println("TIMEOUT URL: " + driver.getCurrentUrl());
            String page = driver.getPageSource();
            System.out.println("PAGE (first 2000 chars): " + page.substring(0, Math.min(2000, page.length())));
        }
    }
}