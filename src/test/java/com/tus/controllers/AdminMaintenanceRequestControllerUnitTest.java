package com.tus.controllers;

import com.tus.db.models.AppUser;
import com.tus.db.models.UserRole;
import com.tus.db.repos.AppUserRepository;
import com.tus.db.repos.MaintenanceRequestRepository;
import com.tus.dtos.AssignRequestDto;
import com.tus.dtos.MaintenanceRequestSummaryDto;
import com.tus.dtos.StaffOptionDto;
import com.tus.services.MaintenanceRequestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminMaintenanceRequestControllerUnitTest {

    @Mock
    private MaintenanceRequestService service;

    @Mock
    private AppUserRepository users;

    @Mock
    private MaintenanceRequestRepository requests;

    @InjectMocks
    private AdminMaintenanceRequestController controller;

    private MaintenanceRequestSummaryDto summaryDto;

    @BeforeEach
    void setUp() {
        summaryDto = new MaintenanceRequestSummaryDto(
                1L,
                LocalDate.of(2026, 3, 10),
                "Fix leaking tap",
                com.tus.db.models.RequestStatus.NEW,
                com.tus.db.models.Priority.MEDIUM,
                "Apt 12",
                "staff1"
        );
    }

    @Test
    void assignRequest_delegatesToService() {
        AssignRequestDto dto = new AssignRequestDto();
        dto.setStaffUserId(2L);

        when(service.assignRequest(1L, 2L)).thenReturn(summaryDto);

        MaintenanceRequestSummaryDto result = controller.assignRequest(1L, dto);

        assertEquals("staff1", result.getAssignedToUsername());

        verify(service).assignRequest(1L, 2L);
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