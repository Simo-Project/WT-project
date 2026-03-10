package com.tus.controllers;

import com.tus.db.models.AppUser;
import com.tus.db.models.MaintenanceRequest;
import com.tus.db.repos.AppUserRepository;
import com.tus.db.repos.MaintenanceRequestRepository;
import com.tus.dtos.CreateMaintenanceRequestDto;
import com.tus.dtos.MaintenanceRequestDetailsDto;
import com.tus.dtos.MaintenanceRequestSummaryDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/requests")
public class ResidentMaintenanceRequestController {

    private final MaintenanceRequestRepository requests;
    private final AppUserRepository users;

    public ResidentMaintenanceRequestController(MaintenanceRequestRepository requests,
                                                AppUserRepository users) {
        this.requests = requests;
        this.users = users;
    }

    @PostMapping
    public MaintenanceRequestSummaryDto create(@Valid @RequestBody CreateMaintenanceRequestDto dto,
                                               Principal principal) {

        AppUser user = users.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String unit = user.getUnit();

        MaintenanceRequest mr = new MaintenanceRequest();
        mr.setTask(dto.getTitle());
        mr.setCategory(dto.getCategory());
        mr.setDescription(dto.getDescription());
        mr.setUnit(unit);

        MaintenanceRequest saved = requests.save(mr);

        return new MaintenanceRequestSummaryDto(
                saved.getId(),
                saved.getCreatedOn(),
                saved.getTask(),
                saved.getStatus(),
                saved.getPriority(),
                saved.getUnit(),
                saved.getAssignedTo() != null ? saved.getAssignedTo().getUsername() : null
        );
    }

    @GetMapping("/my")
    public List<MaintenanceRequestSummaryDto> myRequests(Principal principal) {
        AppUser user = users.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return requests.findByUnit(user.getUnit()).stream()
                .map(r -> new MaintenanceRequestSummaryDto(
                        r.getId(), r.getCreatedOn(), r.getTask(), r.getStatus(), r.getPriority(), r.getUnit(),  r.getAssignedTo() != null ? r.getAssignedTo().getUsername() : null
                ))
                .toList();
    }

    @GetMapping("/{id}")
    public MaintenanceRequestDetailsDto getOne(@PathVariable Long id, Principal principal) {

        AppUser user = users.findByUsername(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        MaintenanceRequest r = requests.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Request not found"));

        if (user.getUnit() == null || !user.getUnit().equals(r.getUnit())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not authorised to view this request");
        }

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