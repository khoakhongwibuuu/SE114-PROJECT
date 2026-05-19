package com.carenest.backend.module.community.service;

import com.carenest.backend.module.community.dto.request.CreateArticleRequest;
import com.carenest.backend.module.community.dto.request.CreateGroupPostRequest;
import com.carenest.backend.module.community.dto.response.ArticleResponse;
import com.carenest.backend.module.community.dto.response.CommunityGroupResponse;
import com.carenest.backend.module.community.dto.response.GroupPostResponse;

import java.util.List;

public interface CommunityKnowledgeService {
    List<ArticleResponse> getArticles();

    ArticleResponse createArticle(CreateArticleRequest request);

    List<CommunityGroupResponse> getCommunityGroups();

    List<GroupPostResponse> getGroupPosts(Long groupId);

    GroupPostResponse createGroupPost(Long groupId, CreateGroupPostRequest request);
}
