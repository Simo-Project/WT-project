package com.tus.api;

import com.tus.db.models.MaintenanceRequest;
import com.tus.db.models.Priority;
import com.tus.db.models.RequestStatus;
import com.tus.db.repos.MaintenanceRequestRepository;
import com.tus.db.models.RequestCategory;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class AdminRequestsRestAssuredIT {

    @LocalServerPort
    int port;

    @Autowired
    MaintenanceRequestRepository repo;

    @BeforeEach
    void setup() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;

        repo.deleteAll();

        repo.save(make("Replace smoke detector", RequestStatus.NEW, Priority.HIGH, "Apt 12", LocalDate.of(2026, 2, 10)));
        repo.save(make("Fix broken air vent", RequestStatus.IN_PROGRESS, Priority.MEDIUM, "Apt 3", LocalDate.of(2026, 2, 9)));
        repo.save(make("Paint", RequestStatus.CLOSED, Priority.LOW, "Apt 8", LocalDate.of(2026, 2, 8)));
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

    private Map<String, String> loginAsAdmin() {
        return given()
                .redirects().follow(false)
                .contentType(ContentType.URLENC)
                .formParam("username", "admin")
                .formParam("password", "admin123")
                .when()
                .post("/login")
                .then()
                .statusCode(anyOf(is(302), is(303)))
                .extract()
                .cookies();
    }

    @Test
    void unauthenticatedUserIsRedirectedToLogin() {
        given()
                .redirects().follow(false)
                .when()
                .get("/api/admin/requests")
                .then()
                .statusCode(anyOf(is(302), is(303)))
                .header("Location", containsString("/login"));
    }

    @Test
    void adminCanViewAllRequests() {
        Map<String, String> cookies = loginAsAdmin();

        given()
                .cookies(cookies)
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
        Map<String, String> cookies = loginAsAdmin();

        given()
                .cookies(cookies)
                .queryParam("status", "NEW")
                .queryParam("priority", "HIGH")
                .when()
                .get("/api/admin/requests")
                .then()
                .statusCode(200)
                .body("size()", is(1))
                .body("[0].task", equalTo("Replace smoke detector"))
                .body("[0].status", equalTo("NEW"))
                .body("[0].priority", equalTo("HIGH"))
                .body("[0].unit", equalTo("Apt 12"));
    }
}