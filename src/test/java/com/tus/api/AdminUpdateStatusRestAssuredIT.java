package com.tus.api;

import com.tus.db.models.*;
import com.tus.db.repos.AppUserRepository;
import com.tus.db.repos.MaintenanceRequestRepository;
import io.restassured.RestAssured;
import io.restassured.filter.session.SessionFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
class AdminUpdateStatusRestAssuredIT {

    private static final String DB_NAME =
            "mr4_ra_" + UUID.randomUUID().toString().replace("-", "");

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

    private Long requestId;

    @BeforeEach
    void setup() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;

        requestRepo.deleteAll();
        userRepo.deleteAll();

        userRepo.save(user("admin", "admin123", UserRole.ADMIN, null));
        userRepo.save(user("resident", "resident123", UserRole.RESIDENT, "Apt 12"));

        MaintenanceRequest mr = new MaintenanceRequest();
        mr.setTask("MR-4 RestAssured status update");
        mr.setCategory(RequestCategory.PLUMBING);
        mr.setDescription("Seeded for MR-4 RestAssured tests");
        mr.setUnit("Apt 12");
        mr.setStatus(RequestStatus.NEW);
        mr.setPriority(Priority.MEDIUM);

        requestId = requestRepo.saveAndFlush(mr).getId();
    }

    private AppUser user(String username, String rawPassword, UserRole role, String unit) {
        AppUser u = new AppUser();
        u.setUsername(username);
        u.setPassword(passwordEncoder.encode(rawPassword));
        u.setRole(role);
        u.setUnit(unit);
        return u;
    }

    private SessionFilter login(String username, String password) {
        SessionFilter session = new SessionFilter();

        given()
                .filter(session)
                .contentType("application/x-www-form-urlencoded")
                .formParam("username", username)
                .formParam("password", password)
                .redirects().follow(false)
                .when()
                .post("/login")
                .then()
                .statusCode(anyOf(is(302), is(303)));

        return session;
    }

    @Test
    void adminCanPatchStatus_andDbAndListReflectChange() {
        SessionFilter adminSession = login("admin", "admin123");

        given()
                .filter(adminSession)
                .contentType("application/json")
                .body(Map.of("status", "IN_PROGRESS"))
                .when()
                .patch("/api/admin/requests/{id}/status", requestId)
                .then()
                .statusCode(200)
                .body("id", equalTo(requestId.intValue()))
                .body("status", equalTo("IN_PROGRESS"));

        RequestStatus dbStatus = requestRepo.findById(requestId).orElseThrow().getStatus();
        org.junit.jupiter.api.Assertions.assertEquals(RequestStatus.IN_PROGRESS, dbStatus);

        given()
                .filter(adminSession)
                .when()
                .get("/api/admin/requests")
                .then()
                .statusCode(200)
                .body(String.format("find { it.id == %d }.status", requestId), equalTo("IN_PROGRESS"));
    }

    @Test
    void residentCannotPatchStatus_forbidden() {
        SessionFilter residentSession = login("resident", "resident123");

        given()
                .filter(residentSession)
                .contentType("application/json")
                .body(Map.of("status", "CLOSED"))
                .when()
                .patch("/api/admin/requests/{id}/status", requestId)
                .then()
                .statusCode(403);
    }
}