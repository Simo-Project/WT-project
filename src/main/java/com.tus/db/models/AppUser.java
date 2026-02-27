package com.tus.db.models;

import jakarta.persistence.*;

@Entity
@Table(name = "app_user")
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    // For residents (admin can be null)
    @Column
    private String unit;

    public AppUser() {}

    public AppUser(String username, String password, UserRole role, String unit) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.unit = unit;
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public UserRole getRole() { return role; }
    public String getUnit() { return unit; }

    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }
    public void setRole(UserRole role) { this.role = role; }
    public void setUnit(String unit) { this.unit = unit; }
}