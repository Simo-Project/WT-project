package com.tus.services;

import com.tus.db.repos.AppUserRepository;
import org.springframework.stereotype.Service;

@Service
public class ResidentUnitService {

    private final AppUserRepository users;

    public ResidentUnitService(AppUserRepository users) {
        this.users = users;
    }

    public String getUnitForUsername(String username) {
        return users.findByUsername(username)
                .map(u -> u.getUnit())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
    }
}