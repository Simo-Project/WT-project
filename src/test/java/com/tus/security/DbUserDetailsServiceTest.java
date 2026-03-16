package com.tus.security;

import com.tus.db.models.AppUser;
import com.tus.db.models.UserRole;
import com.tus.db.repos.AppUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DbUserDetailsServiceTest {

    @Mock
    private AppUserRepository repo;

    private DbUserDetailsService service;

    @BeforeEach
    void setUp() {
        service = new DbUserDetailsService(repo);
    }

    @Test
    void loadUserByUsername_returnsSpringUserWithRoleAuthority() {
        AppUser user = new AppUser();
        user.setUsername("admin");
        user.setPassword("$2a$10$encoded");
        user.setRole(UserRole.ADMIN);

        when(repo.findByUsername("admin")).thenReturn(Optional.of(user));

        UserDetails result = service.loadUserByUsername("admin");

        assertEquals("admin", result.getUsername());
        assertEquals("$2a$10$encoded", result.getPassword());

        Set<String> authorities = result.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        assertTrue(authorities.contains("ROLE_ADMIN"));
        verify(repo).findByUsername("admin");
    }

    @Test
    void loadUserByUsername_returnsResidentRoleAuthority() {
        AppUser user = new AppUser();
        user.setUsername("resident");
        user.setPassword("encoded-password");
        user.setRole(UserRole.RESIDENT);

        when(repo.findByUsername("resident")).thenReturn(Optional.of(user));

        UserDetails result = service.loadUserByUsername("resident");

        assertEquals("resident", result.getUsername());
        assertEquals("encoded-password", result.getPassword());
        assertTrue(result.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_RESIDENT"::equals));

        verify(repo).findByUsername("resident");
    }

    @Test
    void loadUserByUsername_throwsUsernameNotFoundException_whenUserMissing() {
        when(repo.findByUsername("missing")).thenReturn(Optional.empty());

        UsernameNotFoundException ex = assertThrows(
                UsernameNotFoundException.class,
                () -> service.loadUserByUsername("missing")
        );

        assertEquals("User not found: missing", ex.getMessage());
        verify(repo).findByUsername("missing");
    }
}