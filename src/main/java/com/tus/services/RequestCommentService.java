package com.tus.services;

import com.tus.db.models.AppUser;
import com.tus.db.models.MaintenanceRequest;
import com.tus.db.models.RequestComment;
import com.tus.db.models.UserRole;
import com.tus.db.repos.AppUserRepository;
import com.tus.db.repos.MaintenanceRequestRepository;
import com.tus.db.repos.RequestCommentRepository;
import com.tus.dtos.RequestCommentDto;
import com.tus.utils.RequestCommentMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class RequestCommentService {

    private final RequestCommentRepository commentRepo;
    private final MaintenanceRequestRepository requestRepo;
    private final AppUserRepository userRepo;
    private static final String USER_NOT_FOUND = "User not found";
    private static final String REQUEST_NOT_FOUND = "Request not found";

    public RequestCommentService(RequestCommentRepository commentRepo,
                                 MaintenanceRequestRepository requestRepo,
                                 AppUserRepository userRepo) {
        this.commentRepo = commentRepo;
        this.requestRepo = requestRepo;
        this.userRepo = userRepo;
    }

    public RequestCommentDto addResidentComment(Long requestId, String username, String text) {
        AppUser user = userRepo.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, USER_NOT_FOUND));

        MaintenanceRequest request = requestRepo.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, REQUEST_NOT_FOUND));

        if (request.getCreatedBy() == null || !request.getCreatedBy().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed to comment on this request");
        }

        RequestComment comment = new RequestComment();
        comment.setRequest(request);
        comment.setAuthor(user);
        comment.setText(text);

        return RequestCommentMapper.toDto(commentRepo.save(comment));
    }

    public RequestCommentDto addAdminComment(Long requestId, String username, String text) {
        AppUser user = userRepo.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, USER_NOT_FOUND));

        if (user.getRole() != UserRole.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only administrators can comment here");
        }

        MaintenanceRequest request = requestRepo.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, REQUEST_NOT_FOUND));

        RequestComment comment = new RequestComment();
        comment.setRequest(request);
        comment.setAuthor(user);
        comment.setText(text);

        return RequestCommentMapper.toDto(commentRepo.save(comment));
    }

    public List<RequestCommentDto> getCommentsForResident(Long requestId, String username) {
        AppUser user = userRepo.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, USER_NOT_FOUND));

        MaintenanceRequest request = requestRepo.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, REQUEST_NOT_FOUND));

        if (request.getCreatedBy() == null || !request.getCreatedBy().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed to comment on this request");
        }

        return commentRepo.findByRequestIdOrderByCreatedAtAsc(requestId).stream()
                .map(RequestCommentMapper::toDto)
                .toList();
    }

    public List<RequestCommentDto> getCommentsForAdmin(Long requestId) {
        MaintenanceRequest request = requestRepo.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, REQUEST_NOT_FOUND));

        return commentRepo.findByRequestIdOrderByCreatedAtAsc(request.getId()).stream()
                .map(RequestCommentMapper::toDto)
                .toList();
    }
}