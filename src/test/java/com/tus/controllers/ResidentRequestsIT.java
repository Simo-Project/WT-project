package com.tus.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tus.db.models.*;
import com.tus.db.repos.AppUserRepository;
import com.tus.db.repos.MaintenanceRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ResidentRequestsIT {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @Autowired AppUserRepository userRepo;
    @Autowired MaintenanceRequestRepository requestRepo;
    @Autowired PasswordEncoder passwordEncoder;

    @BeforeEach
    void setup() {
        requestRepo.deleteAll();
        userRepo.deleteAll();

        userRepo.save(makeUser("admin", "admin123", UserRole.ADMIN, null));
        userRepo.save(makeUser("resident", "resident123", UserRole.RESIDENT, "Apt 12"));
        userRepo.save(makeUser("resident2", "resident123", UserRole.RESIDENT, "Apt 3"));
    }

    private AppUser makeUser(String username, String rawPassword, UserRole role, String unit) {
        AppUser u = new AppUser();
        u.setUsername(username);
        u.setPassword(passwordEncoder.encode(rawPassword));
        u.setRole(role);
        u.setUnit(unit);
        return u;
    }

    private MockHttpSession login(String username, String password) throws Exception {
        MvcResult loginResult = mockMvc.perform(post("/login")
                        .param("username", username)
                        .param("password", password))
                .andExpect(status().is3xxRedirection())
                .andReturn();

        return (MockHttpSession) loginResult.getRequest().getSession(false);
    }

    @Test
    void createRequest_savesWithResidentsOwnUnit_andDefaultsStatusNew() throws Exception {
        MockHttpSession session = login("resident", "resident123");

        Map<String, Object> body = new HashMap<>();
        body.put("title", "Leaking tap");
        body.put("category", "PLUMBING");
        body.put("description", "Kitchen tap is dripping constantly");

        mockMvc.perform(post("/api/requests")
                        .session(session)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.task").value("Leaking tap"))
                .andExpect(jsonPath("$.unit").value("Apt 12"))
                .andExpect(jsonPath("$.status").value("NEW"));

        assertEquals(1, requestRepo.count());
        MaintenanceRequest saved = requestRepo.findAll().get(0);
        assertEquals("Apt 12", saved.getUnit());
        assertEquals(RequestStatus.NEW, saved.getStatus());
    }

    @Test
    void createRequest_validationFails_returns400WithClearMessage() throws Exception {
        MockHttpSession session = login("resident", "resident123");

        String badJson = """
            {"title":"","category":null,"description":""}
        """;

        mockMvc.perform(post("/api/requests")
                        .session(session)
                        .contentType(APPLICATION_JSON)
                        .content(badJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.errors").exists());
    }

    @Test
    void myRequests_returnsOnlyRequestsCreatedByResident() throws Exception {
        MockHttpSession session = login("resident", "resident123");

        AppUser resident = userRepo.findByUsername("resident").orElseThrow();
        AppUser resident2 = userRepo.findByUsername("resident2").orElseThrow();

        requestRepo.save(makeRequest("Fix heater", "Apt 12", RequestStatus.NEW, resident));
        requestRepo.save(makeRequest("Paint wall", "Apt 3", RequestStatus.NEW, resident2));

        mockMvc.perform(get("/api/requests/my").session(session))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].unit").value("Apt 12"))
                .andExpect(jsonPath("$[0].task").value("Fix heater"));
    }

    private MaintenanceRequest makeRequest(String task, String unit, RequestStatus status, AppUser createdBy) {
        MaintenanceRequest mr = new MaintenanceRequest();
        mr.setTask(task);
        mr.setUnit(unit);
        mr.setCategory(RequestCategory.OTHER);
        mr.setDescription("test");
        mr.setPriority(Priority.MEDIUM);
        mr.setStatus(status);
        mr.setCreatedOn(LocalDate.of(2026, 2, 28));
        mr.setCreatedBy(createdBy);
        return mr;
    }

    @Test
    void residentCanCancelOwnNewRequest() throws Exception {
        MockHttpSession session = login("resident", "resident123");

        AppUser resident = userRepo.findByUsername("resident").orElseThrow();
        MaintenanceRequest request = requestRepo.saveAndFlush(
                makeRequest("Fix heater", "Apt 12", RequestStatus.NEW, resident)
        );

        mockMvc.perform(patch("/api/requests/{id}/cancel", request.getId())
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(request.getId()))
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        assertEquals(RequestStatus.CANCELLED,
                requestRepo.findById(request.getId()).orElseThrow().getStatus());

        mockMvc.perform(get("/api/requests/my").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void residentCannotCancelOtherResidentsRequest() throws Exception {
        MockHttpSession session = login("resident", "resident123");

        AppUser resident2 = userRepo.findByUsername("resident2").orElseThrow();
        MaintenanceRequest request = requestRepo.saveAndFlush(
                makeRequest("Paint wall", "Apt 3", RequestStatus.NEW, resident2)
        );

        mockMvc.perform(patch("/api/requests/{id}/cancel", request.getId())
                        .session(session))
                .andExpect(status().isForbidden());

        assertEquals(RequestStatus.NEW,
                requestRepo.findById(request.getId()).orElseThrow().getStatus());
    }

    @Test
    void residentCannotCancelOwnRequest_whenAlreadyInProgress() throws Exception {
        MockHttpSession session = login("resident", "resident123");

        AppUser resident = userRepo.findByUsername("resident").orElseThrow();
        MaintenanceRequest request = requestRepo.saveAndFlush(
                makeRequest("Fix heater", "Apt 12", RequestStatus.IN_PROGRESS, resident)
        );

        mockMvc.perform(patch("/api/requests/{id}/cancel", request.getId())
                        .session(session))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Only requests with status NEW can be cancelled"));

        assertEquals(RequestStatus.IN_PROGRESS,
                requestRepo.findById(request.getId()).orElseThrow().getStatus());
    }
}