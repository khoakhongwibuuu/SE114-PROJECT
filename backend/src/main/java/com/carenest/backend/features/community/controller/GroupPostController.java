package com.carenest.backend.features.community.controller;

import com.carenest.backend.core.api.ApiResponse;
import com.carenest.backend.features.community.dto.request.ReportPostRequest;
import com.carenest.backend.features.community.service.CommunityKnowledgeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('USER', 'DOCTOR', 'ADMIN')")
public class GroupPostController {

    private final CommunityKnowledgeService communityKnowledgeService;

    @PostMapping("/{postId}/report")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Void> reportPost(
            @PathVariable("postId") Long postId,
            @Valid @RequestBody ReportPostRequest request) {
        communityKnowledgeService.reportPost(postId, request);
        return ApiResponse.success("Đã gửi báo cáo vi phạm", null);
    }
}
