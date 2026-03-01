package com.tus.services;

import com.tus.db.models.*;
import com.tus.db.repos.MaintenanceRequestRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class MaintenanceRequestServiceUpdateStatusIntegrationTest {

    private static final String DB_NAME =
            "mr4_service_" + UUID.randomUUID().toString().replace("-", "");

    @DynamicPropertySource
    static void dbProps(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", () ->
                "jdbc:h2:mem:" + DB_NAME + ";MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false"
        );
    }

    @Autowired MaintenanceRequestService service;
    @Autowired MaintenanceRequestRepository repo;

    @Test
    void updateStatus_persistsNewValue() {
        MaintenanceRequest mr = new MaintenanceRequest();
        mr.setTask("Service status update");
        mr.setCategory(RequestCategory.OTHER);
        mr.setDescription("test");
        mr.setUnit("Apt 12");
        mr.setStatus(RequestStatus.NEW);
        mr.setPriority(Priority.MEDIUM);

        Long id = repo.saveAndFlush(mr).getId();

        service.updateStatus(id, RequestStatus.CLOSED);

        assertEquals(RequestStatus.CLOSED, repo.findById(id).orElseThrow().getStatus());
    }
}