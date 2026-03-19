package com.tus.db.models;

import com.tus.dtos.MaintenanceRequestSummaryDto;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class MaintenanceRequestUnitTest {

    @Test
    void onCreate_setsDefaults_whenMissing() {
        MaintenanceRequest mr = new MaintenanceRequest();
        mr.setTask("Replace smoke detector");
        mr.setUnit("Apt 12");

        mr.onCreate();

        assertEquals(RequestStatus.NEW, mr.getStatus());
        assertEquals(Priority.MEDIUM, mr.getPriority());
        assertNotNull(mr.getCreatedOn());
    }

    @Test
    void onCreate_doesNotOverwriteProvidedValues() {
        MaintenanceRequest mr = new MaintenanceRequest();
        mr.setTask("Paint");
        mr.setUnit("Apt 8");
        mr.setStatus(RequestStatus.CLOSED);
        mr.setPriority(Priority.HIGH);
        mr.setCreatedOn(LocalDate.now());

        mr.onCreate();

        assertEquals(RequestStatus.CLOSED, mr.getStatus());
        assertEquals(Priority.HIGH, mr.getPriority());
        assertEquals(LocalDate.now(), mr.getCreatedOn());
    }

    @Test
    void gettersReturnExpectedValues() {
        MaintenanceRequestSummaryDto dto = new MaintenanceRequestSummaryDto(
                1L,
                LocalDate.of(2026, 2, 10),
                "Replace smoke detector",
                RequestStatus.NEW,
                Priority.HIGH,
                "Apt 12",
                "s"
        );

        assertEquals(1L, dto.getId());
        assertEquals(LocalDate.of(2026, 2, 10), dto.getCreatedOn());
        assertEquals("Replace smoke detector", dto.getTask());
        assertEquals(RequestStatus.NEW, dto.getStatus());
        assertEquals(Priority.HIGH, dto.getPriority());
        assertEquals("Apt 12", dto.getUnit());
    }
}
