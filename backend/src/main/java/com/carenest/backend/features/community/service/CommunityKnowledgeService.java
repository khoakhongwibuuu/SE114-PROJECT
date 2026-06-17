package com.carenest.backend.features.community.service;

import com.carenest.backend.core.api.PageResponse;
import com.carenest.backend.features.community.dto.request.CreateArticleCommentRequest;
import com.carenest.backend.features.community.dto.request.CreateArticleRequest;
import com.carenest.backend.features.community.dto.request.CreateGroupCreationRequest;
import com.carenest.backend.features.community.dto.request.CreateGroupPostCommentRequest;
import com.carenest.backend.features.community.dto.request.CreateGroupPostRequest;
import com.carenest.backend.features.community.dto.request.ReportPostRequest;
import com.carenest.backend.features.community.dto.request.UpdateGroupMemberRoleRequest;
import com.carenest.backend.features.community.dto.response.ArticleCommentResponse;
import com.carenest.backend.features.community.dto.response.ArticleLikeResponse;
import com.carenest.backend.features.community.dto.response.ArticleResponse;
import com.carenest.backend.features.community.dto.response.ChatGroupPreviewResponse;
import com.carenest.backend.features.community.dto.response.ChatGroupResponse;
import com.carenest.backend.features.community.dto.response.GroupCreationRequestResponse;
import com.carenest.backend.features.community.dto.response.GroupMemberResponse;
import com.carenest.backend.features.community.dto.response.GroupPostCommentResponse;
import com.carenest.backend.features.community.dto.response.GroupPostInteractionResponse;
import com.carenest.backend.features.community.dto.response.GroupPostResponse;

import java.util.List;

public interface CommunityKnowledgeService {
    List<ArticleResponse> getArticles();

    ArticleResponse createArticle(CreateArticleRequest request);

    ArticleLikeResponse toggleArticleLike(Long articleId);

    List<ArticleCommentResponse> getArticleComments(Long articleId);

    ArticleCommentResponse createArticleComment(Long articleId, CreateArticleCommentRequest request);

    List<ChatGroupResponse> getChatGroups(String search, String category);

    List<ChatGroupResponse> getMyChatGroups(String search);

    List<ChatGroupResponse> getDiscoverChatGroups(String search);

    GroupCreationRequestResponse createGroupRequest(CreateGroupCreationRequest request);

    List<GroupCreationRequestResponse> getMyGroupRequests();

    List<GroupCreationRequestResponse> getAdminGroupRequests();

    void approveGroupRequest(Long requestId);

    void rejectGroupRequest(Long requestId, String reason);

    ChatGroupPreviewResponse getChatGroupPreview(Long groupId);

    ChatGroupPreviewResponse joinChatGroup(Long groupId);

    void leaveChatGroup(Long groupId);

    PageResponse<GroupPostResponse> getGroupPosts(Long groupId, org.springframework.data.domain.Pageable pageable);

    PageResponse<GroupPostResponse> getMyGroupPosts(Long groupId, org.springframework.data.domain.Pageable pageable);

    PageResponse<GroupPostResponse> getPendingGroupPosts(Long groupId, org.springframework.data.domain.Pageable pageable);

    GroupPostResponse createGroupPost(Long groupId, CreateGroupPostRequest request);

    GroupPostResponse updateGroupPost(Long postId, CreateGroupPostRequest request);

    void deleteGroupPost(Long postId);

    void approveGroupPost(Long postId);

    void rejectGroupPost(Long postId, String reason);

    void reportPost(Long postId, ReportPostRequest request);

    GroupPostInteractionResponse toggleGroupPostLike(Long postId);

    PageResponse<GroupPostCommentResponse> getGroupPostComments(Long postId, org.springframework.data.domain.Pageable pageable);

    GroupPostCommentResponse createGroupPostComment(Long postId, CreateGroupPostCommentRequest request);

    List<GroupMemberResponse> getGroupMembers(Long groupId);

    GroupMemberResponse updateGroupMemberRole(Long groupId, Long targetUserId, UpdateGroupMemberRoleRequest request);

    void kickMember(Long groupId, Long targetUserId);

    void freezeGroup(Long groupId);

    void unfreezeGroup(Long groupId);
}
