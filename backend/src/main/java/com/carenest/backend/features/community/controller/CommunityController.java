package com.carenest.backend.features.community.controller;

import com.carenest.backend.core.api.ApiResponse;
import com.carenest.backend.core.api.PageResponse;
import com.carenest.backend.features.community.dto.request.CreateGroupPostRequest;
import com.carenest.backend.features.community.dto.response.ChatGroupPreviewResponse;
import com.carenest.backend.features.community.dto.response.ChatGroupResponse;
import com.carenest.backend.features.community.dto.response.GroupPostResponse;
import com.carenest.backend.features.community.service.CommunityKnowledgeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/communities")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('USER', 'DOCTOR', 'ADMIN')")
public class CommunityController {

    private final CommunityKnowledgeService communityKnowledgeService;

    @GetMapping
    public ApiResponse<List<ChatGroupResponse>> getChatGroups(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "category", required = false) String category) {
        return ApiResponse.success(communityKnowledgeService.getChatGroups(search, category));
    }

    @GetMapping("/my")
    public ApiResponse<List<ChatGroupResponse>> getMyChatGroups(
            @RequestParam(value = "search", required = false) String search) {
        return ApiResponse.success(communityKnowledgeService.getMyChatGroups(search));
    }

    @GetMapping("/discover")
    public ApiResponse<List<ChatGroupResponse>> getDiscoverChatGroups(
            @RequestParam(value = "search", required = false) String search) {
        return ApiResponse.success(communityKnowledgeService.getDiscoverChatGroups(search));
    }

    @GetMapping("/{id}/preview")
    public ApiResponse<ChatGroupPreviewResponse> getChatGroupPreview(@PathVariable("id") Long id) {
        return ApiResponse.success(communityKnowledgeService.getChatGroupPreview(id));
    }

    @PostMapping("/{id}/join")
    public ApiResponse<ChatGroupPreviewResponse> joinChatGroup(@PathVariable("id") Long id) {
        return ApiResponse.success("Đã tham gia nhóm", communityKnowledgeService.joinChatGroup(id));
    }

    @PostMapping("/{id}/leave")
    public ApiResponse<Void> leaveChatGroup(@PathVariable("id") Long id) {
        communityKnowledgeService.leaveChatGroup(id);
        return ApiResponse.success("Đã rời nhóm", null);
    }

    @GetMapping("/{id}/posts")
    public ApiResponse<PageResponse<GroupPostResponse>> getGroupPosts(
            @PathVariable("id") Long id,
            @PageableDefault(size = 30, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ApiResponse.success(communityKnowledgeService.getGroupPosts(id, pageable));
    }

    @PostMapping("/{id}/posts")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<GroupPostResponse> createGroupPost(
            @PathVariable("id") Long id,
            @Valid @RequestBody CreateGroupPostRequest request) {
        return ApiResponse.success("Đã gửi tin nhắn vào nhóm", communityKnowledgeService.createGroupPost(id, request));
    }

    @DeleteMapping("/{id}/members/{targetUserId}")
    public ApiResponse<Void> kickMember(
            @PathVariable("id") Long id,
            @PathVariable("targetUserId") Long targetUserId) {
        communityKnowledgeService.kickMember(id, targetUserId);
        return ApiResponse.success("Đã mời thành viên ra khỏi nhóm", null);
    }
}
