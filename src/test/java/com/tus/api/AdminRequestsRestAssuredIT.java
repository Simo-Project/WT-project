package com.tus.api;

import com.tus.db.models.*;
import com.tus.db.repos.AppUserRepository;
import com.tus.db.repos.MaintenanceRequestRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class AdminRequestsRestAssuredIT {

    private static final String DB_NAME = "admin_requests_" + UUID.randomUUID().toString().replace("-", "");

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () ->
                "jdbc:h2:mem:" + DB_NAME + ";MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false"
        );
    }

    @LocalServerPort
    int port;

    @Autowired
    MaintenanceRequestRepository repo;

    @Autowired
    AppUserRepository userRepo;

    @Autowired
    PasswordEncoder passwordEncoder;

    @BeforeEach
    void setup() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;

        repo.deleteAll();
        userRepo.deleteAll();

        userRepo.save(makeUser("admin", "admin123", UserRole.ADMIN, null));

        repo.save(make("Replace smoke detector", RequestStatus.NEW, Priority.HIGH, "Apt 12", LocalDate.of(2026, 2, 10)));
        repo.save(make("Fix broken air vent", RequestStatus.IN_PROGRESS, Priority.MEDIUM, "Apt 3", LocalDate.of(2026, 2, 9)));
        repo.save(make("Paint", RequestStatus.CLOSED, Priority.LOW, "Apt 8", LocalDate.of(2026, 2, 8)));
    }

    private AppUser makeUser(String username, String rawPassword, UserRole role, String unit) {
        AppUser u = new AppUser();
        u.setUsername(username);
        u.setPassword(passwordEncoder.encode(rawPassword));
        u.setRole(role);
        u.setUnit(unit);
        return u;
    }

    private MaintenanceRequest make(String task, RequestStatus status, Priority priority, String unit, LocalDate createdOn) {
        MaintenanceRequest mr = new MaintenanceRequest();
        mr.setTask(task);
        mr.setCategory(RequestCategory.OTHER);
        mr.setDescription("Test description");
        mr.setStatus(status);
        mr.setPriority(priority);
        mr.setUnit(unit);
        mr.setCreatedOn(createdOn);
        return mr;
    }

    private String loginAsAdmin() {
        return given()
                .contentType(ContentType.JSON)
                .body("""
                  {
                    "username": "admin",
                    "password": "admin123"
                  }
                  """)
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getString("token");
    }

    @Test
    void unauthenticatedUserGets401() {
        given()
                .when()
                .get("/api/admin/requests")
                .then()
                .statusCode(401);
    }

    @Test
    void adminCanViewAllRequests() {
        String token = loginAsAdmin();

        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/api/admin/requests")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("size()", is(3))
                .body("task", hasItems("Replace smoke detector", "Fix broken air vent", "Paint"))
                .body("status", hasItems("NEW", "IN_PROGRESS", "CLOSED"))
                .body("priority", hasItems("HIGH", "MEDIUM", "LOW"))
                .body("unit", hasItems("Apt 12", "Apt 3", "Apt 8"));
    }

    @Test
    void adminCanFilterByStatusAndPriority() {
        String token = loginAsAdmin();

        given()
                .header("Authorization", "Bearer " + token)
                .queryParam("status", "NEW")
                .queryParam("priority", "HIGH")
                .when()
                .get("/api/admin/requests")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("size()", is(1))
                .body("[0].task", equalTo("Replace smoke detector"))
                .body("[0].status", equalTo("NEW"))
                .body("[0].priority", equalTo("HIGH"))
                .body("[0].unit", equalTo("Apt 12"));
    }
}
