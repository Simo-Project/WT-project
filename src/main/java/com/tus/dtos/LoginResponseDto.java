package com.tus.dtos;

public class LoginResponseDto {

    private String token;
    private String username;
    private String role;
    private String unit;

    public LoginResponseDto() {
    }

    public LoginResponseDto(String token, String username, String role, String unit) {
        this.token = token;
        this.username = username;
        this.role = role;
        this.unit = unit;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }
}