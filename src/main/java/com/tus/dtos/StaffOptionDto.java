package com.tus.dtos;

public class StaffOptionDto {
    private Long id;
    private String username;

    public StaffOptionDto(Long id, String username) {
        this.id = id;
        this.username = username;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }
}