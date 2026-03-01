package com.tus.db.repos;

import com.tus.db.models.MaintenanceRequest;
import com.tus.db.models.Priority;
import com.tus.db.models.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MaintenanceRequestRepository extends JpaRepository<MaintenanceRequest, Long> {
    List<MaintenanceRequest> findByStatus(RequestStatus status);
    List<MaintenanceRequest> findByPriority(Priority priority);
    List<MaintenanceRequest> findByStatusAndPriority(RequestStatus status, Priority priority);
    List<MaintenanceRequest> findByUnit(String unit);
}
