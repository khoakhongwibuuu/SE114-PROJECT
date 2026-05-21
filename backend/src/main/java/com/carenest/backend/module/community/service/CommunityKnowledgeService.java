package com.carenest.backend.module.community.service;

import com.carenest.backend.common.dto.PageResponse;
import com.carenest.backend.module.community.dto.request.CreateArticleCommentRequest;
import com.carenest.backend.module.community.dto.request.CreateArticleRequest;
import com.carenest.backend.module.community.dto.request.CreateGroupPostRequest;
import com.carenest.backend.module.community.dto.request.ReportPostRequest;
import com.carenest.backend.module.community.dto.response.ArticleCommentResponse;
import com.carenest.backend.module.community.dto.response.ArticleLikeResponse;
import com.carenest.backend.module.community.dto.response.ArticleResponse;
import com.carenest.backend.module.community.dto.response.CommunityGroupPreviewResponse;
import com.carenest.backend.module.community.dto.response.CommunityGroupResponse;
import com.carenest.backend.module.community.dto.response.GroupPostResponse;

import java.util.List;

public interface CommunityKnowledgeService {
    List<ArticleResponse> getArticles();

    ArticleResponse createArticle(CreateArticleRequest request);

    ArticleLikeResponse toggleArticleLike(Long articleId);

    List<ArticleCommentResponse> getArticleComments(Long articleId);

    ArticleCommentResponse createArticleComment(Long articleId, CreateArticleCommentRequest request);

    List<CommunityGroupResponse> getCommunityGroups(String search, String category);

    List<CommunityGroupResponse> getMyCommunityGroups(String search);

    List<CommunityGroupResponse> getDiscoverCommunityGroups(String search);

    CommunityGroupPreviewResponse getCommunityGroupPreview(Long groupId);

    CommunityGroupPreviewResponse joinCommunityGroup(Long groupId);

    void leaveCommunityGroup(Long groupId);

    PageResponse<GroupPostResponse> getGroupPosts(Long groupId, org.springframework.data.domain.Pageable pageable);

    GroupPostResponse createGroupPost(Long groupId, CreateGroupPostRequest request);

    void reportPost(Long postId, ReportPostRequest request);

    void kickMember(Long groupId, Long targetUserId);
}
