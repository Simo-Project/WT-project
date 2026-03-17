package com.tus.controllers;

import com.tus.db.models.AppUser;
import com.tus.db.repos.AppUserRepository;
import com.tus.dtos.UserMeDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@RestController
public class UserController {

    private final AppUserRepository users;

    public UserController(AppUserRepository users) {
        this.users = users;
    }

    @GetMapping("/api/user/me")
    public UserMeDto me(Principal principal) {
        if (principal == null) {
            throw new ResponseStatusException(UNAUTHORIZED, "Not logged in");
        }

        AppUser user = users.findByUsername(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found"));

        return new UserMeDto(
                user.getUsername(),
                user.getRole().name(),
                user.getUnit()
        );
    }
}