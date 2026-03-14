package com.tus.services;

import com.tus.db.models.AppUser;
import com.tus.db.models.MaintenanceRequest;
import com.tus.db.models.RequestComment;
import com.tus.db.models.UserRole;
import com.tus.db.repos.AppUserRepository;
import com.tus.db.repos.MaintenanceRequestRepository;
import com.tus.db.repos.RequestCommentRepository;
import com.tus.dtos.RequestCommentDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RequestCommentServiceUnitTest {

    @Mock
    private RequestCommentRepository commentRepo;

    @Mock
    private MaintenanceRequestRepository requestRepo;

    @Mock
    private AppUserRepository userRepo;

    private RequestCommentService service;

    @BeforeEach
    void setUp() {
        service = new RequestCommentService(commentRepo, requestRepo, userRepo);
    }

    @Test
    void addResidentComment_savesCommentAndReturnsDto_whenResidentOwnsRequest() {
        AppUser resident = user("resident", UserRole.RESIDENT);
        setId(resident, 10L);

        MaintenanceRequest request = request(resident);

        LocalDateTime createdAt = LocalDateTime.of(2026, 3, 14, 12, 0);

        when(userRepo.findByUsername("resident")).thenReturn(Optional.of(resident));
        when(requestRepo.findById(1L)).thenReturn(Optional.of(request));
        when(commentRepo.save(any(RequestComment.class))).thenAnswer(inv -> {
            RequestComment saved = inv.getArgument(0);
            saved.setCreatedAt(createdAt);
            return saved;
        });

        RequestCommentDto result = service.addResidentComment(1L, "resident", "Please call before arriving");

        assertEquals("resident", result.getAuthorUsername());
        assertEquals("Please call before arriving", result.getText());
        assertEquals(createdAt, result.getCreatedAt());

        ArgumentCaptor<RequestComment> captor = ArgumentCaptor.forClass(RequestComment.class);
        verify(commentRepo).save(captor.capture());

        RequestComment savedComment = captor.getValue();
        assertSame(request, savedComment.getRequest());
        assertSame(resident, savedComment.getAuthor());
        assertEquals("Please call before arriving", savedComment.getText());
    }

    @Test
    void addResidentComment_throwsUnauthorized_whenUserNotFound() {
        when(userRepo.findByUsername("resident")).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> service.addResidentComment(1L, "resident", "Hello")
        );

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
        assertEquals("User not found", ex.getReason());

        verify(requestRepo, never()).findById(any());
        verify(commentRepo, never()).save(any());
    }

    @Test
    void addResidentComment_throwsNotFound_whenRequestMissing() {
        AppUser resident = user("resident", UserRole.RESIDENT);
        setId(resident, 10L);

        when(userRepo.findByUsername("resident")).thenReturn(Optional.of(resident));
        when(requestRepo.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> service.addResidentComment(1L, "resident", "Hello")
        );

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        assertEquals("Request not found", ex.getReason());

        verify(commentRepo, never()).save(any());
    }

    @Test
    void addResidentComment_throwsForbidden_whenResidentDoesNotOwnRequest() {
        AppUser resident = user("resident", UserRole.RESIDENT);
        setId(resident, 10L);

        AppUser otherResident = user("resident2", UserRole.RESIDENT);
        setId(otherResident, 20L);

        MaintenanceRequest request = request(otherResident);

        when(userRepo.findByUsername("resident")).thenReturn(Optional.of(resident));
        when(requestRepo.findById(1L)).thenReturn(Optional.of(request));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> service.addResidentComment(1L, "resident", "Hello")
        );

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        assertEquals("Not allowed to comment on this request", ex.getReason());

        verify(commentRepo, never()).save(any());
    }

    @Test
    void addAdminComment_savesCommentAndReturnsDto_whenUserIsAdmin() {
        AppUser admin = user("admin", UserRole.ADMIN);
        setId(admin, 1L);

        AppUser resident = user("resident", UserRole.RESIDENT);
        setId(resident, 10L);

        MaintenanceRequest request = request(resident);
        LocalDateTime createdAt = LocalDateTime.of(2026, 3, 14, 13, 0);

        when(userRepo.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(requestRepo.findById(99L)).thenReturn(Optional.of(request));
        when(commentRepo.save(any(RequestComment.class))).thenAnswer(inv -> {
            RequestComment saved = inv.getArgument(0);
            saved.setCreatedAt(createdAt);
            return saved;
        });

        RequestCommentDto result = service.addAdminComment(99L, "admin", "Engineer booked for tomorrow");

        assertEquals("admin", result.getAuthorUsername());
        assertEquals("Engineer booked for tomorrow", result.getText());
        assertEquals(createdAt, result.getCreatedAt());

        verify(commentRepo).save(any(RequestComment.class));
    }

    @Test
    void addAdminComment_throwsForbidden_whenUserIsNotAdmin() {
        AppUser resident = user("resident", UserRole.RESIDENT);
        setId(resident, 10L);

        when(userRepo.findByUsername("resident")).thenReturn(Optional.of(resident));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> service.addAdminComment(1L, "resident", "Trying admin comment")
        );

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        assertEquals("Only administrators can comment here", ex.getReason());

        verify(requestRepo, never()).findById(any());
        verify(commentRepo, never()).save(any());
    }

    @Test
    void getCommentsForResident_returnsDtosInRepositoryOrder_whenResidentOwnsRequest() {
        AppUser resident = user("resident", UserRole.RESIDENT);
        setId(resident, 10L);

        MaintenanceRequest request = request(resident);

        RequestComment older = comment(request, resident, "Older comment",
                LocalDateTime.of(2026, 3, 14, 9, 0));
        RequestComment newer = comment(request, resident, "Newer comment",
                LocalDateTime.of(2026, 3, 14, 10, 0));

        when(userRepo.findByUsername("resident")).thenReturn(Optional.of(resident));
        when(requestRepo.findById(1L)).thenReturn(Optional.of(request));
        when(commentRepo.findByRequestIdOrderByCreatedAtAsc(1L)).thenReturn(List.of(older, newer));

        List<RequestCommentDto> result = service.getCommentsForResident(1L, "resident");

        assertEquals(2, result.size());
        assertEquals("Older comment", result.get(0).getText());
        assertEquals("Newer comment", result.get(1).getText());
        assertEquals("resident", result.get(0).getAuthorUsername());
    }

    @Test
    void getCommentsForResident_throwsForbidden_whenResidentDoesNotOwnRequest() {
        AppUser resident = user("resident", UserRole.RESIDENT);
        setId(resident, 10L);

        AppUser otherResident = user("resident2", UserRole.RESIDENT);
        setId(otherResident, 20L);

        MaintenanceRequest request = request(otherResident);

        when(userRepo.findByUsername("resident")).thenReturn(Optional.of(resident));
        when(requestRepo.findById(1L)).thenReturn(Optional.of(request));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> service.getCommentsForResident(1L, "resident")
        );

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        assertEquals("Not allowed to comment on this request", ex.getReason());

        verify(commentRepo, never()).findByRequestIdOrderByCreatedAtAsc(any());
    }

    @Test
    void getCommentsForAdmin_returnsDtos_whenRequestExists() {
        AppUser admin = user("admin", UserRole.ADMIN);
        setId(admin, 1L);

        AppUser resident = user("resident", UserRole.RESIDENT);
        setId(resident, 10L);

        MaintenanceRequest request = request(resident);
        setId(request, 5L);

        RequestComment comment = comment(request, admin, "Admin note",
                LocalDateTime.of(2026, 3, 14, 15, 30));

        when(requestRepo.findById(5L)).thenReturn(Optional.of(request));
        when(commentRepo.findByRequestIdOrderByCreatedAtAsc(5L)).thenReturn(List.of(comment));

        List<RequestCommentDto> result = service.getCommentsForAdmin(5L);

        assertEquals(1, result.size());
        assertEquals("admin", result.get(0).getAuthorUsername());
        assertEquals("Admin note", result.get(0).getText());
        assertEquals(LocalDateTime.of(2026, 3, 14, 15, 30), result.get(0).getCreatedAt());
    }

    private AppUser user(String username, UserRole role) {
        AppUser user = new AppUser();
        user.setUsername(username);
        user.setPassword("x");
        user.setRole(role);
        return user;
    }

    private MaintenanceRequest request(AppUser createdBy) {
        MaintenanceRequest request = new MaintenanceRequest();
        request.setCreatedBy(createdBy);
        return request;
    }

    private RequestComment comment(MaintenanceRequest request, AppUser author, String text, LocalDateTime createdAt) {
        RequestComment comment = new RequestComment();
        comment.setRequest(request);
        comment.setAuthor(author);
        comment.setText(text);
        comment.setCreatedAt(createdAt);
        return comment;
    }

    private void setId(Object target, Long id) {
        try {
            Field field = target.getClass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(target, id);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}