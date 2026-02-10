package com.tus.services;

import com.tus.db.models.MaintenanceRequest;
import com.tus.db.models.Priority;
import com.tus.db.models.RequestStatus;
import com.tus.db.repos.MaintenanceRequestRepository;
import com.tus.dtos.MaintenanceRequestSummaryDto;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MaintenanceRequestService {

    private final MaintenanceRequestRepository repo;

    public MaintenanceRequestService(MaintenanceRequestRepository repo) {
        this.repo = repo;
    }

    public List<MaintenanceRequestSummaryDto> list(Optional<RequestStatus> status, Optional<Priority> priority) {
        List<MaintenanceRequest> results;

        if (status.isPresent() && priority.isPresent()) {
            results = repo.findByStatusAndPriority(status.get(), priority.get());
        } else if (status.isPresent()) {
            results = repo.findByStatus(status.get());
        } else if (priority.isPresent()) {
            results = repo.findByPriority(priority.get());
        } else {
            results = repo.findAll();
        }

        return results.stream()
                .map(r -> new MaintenanceRequestSummaryDto(
                        r.getId(), r.getStatus(), r.getPriority(), r.getUnit(), r.getCreatedAt()
                ))
                .toList();
    }
}

