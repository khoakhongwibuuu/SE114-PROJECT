package com.carenest.backend.features.community.controller;

import com.carenest.backend.core.api.ApiResponse;
import com.carenest.backend.features.community.dto.request.CreateGroupCreationRequest;
import com.carenest.backend.features.community.dto.response.GroupCreationRequestResponse;
import com.carenest.backend.features.community.service.CommunityKnowledgeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class GroupCreationRequestController {

    private final CommunityKnowledgeService communityKnowledgeService;

    @PostMapping("/group-requests")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('DOCTOR')")
    public ApiResponse<GroupCreationRequestResponse> createGroupRequest(
            @Valid @RequestBody CreateGroupCreationRequest request
    ) {
        return ApiResponse.success(
                "Da gui yeu cau tao nhom",
                communityKnowledgeService.createGroupRequest(request)
        );
    }

    @GetMapping("/group-requests/mine")
    @PreAuthorize("hasRole('DOCTOR')")
    public ApiResponse<List<GroupCreationRequestResponse>> getMyGroupRequests() {
        return ApiResponse.success(communityKnowledgeService.getMyGroupRequests());
    }

    @GetMapping("/admin/group-requests")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<GroupCreationRequestResponse>> getAdminGroupRequests() {
        return ApiResponse.success(communityKnowledgeService.getAdminGroupRequests());
    }

    @PostMapping("/admin/group-requests/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> approveGroupRequest(@PathVariable("id") Long id) {
        communityKnowledgeService.approveGroupRequest(id);
        return ApiResponse.success("Da duyet yeu cau tao nhom", null);
    }

    @PostMapping("/admin/group-requests/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> rejectGroupRequest(
            @PathVariable("id") Long id,
            @RequestParam("reason") String reason
    ) {
        communityKnowledgeService.rejectGroupRequest(id, reason);
        return ApiResponse.success("Da tu choi yeu cau tao nhom", null);
    }
}
