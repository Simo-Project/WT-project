package com.tus.dtos;

import com.tus.db.models.AppUser;
import com.tus.db.models.Priority;
import com.tus.db.models.RequestCategory;
import com.tus.db.models.RequestStatus;

import java.time.LocalDate;

public class MaintenanceRequestDetailsDto {
    private Long id;
    private LocalDate createdOn;
    private String task;
    private RequestCategory category;
    private String description;
    private RequestStatus status;
    private Priority priority;
    private String unit;
    private String assignedToUsername;

    public MaintenanceRequestDetailsDto(Long id, LocalDate createdOn, String task,
                                        RequestCategory category, String description,
                                        RequestStatus status, Priority priority, String unit, String assignedToUsername) {
        this.id = id;
        this.createdOn = createdOn;
        this.task = task;
        this.category = category;
        this.description = description;
        this.status = status;
        this.priority = priority;
        this.unit = unit;
        this.assignedToUsername = assignedToUsername;
    }

    public Long getId() { return id; }
    public LocalDate getCreatedOn() { return createdOn; }
    public String getTask() { return task; }
    public RequestCategory getCategory() { return category; }
    public String getDescription() { return description; }
    public RequestStatus getStatus() { return status; }
    public Priority getPriority() { return priority; }
    public String getUnit() { return unit; }
    public String getAssignedToUsername() { return assignedToUsername; }
}