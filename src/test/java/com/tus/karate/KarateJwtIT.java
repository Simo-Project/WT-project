package com.tus.karate;

import com.intuit.karate.Results;
import com.intuit.karate.Runner;
import com.tus.db.models.AppUser;
import com.tus.db.models.UserRole;
import com.tus.db.repos.AppUserRepository;
import com.tus.db.repos.MaintenanceRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class KarateJwtIT {

    @LocalServerPort
    int port;

    @Autowired
    AppUserRepository userRepo;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    MaintenanceRequestRepository requestRepo;

    @BeforeEach
    void seedUsers() {
        requestRepo.deleteAll();
        userRepo.deleteAll();

        userRepo.save(user("admin", "admin123", UserRole.ADMIN, null));
        userRepo.save(user("resident", "resident123", UserRole.RESIDENT, "Apt 12"));
    }

    private AppUser user(String username, String rawPassword, UserRole role, String unit) {
        AppUser u = new AppUser();
        u.setUsername(username);
        u.setPassword(passwordEncoder.encode(rawPassword));
        u.setRole(role);
        u.setUnit(unit);
        return u;
    }

    @Test
    void runKarateAuthTests() {
        System.setProperty("baseUrl", "http://localhost:" + port);

        Results results = Runner.path("classpath:karate")
                .parallel(1);

        assertEquals(0, results.getFailCount(), results.getErrorMessages());
    }
}