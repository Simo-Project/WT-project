package com.tus.dtos;

public class UserMeDto {
    private String username;
    private String role;
    private String unit;

    public UserMeDto(String username, String role, String unit) {
        this.username = username;
        this.role = role;
        this.unit = unit;
    }

    public String getUsername() { return username; }
    public String getRole() { return role; }
    public String getUnit() { return unit; }
}