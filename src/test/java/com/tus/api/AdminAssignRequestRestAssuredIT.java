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
class AdminAssignRequestRestAssuredIT {

    private static final String DB_NAME =
            "mr5_api_" + UUID.randomUUID().toString().replace("-", "");

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
    private Long staff1Id;
    private Long residentId;

    @BeforeEach
    void setup() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;

        requestRepo.deleteAll();
        userRepo.deleteAll();

        userRepo.save(user("admin", "admin123", UserRole.ADMIN, null));
        AppUser resident = userRepo.save(user("resident", "resident123", UserRole.RESIDENT, "Apt 12"));
        AppUser staff1 = userRepo.save(user("staff1", "staff123", UserRole.STAFF, null));
        userRepo.save(user("staff2", "staff123", UserRole.STAFF, null));

        residentId = resident.getId();
        staff1Id = staff1.getId();

        MaintenanceRequest mr = new MaintenanceRequest();
        mr.setTask("MR-5 API assign");
        mr.setCategory(RequestCategory.ELECTRICAL);
        mr.setDescription("Seeded for assignment API tests");
        mr.setUnit("Apt 12");
        mr.setStatus(RequestStatus.NEW);
        mr.setPriority(Priority.HIGH);
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
    void adminCanAssignRequest_andListAndDetailReflectAssignment() {
        SessionFilter adminSession = login("admin", "admin123");

        given()
                .filter(adminSession)
                .contentType("application/json")
                .body(Map.of("staffUserId", staff1Id))
                .when()
                .patch("/api/admin/requests/{id}/assign", requestId)
                .then()
                .statusCode(200)
                .body("id", equalTo(requestId.intValue()))
                .body("assignedToUsername", equalTo("staff1"));

        given()
                .filter(adminSession)
                .when()
                .get("/api/admin/requests/{id}", requestId)
                .then()
                .statusCode(200)
                .body("assignedToUsername", equalTo("staff1"));

        given()
                .filter(adminSession)
                .when()
                .get("/api/admin/requests")
                .then()
                .statusCode(200)
                .body(String.format("find { it.id == %d }.assignedToUsername", requestId), equalTo("staff1"));
    }

    @Test
    void adminCanUnassignRequest() {
        SessionFilter adminSession = login("admin", "admin123");

        given()
                .filter(adminSession)
                .contentType("application/json")
                .body(Map.of("staffUserId", staff1Id))
                .when()
                .patch("/api/admin/requests/{id}/assign", requestId)
                .then()
                .statusCode(200)
                .body("assignedToUsername", equalTo("staff1"));

        given()
                .filter(adminSession)
                .contentType("application/json")
                .body("{\"staffUserId\":null}")
                .when()
                .patch("/api/admin/requests/{id}/assign", requestId)
                .then()
                .statusCode(200)
                .body("assignedToUsername", nullValue());

        given()
                .filter(adminSession)
                .when()
                .get("/api/admin/requests/{id}", requestId)
                .then()
                .statusCode(200)
                .body("assignedToUsername", nullValue());
    }

    @Test
    void adminCannotAssignNonStaffUser_returnsBadRequest() {
        SessionFilter adminSession = login("admin", "admin123");

        given()
                .filter(adminSession)
                .contentType("application/json")
                .body(Map.of("staffUserId", residentId))
                .when()
                .patch("/api/admin/requests/{id}/assign", requestId)
                .then()
                .statusCode(400);
    }

    @Test
    void residentCannotAssignRequest_forbidden() {
        SessionFilter residentSession = login("resident", "resident123");

        given()
                .filter(residentSession)
                .contentType("application/json")
                .body(Map.of("staffUserId", staff1Id))
                .when()
                .patch("/api/admin/requests/{id}/assign", requestId)
                .then()
                .statusCode(403);
    }

    @Test
    void staffUsersEndpoint_returnsOnlyStaffUsers() {
        SessionFilter adminSession = login("admin", "admin123");

        given()
                .filter(adminSession)
                .when()
                .get("/api/admin/requests/staff")
                .then()
                .statusCode(200)
                .body("username", hasItems("staff1", "staff2"))
                .body("username", not(hasItems("admin", "resident")));
    }

    @Test
    void adminCannotAssignCancelledRequest_returnsConflict() {
        SessionFilter adminSession = login("admin", "admin123");

        MaintenanceRequest request = requestRepo.findById(requestId).orElseThrow();
        request.setStatus(RequestStatus.CANCELLED);
        requestRepo.saveAndFlush(request);

        given()
                .filter(adminSession)
                .contentType("application/json")
                .body(Map.of("staffUserId", staff1Id))
                .when()
                .patch("/api/admin/requests/{id}/assign", requestId)
                .then()
                .statusCode(409)
                .body("message", equalTo("Cannot assign a cancelled request"));
    }
}


