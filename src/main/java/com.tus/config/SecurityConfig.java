package com.tus.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http.csrf(csrf -> csrf.disable());

        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/index.html", "/login", "/error",
                        "/styles.css", "/script.js").permitAll()

                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/requests/**").hasRole("RESIDENT")

                .anyRequest().authenticated()
        );

        http.formLogin(form -> form.defaultSuccessUrl("/", true));
        http.logout(logout -> logout.logoutSuccessUrl("/login?logout"));

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}