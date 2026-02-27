package com.tus.dtos;

import com.tus.db.models.Priority;
import com.tus.db.models.RequestStatus;

import java.time.LocalDate;

public class MaintenanceRequestSummaryDto {
    private Long id;
    private LocalDate createdOn;
    private String task;
    private RequestStatus status;
    private Priority priority;
    private String unit;


    public MaintenanceRequestSummaryDto(Long id, LocalDate createdOn, String task, RequestStatus status, Priority priority, String unit) {
        this.id = id;
        this.task = task;
        this.status = status;
        this.priority = priority;
        this.unit = unit;
        this.createdOn = createdOn;
    }

    public Long getId() { return id; }
    public RequestStatus getStatus() { return status; }
    public String getTask() { return task; }
    public Priority getPriority() { return priority; }
    public String getUnit() { return unit; }
    public LocalDate getCreatedOn() { return createdOn; }
}
