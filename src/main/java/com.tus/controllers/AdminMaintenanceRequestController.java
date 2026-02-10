package com.tus.controllers;

import com.tus.db.models.Priority;
import com.tus.db.models.RequestStatus;
import com.tus.dtos.MaintenanceRequestSummaryDto;
import com.tus.services.MaintenanceRequestService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin/requests")
public class AdminMaintenanceRequestController {

    private final MaintenanceRequestService service;

    public AdminMaintenanceRequestController(MaintenanceRequestService service) {
        this.service = service;
    }

    @GetMapping
    public List<MaintenanceRequestSummaryDto> list(
            @RequestParam Optional<RequestStatus> status,
            @RequestParam Optional<Priority> priority
    ) {
        return service.list(status, priority);
    }
}
