package com.tus.controllers;

import com.tus.db.models.AppUser;
import com.tus.db.models.UserRole;
import com.tus.db.repos.AppUserRepository;
import com.tus.dtos.UserMeDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerUnitTest {

    @Mock
    private AppUserRepository userRepo;

    @Mock
    private Principal principal;

    private UserController controller;

    @BeforeEach
    void setUp() {
        controller = new UserController(userRepo);
    }

    @Test
    void me_returnsCurrentUserDetails() {
        AppUser user = new AppUser();
        user.setUsername("resident");
        user.setRole(UserRole.RESIDENT);
        user.setUnit("Apt 12");

        when(principal.getName()).thenReturn("resident");
        when(userRepo.findByUsername("resident")).thenReturn(Optional.of(user));

        UserMeDto result = controller.me(principal);

        assertEquals("resident", result.getUsername());
        assertEquals("RESIDENT", result.getRole());
        assertEquals("Apt 12", result.getUnit());

        verify(userRepo).findByUsername("resident");
    }

    @Test
    void me_returnsAdminWithNullUnit() {
        AppUser user = new AppUser();
        user.setUsername("admin");
        user.setRole(UserRole.ADMIN);
        user.setUnit(null);

        when(principal.getName()).thenReturn("admin");
        when(userRepo.findByUsername("admin")).thenReturn(Optional.of(user));

        UserMeDto result = controller.me(principal);

        assertEquals("admin", result.getUsername());
        assertEquals("ADMIN", result.getRole());
        assertNull(result.getUnit());

        verify(userRepo).findByUsername("admin");
    }

    @Test
    void me_throwsWhenUserNotFound() {
        when(principal.getName()).thenReturn("missing");
        when(userRepo.findByUsername("missing")).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> controller.me(principal));

        verify(userRepo).findByUsername("missing");
    }
}