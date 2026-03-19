package com.tus.dtos;

import java.time.LocalDateTime;

public class RequestCommentDto {

    private Long id;
    private String authorUsername;
    private String text;
    private LocalDateTime createdAt;

    public RequestCommentDto(Long id, String authorUsername, String text, LocalDateTime createdAt) {
        this.id = id;
        this.authorUsername = authorUsername;
        this.text = text;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getAuthorUsername() {
        return authorUsername;
    }

    public String getText() {
        return text;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}