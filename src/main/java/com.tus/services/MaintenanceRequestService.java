package com.tus.services;

import com.tus.db.models.*;
import com.tus.db.repos.AppUserRepository;
import com.tus.db.repos.MaintenanceRequestRepository;
import com.tus.dtos.MaintenanceRequestSummaryDto;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class MaintenanceRequestService {

    private final MaintenanceRequestRepository repo;
    private final AppUserRepository userRepo;

    public MaintenanceRequestService(MaintenanceRequestRepository repo, AppUserRepository userRepo ) {
        this.repo = repo;
        this.userRepo = userRepo;
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
                        r.getId(), r.getCreatedOn(), r.getTask(), r.getStatus(), r.getPriority(), r.getUnit(), r.getAssignedTo() != null ? r.getAssignedTo().getUsername() : null
                ))
                .toList();
    }

    public MaintenanceRequestSummaryDto updateStatus(Long id, RequestStatus newStatus) {
        MaintenanceRequest r = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Request not found"));

        if (r.getStatus() == RequestStatus.CANCELLED && newStatus != RequestStatus.CANCELLED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cancelled requests cannot be updated");
        }

        r.setStatus(newStatus);
        MaintenanceRequest saved = repo.save(r);

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

    public MaintenanceRequestSummaryDto assignRequest(Long requestId, Long staffUserId) {
        MaintenanceRequest r = repo.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Request not found"));

        if (r.getStatus() == RequestStatus.CANCELLED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cannot assign a cancelled request");
        }

        if (staffUserId == null) {
            r.setAssignedTo(null);
        } else {
            AppUser staff = userRepo.findById(staffUserId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Staff user not found"));

            if (staff.getRole() != UserRole.STAFF) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only staff users can be assigned");
            }

            r.setAssignedTo(staff);
        }

        MaintenanceRequest saved = repo.save(r);

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

    public MaintenanceRequestSummaryDto cancelRequestAsResident(Long requestId, String username) {
        AppUser resident = userRepo.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        MaintenanceRequest request = repo.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Request not found"));

        if (request.getCreatedBy() == null || !resident.getId().equals(request.getCreatedBy().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only cancel your own requests");
        }

        if (request.getStatus() != RequestStatus.NEW) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only requests with status NEW can be cancelled");
        }

        request.setStatus(RequestStatus.CANCELLED);
        MaintenanceRequest saved = repo.save(request);

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
}

