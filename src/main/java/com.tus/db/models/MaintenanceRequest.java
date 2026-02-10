package com.tus.db.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "maintenance_request")
public class MaintenanceRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Priority priority;

    @Column(nullable = false)
    private String unit;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public MaintenanceRequest() {}

    public MaintenanceRequest(RequestStatus status, Priority priority, String unit) {
        this.status = status;
        this.priority = priority;
        this.unit = unit;
    }

    @PrePersist
    public void onCreate() {
        if (this.status == null) this.status = RequestStatus.NEW;
        if (this.priority == null) this.priority = Priority.MEDIUM;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public RequestStatus getStatus() { return status; }
    public Priority getPriority() { return priority; }
    public String getUnit() { return unit; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setStatus(RequestStatus status) { this.status = status; }
    public void setPriority(Priority priority) { this.priority = priority; }
    public void setUnit(String unit) { this.unit = unit; }
}
