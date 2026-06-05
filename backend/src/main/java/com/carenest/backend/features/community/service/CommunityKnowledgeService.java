package com.carenest.backend.features.community.service;

import com.carenest.backend.core.api.PageResponse;
import com.carenest.backend.features.community.dto.request.CreateArticleCommentRequest;
import com.carenest.backend.features.community.dto.request.CreateArticleRequest;
import com.carenest.backend.features.community.dto.request.CreateGroupPostRequest;
import com.carenest.backend.features.community.dto.request.ReportPostRequest;
import com.carenest.backend.features.community.dto.response.ArticleCommentResponse;
import com.carenest.backend.features.community.dto.response.ArticleLikeResponse;
import com.carenest.backend.features.community.dto.response.ArticleResponse;
import com.carenest.backend.features.community.dto.response.ChatGroupPreviewResponse;
import com.carenest.backend.features.community.dto.response.ChatGroupResponse;
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

    ChatGroupPreviewResponse getChatGroupPreview(Long groupId);

    ChatGroupPreviewResponse joinChatGroup(Long groupId);

    void leaveChatGroup(Long groupId);

    PageResponse<GroupPostResponse> getGroupPosts(Long groupId, org.springframework.data.domain.Pageable pageable);

    GroupPostResponse createGroupPost(Long groupId, CreateGroupPostRequest request);

    void reportPost(Long postId, ReportPostRequest request);

    void kickMember(Long groupId, Long targetUserId);
}
