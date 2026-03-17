package com.tus.dtos;

import com.tus.db.models.Priority;
import com.tus.db.models.RequestCategory;
import com.tus.db.models.RequestStatus;

import java.time.LocalDate;
import java.util.List;

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
    private List<RequestCommentDto> comments;

    public MaintenanceRequestDetailsDto() {
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
    public List<RequestCommentDto> getComments() { return comments; }

    public void setId(Long id) { this.id = id; }
    public void setCreatedOn(LocalDate createdOn) { this.createdOn = createdOn; }
    public void setTask(String task) { this.task = task; }
    public void setCategory(RequestCategory category) { this.category = category; }
    public void setDescription(String description) { this.description = description; }
    public void setStatus(RequestStatus status) { this.status = status; }
    public void setPriority(Priority priority) { this.priority = priority; }
    public void setUnit(String unit) { this.unit = unit; }
    public void setAssignedToUsername(String assignedToUsername) { this.assignedToUsername = assignedToUsername; }
    public void setComments(List<RequestCommentDto> comments) { this.comments = comments; }
}