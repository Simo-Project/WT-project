package com.tus.db.models;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "maintenance_request")
public class MaintenanceRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String task;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Priority priority;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestCategory category;

    @Column(nullable = false)
    private String unit;

    @Column(name = "created_on", nullable = false, updatable = false)
    private LocalDate createdOn;

    @Column(nullable = false, length = 1000)
    private String description;

    public MaintenanceRequest() {}

    public MaintenanceRequest(String task, RequestStatus status, Priority priority, String unit, String createdOn) {
        this.task = task;
        this.status = status;
        this.priority = priority;
        this.unit = unit;
    }

    @PrePersist
    public void onCreate() {
        if (this.status == null) this.status = RequestStatus.NEW;
        if (this.priority == null) this.priority = Priority.MEDIUM;
        this.createdOn = LocalDate.now();
    }

    public Long getId() { return id; }
    public String getTask() { return task; }
    public RequestStatus getStatus() { return status; }
    public Priority getPriority() { return priority; }
    public String getUnit() { return unit; }
    public LocalDate getCreatedOn() { return createdOn; }
    public RequestCategory getCategory() { return category; }
    public String getDescription() { return description; }

    public void setTask(String task) { this.task = task; }
    public void setStatus(RequestStatus status) { this.status = status; }
    public void setPriority(Priority priority) { this.priority = priority; }
    public void setUnit(String unit) { this.unit = unit; }
    public void setCreatedOn(LocalDate createdOn) { this.createdOn = createdOn; }
    public void setCategory(RequestCategory category) { this.category = category; }
    public void setDescription(String description) { this.description = description; }
}
