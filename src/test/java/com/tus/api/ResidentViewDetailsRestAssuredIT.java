package com.tus.api;

import com.tus.db.models.*;
import com.tus.db.repos.AppUserRepository;
import com.tus.db.repos.MaintenanceRequestRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ResidentViewDetailsRestAssuredIT {

    private static final String DB_NAME =
            "ra_view_" + UUID.randomUUID().toString().replace("-", "");

    @DynamicPropertySource
    static void dbProps(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", () ->
                "jdbc:h2:mem:" + DB_NAME + ";MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false"
        );
    }

    @LocalServerPort
    int port;

    @Autowired AppUserRepository userRepo;
    @Autowired MaintenanceRequestRepository requestRepo;
    @Autowired PasswordEncoder passwordEncoder;

    @BeforeEach
    void setup() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;

        requestRepo.deleteAll();
        userRepo.deleteAll();

        AppUser resident = new AppUser();
        resident.setUsername("resident");
        resident.setPassword(passwordEncoder.encode("resident123"));
        resident.setRole(UserRole.RESIDENT);
        resident.setUnit("Apt 12");
        userRepo.save(resident);

        MaintenanceRequest other = new MaintenanceRequest();
        other.setTask("Other unit request");
        other.setCategory(RequestCategory.OTHER);
        other.setDescription("Should not be accessible to Apt 12");
        other.setUnit("Apt 99");
        other.setStatus(RequestStatus.NEW);
        other.setPriority(Priority.LOW);
        requestRepo.saveAndFlush(other);
    }

    private String loginResident() {
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

    @Test
    void residentCanCreateAndThenViewDetailsById() {
        String residentToken = loginResident();

        Long id =
                given()
                        .header("Authorization", "Bearer " + residentToken)
                        .contentType(ContentType.JSON)
                        .body(Map.of(
                                "title", "RestAssured view test",
                                "category", "PLUMBING",
                                "description", "Created in RestAssured test"
                        ))
                        .when()
                        .post("/api/requests")
                        .then()
                        .statusCode(200)
                        .body("id", notNullValue())
                        .body("task", equalTo("RestAssured view test"))
                        .body("unit", equalTo("Apt 12"))
                        .extract()
                        .jsonPath()
                        .getLong("id");

        given()
                .header("Authorization", "Bearer " + residentToken)
                .when()
                .get("/api/requests/{id}", id)
                .then()
                .statusCode(200)
                .body("id", equalTo(id.intValue()))
                .body("task", equalTo("RestAssured view test"))
                .body("category", equalTo("PLUMBING"))
                .body("description", equalTo("Created in RestAssured test"))
                .body("unit", equalTo("Apt 12"))
                .body("status", notNullValue())
                .body("priority", notNullValue())
                .body("createdOn", notNullValue());
    }

    @Test
    void residentCannotViewOtherUnitsRequest() {
        String residentToken = loginResident();

        Long otherId = requestRepo.findAll().stream()
                .filter(r -> "Apt 99".equals(r.getUnit()))
                .findFirst()
                .orElseThrow()
                .getId();

        given()
                .header("Authorization", "Bearer " + residentToken)
                .when()
                .get("/api/requests/{id}", otherId)
                .then()
                .statusCode(403);
    }
}