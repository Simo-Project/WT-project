package com.tus.db.models;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "request_comment")
public class RequestComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "request_id", nullable = false)
    private MaintenanceRequest request;

    @ManyToOne(optional = false)
    @JoinColumn(name = "author_user_id", nullable = false)
    private AppUser author;

    @Column(nullable = false, length = 1000)
    private String text;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public RequestComment() {
    }

    public RequestComment(MaintenanceRequest request, AppUser author, String text) {
        this.request = request;
        this.author = author;
        this.text = text;
    }

    @PrePersist
    public void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public MaintenanceRequest getRequest() {
        return request;
    }

    public AppUser getAuthor() {
        return author;
    }

    public String getText() {
        return text;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setRequest(MaintenanceRequest request) {
        this.request = request;
    }

    public void setAuthor(AppUser author) {
        this.author = author;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}