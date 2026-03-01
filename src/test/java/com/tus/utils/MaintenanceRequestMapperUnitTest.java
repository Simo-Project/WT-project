package com.tus.utils;

import com.tus.db.models.*;
import com.tus.dtos.MaintenanceRequestSummaryDto;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class MaintenanceRequestMapperUnitTest {

    @Test
    void toSummary_mapsFieldsCorrectly() {
        MaintenanceRequest mr = new MaintenanceRequest();
        mr.setTask("Fix broken air vent");
        mr.setUnit("Apt 3");
        mr.setStatus(RequestStatus.IN_PROGRESS);
        mr.setPriority(Priority.MEDIUM);
        mr.setCategory(RequestCategory.HEATING);
        mr.setDescription("Vent is loose");
        mr.setCreatedOn(LocalDate.of(2026, 2, 9));

        MaintenanceRequestSummaryDto dto = MaintenanceRequestMapper.toSummary(mr);

        assertEquals("Fix broken air vent", dto.getTask());
        assertEquals("Apt 3", dto.getUnit());
        assertEquals(RequestStatus.IN_PROGRESS, dto.getStatus());
        assertEquals(Priority.MEDIUM, dto.getPriority());
        assertEquals(LocalDate.of(2026, 2, 9), dto.getCreatedOn());
    }
}