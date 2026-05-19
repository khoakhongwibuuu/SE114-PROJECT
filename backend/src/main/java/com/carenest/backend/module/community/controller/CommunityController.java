package com.carenest.backend.module.community.controller;

import com.carenest.backend.common.dto.ApiResponse;
import com.carenest.backend.module.community.dto.request.CreateGroupPostRequest;
import com.carenest.backend.module.community.dto.response.CommunityGroupResponse;
import com.carenest.backend.module.community.dto.response.GroupPostResponse;
import com.carenest.backend.module.community.service.CommunityKnowledgeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/communities")
@RequiredArgsConstructor
public class CommunityController {

    private final CommunityKnowledgeService communityKnowledgeService;

    @GetMapping
    public ApiResponse<List<CommunityGroupResponse>> getCommunityGroups() {
        return ApiResponse.success(communityKnowledgeService.getCommunityGroups());
    }

    @GetMapping("/{id}/posts")
    public ApiResponse<List<GroupPostResponse>> getGroupPosts(@PathVariable("id") Long id) {
        return ApiResponse.success(communityKnowledgeService.getGroupPosts(id));
    }

    @PostMapping("/{id}/posts")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<GroupPostResponse> createGroupPost(
            @PathVariable("id") Long id,
            @Valid @RequestBody CreateGroupPostRequest request) {
        return ApiResponse.success("Post created successfully", communityKnowledgeService.createGroupPost(id, request));
    }
}
