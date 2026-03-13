package com.tus.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tus.db.models.*;
import com.tus.db.repos.AppUserRepository;
import com.tus.db.repos.MaintenanceRequestRepository;
import com.tus.db.repos.RequestCommentRepository;
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
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RequestCommentsIT {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @Autowired AppUserRepository userRepo;
    @Autowired MaintenanceRequestRepository requestRepo;
    @Autowired RequestCommentRepository commentRepo;
    @Autowired PasswordEncoder passwordEncoder;

    private AppUser admin;
    private AppUser resident;
    private AppUser resident2;

    @BeforeEach
    void setup() {
        commentRepo.deleteAll();
        requestRepo.deleteAll();
        userRepo.deleteAll();

        admin = userRepo.save(makeUser("admin", "admin123", UserRole.ADMIN, null));
        resident = userRepo.save(makeUser("resident", "resident123", UserRole.RESIDENT, "Apt 12"));
        resident2 = userRepo.save(makeUser("resident2", "resident123", UserRole.RESIDENT, "Apt 3"));
    }

    private AppUser makeUser(String username, String rawPassword, UserRole role, String unit) {
        AppUser u = new AppUser();
        u.setUsername(username);
        u.setPassword(passwordEncoder.encode(rawPassword));
        u.setRole(role);
        u.setUnit(unit);
        return u;
    }

    private MaintenanceRequest makeRequest(String task, String unit, RequestStatus status, AppUser createdBy) {
        MaintenanceRequest mr = new MaintenanceRequest();
        mr.setTask(task);
        mr.setUnit(unit);
        mr.setCategory(RequestCategory.OTHER);
        mr.setDescription("test");
        mr.setPriority(Priority.MEDIUM);
        mr.setStatus(status);
        mr.setCreatedOn(LocalDate.of(2026, 3, 11));
        mr.setCreatedBy(createdBy);
        return requestRepo.saveAndFlush(mr);
    }

    private RequestComment makeComment(MaintenanceRequest request, AppUser author, String text, LocalDateTime createdAt) {
        RequestComment c = new RequestComment();
        c.setRequest(request);
        c.setAuthor(author);
        c.setText(text);
        c.setCreatedAt(createdAt);
        return commentRepo.saveAndFlush(c);
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
    void residentCanAddCommentToOwnRequest_andSeeItInHistory() throws Exception {
        MockHttpSession session = login("resident", "resident123");
        AppUser resident = userRepo.findByUsername("resident").orElseThrow();
        MaintenanceRequest request = requestRepo.save(
                makeRequest("Fix heater", "Apt 12", RequestStatus.NEW, resident)
        );

        Map<String, Object> body = new HashMap<>();
        body.put("text", "Please call before arriving.");

        mockMvc.perform(post("/api/requests/{id}/comments", request.getId())
                        .session(session)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.authorUsername").value("resident"))
                .andExpect(jsonPath("$.text").value("Please call before arriving."))
                .andExpect(jsonPath("$.createdAt").exists());

        assertEquals(1, commentRepo.count());
        RequestComment saved = commentRepo.findAll().get(0);
        assertEquals("Please call before arriving.", saved.getText());
        assertEquals("resident", saved.getAuthor().getUsername());
        assertEquals(request.getId(), saved.getRequest().getId());
        assertNotNull(saved.getCreatedAt());

        mockMvc.perform(get("/api/requests/{id}", request.getId()).session(session))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.comments.length()").value(1))
                .andExpect(jsonPath("$.comments[0].authorUsername").value("resident"))
                .andExpect(jsonPath("$.comments[0].text").value("Please call before arriving."));
    }

    @Test
    void residentCannotCommentOnOtherUnitsRequest() throws Exception {
        MockHttpSession session = login("resident", "resident123");
        MaintenanceRequest otherUnitsRequest = makeRequest("Paint wall", "Apt 3", RequestStatus.NEW, resident2);

        Map<String, Object> body = new HashMap<>();
        body.put("text", "Trying to comment on another unit");

        mockMvc.perform(post("/api/requests/{id}/comments", otherUnitsRequest.getId())
                        .session(session)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isForbidden());

        assertEquals(0, commentRepo.count());
    }

    @Test
    void residentCommentValidationFails_whenEmpty() throws Exception {
        MockHttpSession session = login("resident", "resident123");
        MaintenanceRequest request = makeRequest("Fix sink", "Apt 12", RequestStatus.NEW, resident);

        mockMvc.perform(post("/api/requests/{id}/comments", request.getId())
                        .session(session)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"text":"   "}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors.text").value("Comment cannot be empty"));

        assertEquals(0, commentRepo.count());
    }

    @Test
    void commentsAreReturnedInChronologicalOrder() throws Exception {
        MockHttpSession session = login("resident", "resident123");
        MaintenanceRequest request = makeRequest("Door issue", "Apt 12", RequestStatus.NEW, resident);

        makeComment(request, resident, "Older comment", LocalDateTime.of(2026, 3, 10, 9, 0));
        makeComment(request, resident, "Newer comment", LocalDateTime.of(2026, 3, 10, 10, 0));

        mockMvc.perform(get("/api/requests/{id}/comments", request.getId()).session(session))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].text").value("Older comment"))
                .andExpect(jsonPath("$[1].text").value("Newer comment"));
    }

    @Test
    void adminCanCommentOnAnyRequest_andSeeItInDetailHistory() throws Exception {
        MockHttpSession adminSession = login("admin", "admin123");
        MaintenanceRequest request = makeRequest("Electrical fault", "Apt 3", RequestStatus.NEW, resident);

        Map<String, Object> body = new HashMap<>();
        body.put("text", "Engineer booked for tomorrow.");

        mockMvc.perform(post("/api/admin/requests/{id}/comments", request.getId())
                        .session(adminSession)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.authorUsername").value("admin"))
                .andExpect(jsonPath("$.text").value("Engineer booked for tomorrow."));

        assertEquals(1, commentRepo.count());

        mockMvc.perform(get("/api/admin/requests/{id}", request.getId()).session(adminSession))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.comments.length()").value(1))
                .andExpect(jsonPath("$.comments[0].authorUsername").value("admin"))
                .andExpect(jsonPath("$.comments[0].text").value("Engineer booked for tomorrow."));
    }
}