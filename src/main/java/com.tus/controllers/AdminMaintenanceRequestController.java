package com.tus.controllers;

import com.tus.db.models.MaintenanceRequest;
import com.tus.db.models.Priority;
import com.tus.db.models.RequestStatus;
import com.tus.db.models.UserRole;
import com.tus.db.repos.AppUserRepository;
import com.tus.db.repos.MaintenanceRequestRepository;
import com.tus.dtos.*;
import com.tus.services.MaintenanceRequestService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin/requests")
public class AdminMaintenanceRequestController {

    private final MaintenanceRequestService service;
    private final AppUserRepository users;
    private final MaintenanceRequestRepository requests;

    public AdminMaintenanceRequestController(MaintenanceRequestService service,  AppUserRepository users, MaintenanceRequestRepository requests) {
        this.service = service;
        this.users = users;
        this.requests = requests;
    }

    @GetMapping
    public List<MaintenanceRequestSummaryDto> list(
            @RequestParam Optional<RequestStatus> status,
            @RequestParam Optional<Priority> priority
    ) {
        return service.list(status, priority);
    }

    @PatchMapping("/{id}/status")
    public MaintenanceRequestSummaryDto updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRequestStatusDto dto
    ) {
        return service.updateStatus(id, dto.getStatus());
    }

    @PatchMapping("/{id}/assign")
    public MaintenanceRequestSummaryDto assignRequest(
            @PathVariable Long id,
            @RequestBody AssignRequestDto dto
    ) {
        return service.assignRequest(id, dto.getStaffUserId());
    }

    @GetMapping("/staff")
    public List<StaffOptionDto> staffUsers() {
        return users.findAll().stream()
                .filter(u -> u.getRole() == UserRole.STAFF)
                .map(u -> new StaffOptionDto(u.getId(), u.getUsername()))
                .toList();
    }

    @GetMapping("/{id}")
    public MaintenanceRequestDetailsDto getOne(@PathVariable Long id) {
        MaintenanceRequest r = requests.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Request not found"));

        return new MaintenanceRequestDetailsDto(
                r.getId(),
                r.getCreatedOn(),
                r.getTask(),
                r.getCategory(),
                r.getDescription(),
                r.getStatus(),
                r.getPriority(),
                r.getUnit(),
                r.getAssignedTo() != null ? r.getAssignedTo().getUsername() : null
        );
    }
}
