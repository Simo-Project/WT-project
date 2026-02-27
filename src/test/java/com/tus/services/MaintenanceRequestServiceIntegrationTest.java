package com.tus.services;

import com.tus.db.models.MaintenanceRequest;
import com.tus.db.models.Priority;
import com.tus.db.models.RequestStatus;
import com.tus.db.repos.MaintenanceRequestRepository;
import com.tus.dtos.MaintenanceRequestSummaryDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(MaintenanceRequestService.class)
@ActiveProfiles("test")
class MaintenanceRequestServiceIntegrationTest {

    @Autowired
    private MaintenanceRequestRepository repository;

    @Autowired
    private MaintenanceRequestService service;

    @BeforeEach
    void setUp() {
        repository.deleteAll();

        repository.save(make("Replace smoke detector", RequestStatus.NEW,         Priority.HIGH,   "Apt 12"));
        repository.save(make("Fix broken air vent",  RequestStatus.IN_PROGRESS, Priority.MEDIUM, "Apt 3"));
        repository.save(make("Paint",                RequestStatus.CLOSED,      Priority.LOW,    "Apt 8"));
    }

    private MaintenanceRequest make(String task, RequestStatus status, Priority priority, String unit) {
        MaintenanceRequest mr = new MaintenanceRequest();
        mr.setTask(task);
        mr.setStatus(status);
        mr.setPriority(priority);
        mr.setUnit(unit);
        return mr;
    }

    @Test
    void list_withoutFilters_returnsAllRequests() {
        List<MaintenanceRequestSummaryDto> results = service.list(Optional.empty(), Optional.empty());
        assertEquals(3, results.size());
    }

    @Test
    void list_withStatusOnly_returnsMatchingRequests() {
        List<MaintenanceRequestSummaryDto> results = service.list(Optional.of(RequestStatus.NEW), Optional.empty());
        assertEquals(1, results.size());
        assertEquals(RequestStatus.NEW, results.get(0).getStatus());
    }

    @Test
    void list_withStatusAndPriority_returnsMatchingRequests() {
        List<MaintenanceRequestSummaryDto> results = service.list(Optional.of(RequestStatus.IN_PROGRESS), Optional.of(Priority.MEDIUM));
        assertEquals(1, results.size());
        assertEquals("Fix broken air vent", results.get(0).getTask());
    }
}