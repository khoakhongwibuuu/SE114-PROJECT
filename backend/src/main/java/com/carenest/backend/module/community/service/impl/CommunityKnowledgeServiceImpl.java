package com.carenest.backend.module.community.service.impl;

import com.carenest.backend.common.dto.PageResponse;
import com.carenest.backend.common.exception.ResourceNotFoundException;
import com.carenest.backend.module.auth.entity.User;
import com.carenest.backend.module.community.dto.request.CreateArticleRequest;
import com.carenest.backend.module.community.dto.request.CreateGroupPostRequest;
import com.carenest.backend.module.community.dto.response.ArticleResponse;
import com.carenest.backend.module.community.dto.response.CommunityGroupResponse;
import com.carenest.backend.module.community.dto.response.GroupPostResponse;
import com.carenest.backend.module.community.entity.Article;
import com.carenest.backend.module.community.entity.CommunityGroup;
import com.carenest.backend.module.community.entity.GroupPost;
import com.carenest.backend.module.community.repository.ArticleRepository;
import com.carenest.backend.module.community.repository.CommunityGroupRepository;
import com.carenest.backend.module.community.repository.GroupPostRepository;
import com.carenest.backend.module.community.service.CommunityKnowledgeService;
import com.carenest.backend.module.family.util.FamilySecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommunityKnowledgeServiceImpl implements CommunityKnowledgeService {

    private final ArticleRepository articleRepository;
    private final CommunityGroupRepository communityGroupRepository;
    private final GroupPostRepository groupPostRepository;
    private final FamilySecurityUtil familySecurityUtil;

    @Override
    @Transactional(readOnly = true)
    public List<ArticleResponse> getArticles() {
        return articleRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toArticleResponse)
                .toList();
    }

    @Override
    @Transactional
    public ArticleResponse createArticle(CreateArticleRequest request) {
        User currentUser = familySecurityUtil.getCurrentUser();
        Article article = Article.builder()
                .title(request.getTitle().trim())
                .content(request.getContent().trim())
                .tags(normalizeOptionalText(request.getTags()))
                .imageUrl(normalizeOptionalText(request.getImageUrl()))
                .author(currentUser)
                .build();
        return toArticleResponse(articleRepository.save(article));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommunityGroupResponse> getCommunityGroups() {
        return communityGroupRepository.findAllByOrderByNameAsc()
                .stream()
                .map(this::toGroupResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<GroupPostResponse> getGroupPosts(Long groupId, Pageable pageable) {
        if (!communityGroupRepository.existsById(groupId)) {
            throw new ResourceNotFoundException("CommunityGroup", groupId);
        }
        Page<GroupPostResponse> page = groupPostRepository
                .findAllByCommunityGroupIdOrderByCreatedAtDesc(groupId, pageable)
                .map(this::toPostResponse);
        return PageResponse.of(page);
    }

    @Override
    @Transactional
    public GroupPostResponse createGroupPost(Long groupId, CreateGroupPostRequest request) {
        User currentUser = familySecurityUtil.getCurrentUser();
        CommunityGroup communityGroup = communityGroupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("CommunityGroup", groupId));

        GroupPost post = GroupPost.builder()
                .communityGroup(communityGroup)
                .author(currentUser)
                .content(request.getContent().trim())
                .build();
        return toPostResponse(groupPostRepository.save(post));
    }

    private String normalizeOptionalText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private ArticleResponse toArticleResponse(Article article) {
        User author = article.getAuthor();
        return ArticleResponse.builder()
                .id(article.getId())
                .title(article.getTitle())
                .content(article.getContent())
                .tags(article.getTags())
                .imageUrl(article.getImageUrl())
                .authorId(author != null ? author.getId() : null)
                .authorName(author != null ? author.getFullName() : null)
                .createdAt(article.getCreatedAt())
                .build();
    }

    private CommunityGroupResponse toGroupResponse(CommunityGroup group) {
        return CommunityGroupResponse.builder()
                .id(group.getId())
                .name(group.getName())
                .description(group.getDescription())
                .build();
    }

    private GroupPostResponse toPostResponse(GroupPost post) {
        User author = post.getAuthor();
        CommunityGroup group = post.getCommunityGroup();
        return GroupPostResponse.builder()
                .id(post.getId())
                .communityGroupId(group != null ? group.getId() : null)
                .communityGroupName(group != null ? group.getName() : null)
                .authorId(author != null ? author.getId() : null)
                .authorName(author != null ? author.getFullName() : null)
                .content(post.getContent())
                .createdAt(post.getCreatedAt())
                .build();
    }
}
