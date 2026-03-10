package com.tus.controllers;

import com.tus.db.models.*;
import com.tus.db.repos.AppUserRepository;
import com.tus.db.repos.MaintenanceRequestRepository;
import com.tus.dtos.AssignRequestDto;
import com.tus.dtos.MaintenanceRequestSummaryDto;
import com.tus.dtos.StaffOptionDto;
import com.tus.services.MaintenanceRequestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminMaintenanceRequestControllerUnitTest {

    @Mock
    private AppUserRepository users;

    @Mock
    private MaintenanceRequestRepository requests;

    private MaintenanceRequestService service;
    private AdminMaintenanceRequestController controller;

    @BeforeEach
    void setUp() {
        service = new MaintenanceRequestService(requests, users);
        controller = new AdminMaintenanceRequestController(service, users, requests);
    }

    @Test
    void assignRequest_returnsAssignedStaffUsername() {
        MaintenanceRequest request = new MaintenanceRequest();
        request.setTask("Fix leaking tap");
        request.setStatus(RequestStatus.NEW);
        request.setPriority(Priority.MEDIUM);
        request.setUnit("Apt 12");
        request.setCreatedOn(LocalDate.of(2026, 3, 10));

        AppUser staff = new AppUser();
        staff.setUsername("staff1");
        staff.setRole(UserRole.STAFF);

        AssignRequestDto dto = new AssignRequestDto();
        dto.setStaffUserId(2L);

        when(requests.findById(1L)).thenReturn(Optional.of(request));
        when(users.findById(2L)).thenReturn(Optional.of(staff));
        when(requests.save(request)).thenReturn(request);

        MaintenanceRequestSummaryDto result = controller.assignRequest(1L, dto);

        assertEquals("staff1", result.getAssignedToUsername());
        assertEquals(staff, request.getAssignedTo());

        verify(requests).findById(1L);
        verify(users).findById(2L);
        verify(requests).save(request);
    }

    @Test
    void staffUsers_returnsOnlyStaffUsers() {
        AppUser admin = new AppUser("admin", "x", UserRole.ADMIN, null);
        AppUser staff1 = new AppUser("staff1", "x", UserRole.STAFF, null);
        AppUser resident = new AppUser("resident1", "x", UserRole.RESIDENT, "Apt 12");
        AppUser staff2 = new AppUser("staff2", "x", UserRole.STAFF, null);

        when(users.findAll()).thenReturn(List.of(admin, staff1, resident, staff2));

        List<StaffOptionDto> result = controller.staffUsers();

        assertEquals(2, result.size());
        assertEquals("staff1", result.get(0).getUsername());
        assertEquals("staff2", result.get(1).getUsername());
    }
}