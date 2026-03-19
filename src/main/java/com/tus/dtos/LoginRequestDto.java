package com.tus.dtos;

public class LoginRequestDto {

    private String username;
    private String password;

    public LoginRequestDto() {
        // Required by Jackson for JSON deserialization.
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}