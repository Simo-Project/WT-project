package com.tus.services;

import com.tus.db.models.MaintenanceRequest;
import com.tus.db.models.Priority;
import com.tus.db.models.RequestCategory;
import com.tus.db.models.RequestStatus;
import com.tus.db.repos.MaintenanceRequestRepository;
import com.tus.dtos.MaintenanceRequestSummaryDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(MaintenanceRequestService.class)
@ActiveProfiles("test")
class MaintenanceRequestServiceIntegrationTest {

    @Autowired
    private MaintenanceRequestRepository repo;

    @Autowired
    private MaintenanceRequestService service;

    @BeforeEach
    void setUp() {
        repo.deleteAll();
        repo.flush();

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

    @Test
    void list_withStatusOnly_returnsMatchingRequests() {
        List<MaintenanceRequestSummaryDto> results = service.list(Optional.of(RequestStatus.NEW), Optional.empty());
        assertEquals(1, results.size());
        assertEquals(RequestStatus.NEW, results.get(0).getStatus());
    }
}