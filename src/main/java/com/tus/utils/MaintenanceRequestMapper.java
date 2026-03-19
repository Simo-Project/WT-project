package com.tus.utils;

import com.tus.db.models.MaintenanceRequest;
import com.tus.dtos.MaintenanceRequestSummaryDto;

public class MaintenanceRequestMapper {

    private MaintenanceRequestMapper() {
        throw new IllegalStateException("Utility class");
    }

    public static MaintenanceRequestSummaryDto toSummary(MaintenanceRequest mr) {
        return new MaintenanceRequestSummaryDto(
                mr.getId(),
                mr.getCreatedOn(),
                mr.getTask(),
                mr.getStatus(),
                mr.getPriority(),
                mr.getUnit(),
                mr.getAssignedTo() != null ? mr.getAssignedTo().getUsername() : null
        );
    }
}