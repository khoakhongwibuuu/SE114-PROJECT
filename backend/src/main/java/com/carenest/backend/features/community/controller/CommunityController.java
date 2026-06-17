package com.carenest.backend.features.community.controller;

import com.carenest.backend.core.api.ApiResponse;
import com.carenest.backend.core.api.PageResponse;
import com.carenest.backend.features.community.dto.request.CreateGroupPostCommentRequest;
import com.carenest.backend.features.community.dto.request.CreateGroupPostRequest;
import com.carenest.backend.features.community.dto.request.UpdateGroupMemberRoleRequest;
import com.carenest.backend.features.community.dto.response.ChatGroupPreviewResponse;
import com.carenest.backend.features.community.dto.response.ChatGroupResponse;
import com.carenest.backend.features.community.dto.response.GroupGovernanceAuditLogResponse;
import com.carenest.backend.features.community.dto.response.GroupMemberResponse;
import com.carenest.backend.features.community.dto.response.GroupPostCommentResponse;
import com.carenest.backend.features.community.dto.response.GroupPostInteractionResponse;
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
import org.springframework.web.bind.annotation.PatchMapping;
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

    @GetMapping("/{id}/members")
    public ApiResponse<List<GroupMemberResponse>> getGroupMembers(@PathVariable("id") Long id) {
        return ApiResponse.success(communityKnowledgeService.getGroupMembers(id));
    }

    @GetMapping("/{id}/governance-audit-logs")
    public ApiResponse<List<GroupGovernanceAuditLogResponse>> getGroupGovernanceAuditLogs(@PathVariable("id") Long id) {
        return ApiResponse.success(communityKnowledgeService.getGroupGovernanceAuditLogs(id));
    }

    @PatchMapping("/{id}/members/{targetUserId}/role")
    public ApiResponse<GroupMemberResponse> updateGroupMemberRole(
            @PathVariable("id") Long id,
            @PathVariable("targetUserId") Long targetUserId,
            @Valid @RequestBody UpdateGroupMemberRoleRequest request) {
        return ApiResponse.success("Đã cập nhật quyền thành viên", communityKnowledgeService.updateGroupMemberRole(id, targetUserId, request));
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
        return ApiResponse.success("Đã gửi bài viết chờ duyệt", communityKnowledgeService.createGroupPost(id, request));
    }

    @GetMapping("/{id}/posts/my")
    public ApiResponse<PageResponse<GroupPostResponse>> getMyGroupPosts(
            @PathVariable("id") Long id,
            @PageableDefault(size = 30, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ApiResponse.success(communityKnowledgeService.getMyGroupPosts(id, pageable));
    }

    @GetMapping("/{id}/posts/pending")
    public ApiResponse<PageResponse<GroupPostResponse>> getPendingGroupPosts(
            @PathVariable("id") Long id,
            @PageableDefault(size = 30, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ApiResponse.success(communityKnowledgeService.getPendingGroupPosts(id, pageable));
    }

    @PostMapping("/posts/{postId}/approve")
    public ApiResponse<Void> approveGroupPost(@PathVariable("postId") Long postId) {
        communityKnowledgeService.approveGroupPost(postId);
        return ApiResponse.success("Đã duyệt bài viết", null);
    }

    @PatchMapping("/posts/{postId}")
    public ApiResponse<GroupPostResponse> updateGroupPost(
            @PathVariable("postId") Long postId,
            @Valid @RequestBody CreateGroupPostRequest request) {
        return ApiResponse.success("Đã cập nhật bài viết và gửi lại chờ duyệt", communityKnowledgeService.updateGroupPost(postId, request));
    }

    @DeleteMapping("/posts/{postId}")
    public ApiResponse<Void> deleteGroupPost(@PathVariable("postId") Long postId) {
        communityKnowledgeService.deleteGroupPost(postId);
        return ApiResponse.success("Đã xóa bài viết", null);
    }

    @PostMapping("/posts/{postId}/reject")
    public ApiResponse<Void> rejectGroupPost(
            @PathVariable("postId") Long postId,
            @RequestParam("reason") String reason) {
        communityKnowledgeService.rejectGroupPost(postId, reason);
        return ApiResponse.success("Đã từ chối bài viết", null);
    }

    @PostMapping("/posts/{postId}/like")
    public ApiResponse<GroupPostInteractionResponse> toggleGroupPostLike(@PathVariable("postId") Long postId) {
        return ApiResponse.success(communityKnowledgeService.toggleGroupPostLike(postId));
    }

    @GetMapping("/posts/{postId}/comments")
    public ApiResponse<PageResponse<GroupPostCommentResponse>> getGroupPostComments(
            @PathVariable("postId") Long postId,
            @PageableDefault(size = 30, sort = "createdAt", direction = Sort.Direction.ASC) Pageable pageable) {
        return ApiResponse.success(communityKnowledgeService.getGroupPostComments(postId, pageable));
    }

    @PostMapping("/posts/{postId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<GroupPostCommentResponse> createGroupPostComment(
            @PathVariable("postId") Long postId,
            @Valid @RequestBody CreateGroupPostCommentRequest request) {
        return ApiResponse.success("Đã gửi bình luận", communityKnowledgeService.createGroupPostComment(postId, request));
    }

    @DeleteMapping("/{id}/members/{targetUserId}")
    public ApiResponse<Void> kickMember(
            @PathVariable("id") Long id,
            @PathVariable("targetUserId") Long targetUserId,
            @RequestParam("reason") String reason) {
        communityKnowledgeService.kickMember(id, targetUserId, reason);
        return ApiResponse.success("Đã mời thành viên ra khỏi nhóm", null);
    }
}
