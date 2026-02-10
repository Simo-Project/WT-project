package com.tus.dtos;

import com.tus.db.models.Priority;
import com.tus.db.models.RequestStatus;

import java.time.LocalDateTime;

public class MaintenanceRequestSummaryDto {
    private Long id;
    private RequestStatus status;
    private Priority priority;
    private String unit;
    private LocalDateTime createdAt;

    public MaintenanceRequestSummaryDto(Long id, RequestStatus status, Priority priority, String unit, LocalDateTime createdAt) {
        this.id = id;
        this.status = status;
        this.priority = priority;
        this.unit = unit;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public RequestStatus getStatus() { return status; }
    public Priority getPriority() { return priority; }
    public String getUnit() { return unit; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
