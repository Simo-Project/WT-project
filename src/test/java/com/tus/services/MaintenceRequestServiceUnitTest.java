package com.tus.services;

import com.tus.db.models.AppUser;
import com.tus.db.models.MaintenanceRequest;
import com.tus.db.models.Priority;
import com.tus.db.models.RequestStatus;
import com.tus.db.models.UserRole;
import com.tus.db.repos.AppUserRepository;
import com.tus.db.repos.MaintenanceRequestRepository;
import com.tus.dtos.MaintenanceRequestSummaryDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MaintenanceRequestServiceUnitTest {

    @Mock
    private MaintenanceRequestRepository repo;

    @Mock
    private AppUserRepository userRepo;

    private MaintenanceRequestService service;

    @BeforeEach
    void setUp() {
        service = new MaintenanceRequestService(repo, userRepo);
    }

    @Test
    void list_withStatusAndPriority_callsCombinedFilterAndMapsAssignedUser() {
        AppUser staff = user("staff1", UserRole.STAFF);
        setId(staff, 20L);

        MaintenanceRequest request = request("Fix heater", RequestStatus.NEW, Priority.HIGH, "Apt 12");
        setId(request, 1L);
        request.setAssignedTo(staff);

        when(repo.findByStatusAndPriority(RequestStatus.NEW, Priority.HIGH))
                .thenReturn(List.of(request));

        List<MaintenanceRequestSummaryDto> result =
                service.list(Optional.of(RequestStatus.NEW), Optional.of(Priority.HIGH));

        assertEquals(1, result.size());
        assertEquals("Fix heater", result.get(0).getTask());
        assertEquals("staff1", result.get(0).getAssignedToUsername());

        verify(repo).findByStatusAndPriority(RequestStatus.NEW, Priority.HIGH);
        verify(repo, never()).findByStatus(any());
        verify(repo, never()).findByPriority(any());
        verify(repo, never()).findAll();
    }

    @Test
    void list_withPriorityOnly_callsPriorityFilter() {
        MaintenanceRequest request = request("Leak", RequestStatus.IN_PROGRESS, Priority.MEDIUM, "Apt 3");
        setId(request, 2L);

        when(repo.findByPriority(Priority.MEDIUM)).thenReturn(List.of(request));

        List<MaintenanceRequestSummaryDto> result =
                service.list(Optional.empty(), Optional.of(Priority.MEDIUM));

        assertEquals(1, result.size());
        assertEquals("Leak", result.get(0).getTask());
        assertEquals(Priority.MEDIUM, result.get(0).getPriority());

        verify(repo).findByPriority(Priority.MEDIUM);
        verify(repo, never()).findAll();
    }

    @Test
    void list_withoutFilters_callsFindAll() {
        MaintenanceRequest request = request("Paint wall", RequestStatus.CLOSED, Priority.LOW, "Apt 8");
        setId(request, 3L);

        when(repo.findAll()).thenReturn(List.of(request));

        List<MaintenanceRequestSummaryDto> result =
                service.list(Optional.empty(), Optional.empty());

        assertEquals(1, result.size());
        assertEquals("Paint wall", result.get(0).getTask());

        verify(repo).findAll();
        verify(repo, never()).findByStatus(any());
        verify(repo, never()).findByPriority(any());
        verify(repo, never()).findByStatusAndPriority(any(), any());
    }

    @Test
    void updateStatus_updatesRequestAndReturnsDto() {
        MaintenanceRequest request = request("Fix sink", RequestStatus.NEW, Priority.MEDIUM, "Apt 12");
        setId(request, 4L);

        when(repo.findById(4L)).thenReturn(Optional.of(request));
        when(repo.save(any(MaintenanceRequest.class))).thenAnswer(inv -> inv.getArgument(0));

        MaintenanceRequestSummaryDto result = service.updateStatus(4L, RequestStatus.IN_PROGRESS);

        assertEquals(RequestStatus.IN_PROGRESS, result.getStatus());
        assertEquals("Fix sink", result.getTask());

        ArgumentCaptor<MaintenanceRequest> captor = ArgumentCaptor.forClass(MaintenanceRequest.class);
        verify(repo).save(captor.capture());
        assertEquals(RequestStatus.IN_PROGRESS, captor.getValue().getStatus());
    }

    @Test
    void updateStatus_throwsNotFound_whenRequestMissing() {
        when(repo.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> service.updateStatus(99L, RequestStatus.CLOSED)
        );

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        assertEquals("Request not found", ex.getReason());

        verify(repo, never()).save(any());
    }

    @Test
    void updateStatus_throwsConflict_whenCancelledRequestIsChangedToAnotherStatus() {
        MaintenanceRequest request = request("Broken vent", RequestStatus.CANCELLED, Priority.MEDIUM, "Apt 3");
        setId(request, 5L);

        when(repo.findById(5L)).thenReturn(Optional.of(request));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> service.updateStatus(5L, RequestStatus.IN_PROGRESS)
        );

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        assertEquals("Cancelled requests cannot be updated", ex.getReason());

        verify(repo, never()).save(any());
    }

    @Test
    void assignRequest_assignsStaffUserAndReturnsDto() {
        MaintenanceRequest request = request("Light issue", RequestStatus.NEW, Priority.HIGH, "Apt 5");
        setId(request, 6L);

        AppUser staff = user("staff1", UserRole.STAFF);
        setId(staff, 30L);

        when(repo.findById(6L)).thenReturn(Optional.of(request));
        when(userRepo.findById(30L)).thenReturn(Optional.of(staff));
        when(repo.save(any(MaintenanceRequest.class))).thenAnswer(inv -> inv.getArgument(0));

        MaintenanceRequestSummaryDto result = service.assignRequest(6L, 30L);

        assertEquals("staff1", result.getAssignedToUsername());

        ArgumentCaptor<MaintenanceRequest> captor = ArgumentCaptor.forClass(MaintenanceRequest.class);
        verify(repo).save(captor.capture());
        assertSame(staff, captor.getValue().getAssignedTo());
    }

    @Test
    void assignRequest_clearsAssignment_whenStaffUserIdIsNull() {
        AppUser existingStaff = user("staff1", UserRole.STAFF);
        setId(existingStaff, 31L);

        MaintenanceRequest request = request("Door issue", RequestStatus.NEW, Priority.MEDIUM, "Apt 2");
        setId(request, 7L);
        request.setAssignedTo(existingStaff);

        when(repo.findById(7L)).thenReturn(Optional.of(request));
        when(repo.save(any(MaintenanceRequest.class))).thenAnswer(inv -> inv.getArgument(0));

        MaintenanceRequestSummaryDto result = service.assignRequest(7L, null);

        assertNull(result.getAssignedToUsername());

        ArgumentCaptor<MaintenanceRequest> captor = ArgumentCaptor.forClass(MaintenanceRequest.class);
        verify(repo).save(captor.capture());
        assertNull(captor.getValue().getAssignedTo());

        verify(userRepo, never()).findById(any());
    }

    @Test
    void assignRequest_throwsBadRequest_whenSelectedUserIsNotStaff() {
        MaintenanceRequest request = request("Smoke detector", RequestStatus.NEW, Priority.HIGH, "Apt 12");
        setId(request, 8L);

        AppUser resident = user("resident", UserRole.RESIDENT);
        setId(resident, 40L);

        when(repo.findById(8L)).thenReturn(Optional.of(request));
        when(userRepo.findById(40L)).thenReturn(Optional.of(resident));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> service.assignRequest(8L, 40L)
        );

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertEquals("Only staff users can be assigned", ex.getReason());

        verify(repo, never()).save(any());
    }

    @Test
    void cancelRequestAsResident_cancelsOwnNewRequest() {
        AppUser resident = user("resident", UserRole.RESIDENT);
        setId(resident, 50L);

        MaintenanceRequest request = request("Radiator not heating", RequestStatus.NEW, Priority.MEDIUM, "Apt 12");
        setId(request, 9L);
        request.setCreatedBy(resident);

        when(userRepo.findByUsername("resident")).thenReturn(Optional.of(resident));
        when(repo.findById(9L)).thenReturn(Optional.of(request));
        when(repo.save(any(MaintenanceRequest.class))).thenAnswer(inv -> inv.getArgument(0));

        MaintenanceRequestSummaryDto result = service.cancelRequestAsResident(9L, "resident");

        assertEquals(RequestStatus.CANCELLED, result.getStatus());

        ArgumentCaptor<MaintenanceRequest> captor = ArgumentCaptor.forClass(MaintenanceRequest.class);
        verify(repo).save(captor.capture());
        assertEquals(RequestStatus.CANCELLED, captor.getValue().getStatus());
    }

    @Test
    void cancelRequestAsResident_throwsForbidden_whenResidentDoesNotOwnRequest() {
        AppUser resident = user("resident", UserRole.RESIDENT);
        setId(resident, 50L);

        AppUser otherResident = user("resident2", UserRole.RESIDENT);
        setId(otherResident, 60L);

        MaintenanceRequest request = request("Paint hall", RequestStatus.NEW, Priority.LOW, "Apt 3");
        setId(request, 10L);
        request.setCreatedBy(otherResident);

        when(userRepo.findByUsername("resident")).thenReturn(Optional.of(resident));
        when(repo.findById(10L)).thenReturn(Optional.of(request));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> service.cancelRequestAsResident(10L, "resident")
        );

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        assertEquals("You can only cancel your own requests", ex.getReason());

        verify(repo, never()).save(any());
    }

    @Test
    void cancelRequestAsResident_throwsConflict_whenRequestIsNotNew() {
        AppUser resident = user("resident", UserRole.RESIDENT);
        setId(resident, 50L);

        MaintenanceRequest request = request("Bathroom light", RequestStatus.IN_PROGRESS, Priority.MEDIUM, "Apt 12");
        setId(request, 11L);
        request.setCreatedBy(resident);

        when(userRepo.findByUsername("resident")).thenReturn(Optional.of(resident));
        when(repo.findById(11L)).thenReturn(Optional.of(request));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> service.cancelRequestAsResident(11L, "resident")
        );

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        assertEquals("Only requests with status NEW can be cancelled", ex.getReason());

        verify(repo, never()).save(any());
    }

    private AppUser user(String username, UserRole role) {
        AppUser user = new AppUser();
        user.setUsername(username);
        user.setPassword("x");
        user.setRole(role);
        return user;
    }

    private MaintenanceRequest request(String task, RequestStatus status, Priority priority, String unit) {
        MaintenanceRequest request = new MaintenanceRequest();
        request.setTask(task);
        request.setCreatedOn(LocalDate.of(2026, 3, 14));
        request.setStatus(status);
        request.setPriority(priority);
        request.setUnit(unit);
        return request;
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