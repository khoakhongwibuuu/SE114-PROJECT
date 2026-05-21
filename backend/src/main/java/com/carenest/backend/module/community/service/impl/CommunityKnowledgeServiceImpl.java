package com.carenest.backend.module.community.service.impl;

import com.carenest.backend.common.dto.PageResponse;
import com.carenest.backend.common.exception.BadRequestException;
import com.carenest.backend.common.exception.ResourceNotFoundException;
import com.carenest.backend.common.exception.UnauthorizedException;
import com.carenest.backend.module.auth.entity.User;
import com.carenest.backend.module.auth.enums.Role;
import com.carenest.backend.module.auth.repository.UserRepository;
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
import com.carenest.backend.module.community.entity.Article;
import com.carenest.backend.module.community.entity.ArticleComment;
import com.carenest.backend.module.community.entity.ArticleLike;
import com.carenest.backend.module.community.entity.CommunityGroup;
import com.carenest.backend.module.community.entity.GroupPost;
import com.carenest.backend.module.community.entity.ReportTicket;
import com.carenest.backend.module.community.entity.UserGroupMembership;
import com.carenest.backend.module.community.enums.GroupRole;
import com.carenest.backend.module.community.repository.ArticleCommentRepository;
import com.carenest.backend.module.community.repository.ArticleLikeRepository;
import com.carenest.backend.module.community.repository.ArticleRepository;
import com.carenest.backend.module.community.repository.CommunityGroupRepository;
import com.carenest.backend.module.community.repository.GroupPostRepository;
import com.carenest.backend.module.community.repository.ReportTicketRepository;
import com.carenest.backend.module.community.repository.UserGroupMembershipRepository;
import com.carenest.backend.module.community.service.CommunityKnowledgeService;
import com.carenest.backend.module.doctorverification.entity.DoctorVerification;
import com.carenest.backend.module.doctorverification.repository.DoctorVerificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class CommunityKnowledgeServiceImpl implements CommunityKnowledgeService {

    private static final long USER_SLOW_MODE_SECONDS = 5L;
    private final Map<String, Instant> lastUserPostAt = new ConcurrentHashMap<>();

    private final ArticleRepository articleRepository;
    private final ArticleLikeRepository articleLikeRepository;
    private final ArticleCommentRepository articleCommentRepository;
    private final CommunityGroupRepository communityGroupRepository;
    private final GroupPostRepository groupPostRepository;
    private final UserGroupMembershipRepository membershipRepository;
    private final ReportTicketRepository reportTicketRepository;
    private final UserRepository userRepository;
    private final DoctorVerificationRepository doctorVerificationRepository;

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
    public List<CommunityGroupResponse> getCommunityGroups(String search, String category) {
        String keyword = normalizeOptionalText(search);
        String normalizedCategory = normalizeOptionalText(category);
        return communityGroupRepository.searchGroups(keyword, normalizedCategory)
                .stream()
                .map(group -> toGroupResponse(group, getCurrentUserIdOrNull()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommunityGroupResponse> getMyCommunityGroups(String search) {
        User currentUser = getCurrentUser();
        String keyword = normalizeOptionalText(search);
        return membershipRepository.findAllByUserIdOrderByJoinedAtDesc(currentUser.getId())
                .stream()
                .map(UserGroupMembership::getGroup)
                .filter(group -> matchesGroupSearch(group, keyword))
                .map(group -> toGroupResponse(group, currentUser.getId()))
                .sorted(Comparator.comparing(
                        CommunityGroupResponse::getLatestActivityAt,
                        Comparator.nullsLast(Comparator.reverseOrder())
                ))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommunityGroupResponse> getDiscoverCommunityGroups(String search) {
        User currentUser = getCurrentUser();
        String keyword = normalizeOptionalText(search);
        return communityGroupRepository.searchGroups(keyword, null)
                .stream()
                .filter(group -> !membershipRepository.existsByGroupIdAndUserId(group.getId(), currentUser.getId()))
                .map(group -> toGroupResponse(group, currentUser.getId()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CommunityGroupPreviewResponse getCommunityGroupPreview(Long groupId) {
        User currentUser = getCurrentUser();
        CommunityGroup group = getCommunityGroupOrThrow(groupId);
        return toGroupPreviewResponse(group, currentUser);
    }

    @Override
    @Transactional
    public CommunityGroupPreviewResponse joinCommunityGroup(Long groupId) {
        User currentUser = getCurrentUser();
        CommunityGroup group = getCommunityGroupOrThrow(groupId);

        membershipRepository.findByGroupIdAndUserId(groupId, currentUser.getId())
                .orElseGet(() -> membershipRepository.save(UserGroupMembership.builder()
                        .group(group)
                        .user(currentUser)
                        .groupRole(isLeadDoctor(group, currentUser) ? GroupRole.HOST : GroupRole.MEMBER)
                        .build()));

        return toGroupPreviewResponse(group, currentUser);
    }

    @Override
    @Transactional
    public void leaveCommunityGroup(Long groupId) {
        User currentUser = getCurrentUser();
        UserGroupMembership membership = membershipRepository.findByGroupIdAndUserId(groupId, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("UserGroupMembership", "groupId", groupId.toString()));

        if (membership.getGroupRole() == GroupRole.HOST && currentUser.getRole() != Role.ADMIN) {
            throw new BadRequestException("Host không thể rời phòng tư vấn đang quản lý");
        }

        membershipRepository.delete(membership);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<GroupPostResponse> getGroupPosts(Long groupId, Pageable pageable) {
        User currentUser = getCurrentUser();
        getCommunityGroupOrThrow(groupId);
        ensureCanEnterGroup(groupId, currentUser);

        Page<GroupPostResponse> page = groupPostRepository
                .findAllByCommunityGroupIdOrderByCreatedAtDesc(groupId, pageable)
                .map(this::toPostResponse);
        return PageResponse.of(page);
    }

    @Override
    @Transactional
    public GroupPostResponse createGroupPost(Long groupId, CreateGroupPostRequest request) {
        User currentUser = getCurrentUser();
        CommunityGroup communityGroup = getCommunityGroupOrThrow(groupId);
        ensureCanEnterGroup(groupId, currentUser);
        enforceSlowMode(groupId, currentUser);

        GroupPost replyToPost = null;
        if (request.getReplyToPostId() != null) {
            replyToPost = groupPostRepository.findById(request.getReplyToPostId())
                    .orElseThrow(() -> new ResourceNotFoundException("GroupPost", request.getReplyToPostId()));
            if (replyToPost.getCommunityGroup() == null
                    || !groupId.equals(replyToPost.getCommunityGroup().getId())) {
                throw new BadRequestException("Tin nhắn được trả lời không thuộc hội nhóm này");
            }
        }

        GroupPost post = GroupPost.builder()
                .communityGroup(communityGroup)
                .author(currentUser)
                .replyToPost(replyToPost)
                .content(request.getContent().trim())
                .imageUrl(normalizeOptionalText(request.getImageUrl()))
                .build();
        return toPostResponse(groupPostRepository.save(post));
    }

    @Override
    @Transactional
    public void reportPost(Long postId, ReportPostRequest request) {
        User currentUser = getCurrentUser();
        GroupPost post = groupPostRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("GroupPost", postId));
        CommunityGroup group = post.getCommunityGroup();
        if (group == null) {
            throw new ResourceNotFoundException("CommunityGroup", "postId", postId.toString());
        }
        ensureCanEnterGroup(group.getId(), currentUser);

        reportTicketRepository.save(ReportTicket.builder()
                .reportedPost(post)
                .reporter(currentUser)
                .reason(request.getReason().trim())
                .build());
    }

    @Override
    @Transactional
    public void kickMember(Long groupId, Long targetUserId) {
        User currentUser = getCurrentUser();
        CommunityGroup group = getCommunityGroupOrThrow(groupId);
        UserGroupMembership targetMembership = membershipRepository.findByGroupIdAndUserId(groupId, targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("UserGroupMembership", "targetUserId", targetUserId.toString()));

        boolean isSystemAdmin = currentUser.getRole() == Role.ADMIN;
        boolean isHostOfThisGroup = membershipRepository.existsByGroupIdAndUserIdAndGroupRole(
                group.getId(),
                currentUser.getId(),
                GroupRole.HOST
        );

        if (!isSystemAdmin && !isHostOfThisGroup) {
            throw new AccessDeniedException("Bạn không có quyền mời thành viên ra khỏi nhóm này");
        }

        if (currentUser.getId().equals(targetUserId)) {
            throw new BadRequestException("Bạn không thể tự mời chính mình ra khỏi nhóm");
        }

        if (targetMembership.getGroupRole() == GroupRole.HOST && !isSystemAdmin) {
            throw new AccessDeniedException("Chỉ Admin hệ thống mới có quyền gỡ Host khỏi nhóm");
        }

        membershipRepository.delete(targetMembership);
    }

    private void enforceSlowMode(Long groupId, User currentUser) {
        if (currentUser.getRole() == null || currentUser.getRole() == Role.DOCTOR || currentUser.getRole() == Role.ADMIN) {
            return;
        }

        String key = groupId + ":" + currentUser.getId();
        Instant now = Instant.now();
        Instant lastSentAt = lastUserPostAt.get(key);
        if (lastSentAt != null) {
            long elapsed = now.getEpochSecond() - lastSentAt.getEpochSecond();
            if (elapsed < USER_SLOW_MODE_SECONDS) {
                long remaining = USER_SLOW_MODE_SECONDS - elapsed;
                throw new BadRequestException("Vui lòng chờ " + remaining + " giây trước khi gửi tin nhắn tiếp theo");
            }
        }
        lastUserPostAt.put(key, now);
    }

    private CommunityGroup getCommunityGroupOrThrow(Long groupId) {
        return communityGroupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("CommunityGroup", groupId));
    }

    private void ensureCanEnterGroup(Long groupId, User currentUser) {
        if (currentUser.getRole() == Role.ADMIN || membershipRepository.existsByGroupIdAndUserId(groupId, currentUser.getId())) {
            return;
        }
        throw new AccessDeniedException("Vui lòng tham gia nhóm trước khi xem hoặc gửi tin nhắn");
    }

    private boolean isLeadDoctor(CommunityGroup group, User user) {
        return group.getLeadDoctor() != null && group.getLeadDoctor().getId().equals(user.getId());
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
        DoctorVerification verification = author != null && author.getRole() == Role.DOCTOR
                ? doctorVerificationRepository.findByUserId(author.getId()).orElse(null)
                : null;
        String specialty = verification != null ? verification.getSpecialty() : null;
        CommunityGroup privateGroup = author != null
                ? communityGroupRepository.findByLeadDoctorIdAndIsPrivateTrue(author.getId()).orElse(null)
                : null;
        CommunityGroup specialtyGroup = specialty != null
                ? communityGroupRepository.findFirstByCategoryIgnoreCaseAndIsPrivateFalse(specialty).orElse(null)
                : null;
        return ArticleResponse.builder()
                .id(article.getId())
                .title(article.getTitle())
                .content(article.getContent())
                .tags(article.getTags())
                .imageUrl(article.getImageUrl())
                .authorId(author != null ? author.getId() : null)
                .authorName(author != null ? author.getFullName() : null)
                .authorAvatarUrl(author != null ? author.getAvatarUrl() : null)
                .authorRole(author != null && author.getRole() != null ? author.getRole().name() : null)
                .authorSpecialty(specialty)
                .authorHospitalName(verification != null ? verification.getHospitalName() : null)
                .authorPrivateGroupId(privateGroup != null ? privateGroup.getId() : null)
                .authorSpecialtyGroupId(specialtyGroup != null ? specialtyGroup.getId() : null)
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

    private boolean matchesGroupSearch(CommunityGroup group, String keyword) {
        if (keyword == null) {
            return true;
        }
        String normalized = keyword.toLowerCase();
        return containsIgnoreCase(group.getName(), normalized)
                || containsIgnoreCase(group.getDescription(), normalized)
                || containsIgnoreCase(group.getCategory(), normalized)
                || containsIgnoreCase(group.getTags(), normalized)
                || (group.getLeadDoctor() != null && containsIgnoreCase(group.getLeadDoctor().getFullName(), normalized));
    }

    private boolean containsIgnoreCase(String value, String normalizedKeyword) {
        return value != null && value.toLowerCase().contains(normalizedKeyword);
    }

    private CommunityGroupResponse toGroupResponse(CommunityGroup group, Long currentUserId) {
        User leadDoctor = group.getLeadDoctor();
        GroupPost latestPost = groupPostRepository.findFirstByCommunityGroupIdOrderByCreatedAtDesc(group.getId()).orElse(null);
        return CommunityGroupResponse.builder()
                .id(group.getId())
                .name(group.getName())
                .description(group.getDescription())
                .category(group.getCategory())
                .tags(group.getTags())
                .isPrivate(group.isPrivate())
                .leadDoctorId(leadDoctor != null ? leadDoctor.getId() : null)
                .leadDoctorName(leadDoctor != null ? leadDoctor.getFullName() : null)
                .memberCount(membershipRepository.countByGroupId(group.getId()))
                .joined(currentUserId != null && membershipRepository.existsByGroupIdAndUserId(group.getId(), currentUserId))
                .latestMessage(latestPost != null ? latestPost.getContent() : "Nhóm vừa được tạo")
                .latestActivityAt(latestPost != null ? latestPost.getCreatedAt() : group.getCreatedAt())
                .build();
    }

    private CommunityGroupPreviewResponse toGroupPreviewResponse(CommunityGroup group, User currentUser) {
        User leadDoctor = group.getLeadDoctor();
        var membership = membershipRepository.findByGroupIdAndUserId(group.getId(), currentUser.getId()).orElse(null);
        return CommunityGroupPreviewResponse.builder()
                .id(group.getId())
                .name(group.getName())
                .description(group.getDescription())
                .category(group.getCategory())
                .tags(group.getTags())
                .isPrivate(group.isPrivate())
                .leadDoctorId(leadDoctor != null ? leadDoctor.getId() : null)
                .leadDoctorName(leadDoctor != null ? leadDoctor.getFullName() : null)
                .memberCount(membershipRepository.countByGroupId(group.getId()))
                .joined(membership != null || currentUser.getRole() == Role.ADMIN)
                .myRole(membership != null ? membership.getGroupRole() : null)
                .rules("Trao đổi văn minh, không tự ý chẩn đoán hoặc thay thế chỉ định của bác sĩ. Khi có dấu hiệu nguy hiểm, hãy liên hệ cơ sở y tế gần nhất.")
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
                .authorRole(author != null && author.getRole() != null ? author.getRole().name() : null)
                .content(post.getContent())
                .replyToPostId(post.getReplyToPost() != null ? post.getReplyToPost().getId() : null)
                .imageUrl(post.getImageUrl())
                .createdAt(post.getCreatedAt())
                .build();
    }
}
