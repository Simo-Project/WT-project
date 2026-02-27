package com.tus.security;

import com.tus.db.models.AppUser;
import com.tus.db.repos.AppUserRepository;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
public class DbUserDetailsService implements UserDetailsService {

    private final AppUserRepository repo;

    public DbUserDetailsService(AppUserRepository repo) {
        this.repo = repo;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AppUser user = repo.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return User.withUsername(user.getUsername())
                .password(user.getPassword())
                .roles(user.getRole().name()) // ADMIN / RESIDENT
                .build();
    }
}