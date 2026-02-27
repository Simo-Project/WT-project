package com.tus.controllers;

import com.tus.db.models.MaintenanceRequest;
import com.tus.db.repos.MaintenanceRequestRepository;
import com.tus.dtos.CreateMaintenanceRequestDto;
import com.tus.dtos.MaintenanceRequestSummaryDto;
import com.tus.services.ResidentUnitService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/requests")
public class ResidentMaintenanceRequestController {

    private final MaintenanceRequestRepository requests;
    private final ResidentUnitService residentUnitService;

    public ResidentMaintenanceRequestController(MaintenanceRequestRepository requests,
                                               ResidentUnitService residentUnitService) {
        this.requests = requests;
        this.residentUnitService = residentUnitService;
    }

    /**
     * MR-2: Resident creates a new request.
     * Unit is derived from the logged-in resident (cannot be overridden by request body).
     */
    @PostMapping
    public MaintenanceRequestSummaryDto create(@Valid @RequestBody CreateMaintenanceRequestDto dto,
                                              Principal principal) {
        String unit = residentUnitService.getUnitForUsername(principal.getName());

        MaintenanceRequest mr = new MaintenanceRequest();
        mr.setTask(dto.getTitle());
        mr.setCategory(dto.getCategory());
        mr.setDescription(dto.getDescription());
        mr.setUnit(unit);
        // status/priority/createdOn defaults set in @PrePersist

        MaintenanceRequest saved = requests.save(mr);

        return new MaintenanceRequestSummaryDto(
                saved.getId(),
                saved.getCreatedOn(),
                saved.getTask(),
                saved.getStatus(),
                saved.getPriority(),
                saved.getUnit()
        );
    }

    /**
     * MR-2: Resident can view requests for their own unit ("My Requests").
     */
    @GetMapping("/my")
    public List<MaintenanceRequestSummaryDto> myRequests(Principal principal) {
        String unit = residentUnitService.getUnitForUsername(principal.getName());

        return requests.findByUnit(unit).stream()
                .map(r -> new MaintenanceRequestSummaryDto(
                        r.getId(),
                        r.getCreatedOn(),
                        r.getTask(),
                        r.getStatus(),
                        r.getPriority(),
                        r.getUnit()
                ))
                .toList();
    }
}
