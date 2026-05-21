package com.carenest.backend.module.community.service.impl;

import com.carenest.backend.common.dto.PageResponse;
import com.carenest.backend.common.exception.ResourceNotFoundException;
import com.carenest.backend.common.exception.UnauthorizedException;
import com.carenest.backend.module.auth.entity.User;
import com.carenest.backend.module.auth.repository.UserRepository;
import com.carenest.backend.module.community.dto.request.CreateArticleCommentRequest;
import com.carenest.backend.module.community.dto.request.CreateArticleRequest;
import com.carenest.backend.module.community.dto.request.CreateGroupPostRequest;
import com.carenest.backend.module.community.dto.response.ArticleCommentResponse;
import com.carenest.backend.module.community.dto.response.ArticleLikeResponse;
import com.carenest.backend.module.community.dto.response.ArticleResponse;
import com.carenest.backend.module.community.dto.response.CommunityGroupResponse;
import com.carenest.backend.module.community.dto.response.GroupPostResponse;
import com.carenest.backend.module.community.entity.Article;
import com.carenest.backend.module.community.entity.ArticleComment;
import com.carenest.backend.module.community.entity.ArticleLike;
import com.carenest.backend.module.community.entity.CommunityGroup;
import com.carenest.backend.module.community.entity.GroupPost;
import com.carenest.backend.module.community.repository.ArticleCommentRepository;
import com.carenest.backend.module.community.repository.ArticleLikeRepository;
import com.carenest.backend.module.community.repository.ArticleRepository;
import com.carenest.backend.module.community.repository.CommunityGroupRepository;
import com.carenest.backend.module.community.repository.GroupPostRepository;
import com.carenest.backend.module.community.service.CommunityKnowledgeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommunityKnowledgeServiceImpl implements CommunityKnowledgeService {

    private final ArticleRepository articleRepository;
    private final ArticleLikeRepository articleLikeRepository;
    private final ArticleCommentRepository articleCommentRepository;
    private final CommunityGroupRepository communityGroupRepository;
    private final GroupPostRepository groupPostRepository;
    private final UserRepository userRepository;

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
        User currentUser = getCurrentUser();
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
    @Transactional
    public ArticleLikeResponse toggleArticleLike(Long articleId) {
        User currentUser = getCurrentUser();
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new ResourceNotFoundException("Article", articleId));

        boolean likedByMe;
        var existing = articleLikeRepository.findByArticleIdAndUserId(articleId, currentUser.getId());
        if (existing.isPresent()) {
            articleLikeRepository.delete(existing.get());
            likedByMe = false;
        } else {
            articleLikeRepository.save(ArticleLike.builder()
                    .article(article)
                    .user(currentUser)
                    .build());
            likedByMe = true;
        }

        return ArticleLikeResponse.builder()
                .articleId(articleId)
                .likedByMe(likedByMe)
                .likeCount(articleLikeRepository.countByArticleId(articleId))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ArticleCommentResponse> getArticleComments(Long articleId) {
        if (!articleRepository.existsById(articleId)) {
            throw new ResourceNotFoundException("Article", articleId);
        }
        return articleCommentRepository.findAllByArticleIdOrderByCreatedAtAsc(articleId)
                .stream()
                .map(this::toArticleCommentResponse)
                .toList();
    }

    @Override
    @Transactional
    public ArticleCommentResponse createArticleComment(Long articleId, CreateArticleCommentRequest request) {
        User currentUser = getCurrentUser();
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new ResourceNotFoundException("Article", articleId));

        ArticleComment comment = ArticleComment.builder()
                .article(article)
                .author(currentUser)
                .content(request.getContent().trim())
                .build();
        return toArticleCommentResponse(articleCommentRepository.save(comment));
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
        User currentUser = getCurrentUser();
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
        Long currentUserId = getCurrentUserIdOrNull();
        return ArticleResponse.builder()
                .id(article.getId())
                .title(article.getTitle())
                .content(article.getContent())
                .tags(article.getTags())
                .imageUrl(article.getImageUrl())
                .authorId(author != null ? author.getId() : null)
                .authorName(author != null ? author.getFullName() : null)
                .createdAt(article.getCreatedAt())
                .likeCount(articleLikeRepository.countByArticleId(article.getId()))
                .commentCount(articleCommentRepository.countByArticleId(article.getId()))
                .likedByMe(currentUserId != null
                        && articleLikeRepository.existsByArticleIdAndUserId(article.getId(), currentUserId))
                .build();
    }

    private Long getCurrentUserIdOrNull() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        String email = authentication.getName();
        if (email == null || "anonymousUser".equals(email)) {
            return null;
        }
        return userRepository.findByEmail(email)
                .map(User::getId)
                .orElse(null);
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("Vui lòng đăng nhập");
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof User user) {
            return user;
        }
        String email = authentication.getName();
        if (email == null || "anonymousUser".equals(email)) {
            throw new UnauthorizedException("Vui lòng đăng nhập");
        }
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("Vui lòng đăng nhập"));
    }

    private ArticleCommentResponse toArticleCommentResponse(ArticleComment comment) {
        User author = comment.getAuthor();
        Article article = comment.getArticle();
        return ArticleCommentResponse.builder()
                .id(comment.getId())
                .articleId(article != null ? article.getId() : null)
                .authorId(author != null ? author.getId() : null)
                .authorName(author != null ? author.getFullName() : null)
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
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
