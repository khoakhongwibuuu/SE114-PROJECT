package com.carenest.backend.features.community.service.impl;

import com.carenest.backend.core.api.PageResponse;
import com.carenest.backend.core.exception.BadRequestException;
import com.carenest.backend.core.exception.ResourceNotFoundException;
import com.carenest.backend.core.exception.UnauthorizedException;
import com.carenest.backend.features.auth.entity.User;
import com.carenest.backend.features.auth.enums.Role;
import com.carenest.backend.features.auth.repository.UserRepository;
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
import com.carenest.backend.features.community.entity.Article;
import com.carenest.backend.features.community.entity.ArticleComment;
import com.carenest.backend.features.community.entity.ArticleLike;
import com.carenest.backend.features.community.entity.ChatGroup;
import com.carenest.backend.features.community.entity.GroupPost;
import com.carenest.backend.features.community.entity.ReportTicket;
import com.carenest.backend.features.community.entity.UserGroupMembership;
import com.carenest.backend.features.community.enums.GroupRole;
import com.carenest.backend.features.community.repository.ArticleCommentRepository;
import com.carenest.backend.features.community.repository.ArticleLikeRepository;
import com.carenest.backend.features.community.repository.ArticleRepository;
import com.carenest.backend.features.community.repository.ChatGroupRepository;
import com.carenest.backend.features.community.repository.GroupPostRepository;
import com.carenest.backend.features.community.repository.ReportTicketRepository;
import com.carenest.backend.features.community.repository.UserGroupMembershipRepository;
import com.carenest.backend.features.community.service.CommunityKnowledgeService;
import com.carenest.backend.features.doctorverification.entity.DoctorVerification;
import com.carenest.backend.features.doctorverification.repository.DoctorVerificationRepository;
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
    private final ChatGroupRepository chatGroupRepository;
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
    public List<ChatGroupResponse> getChatGroups(String search, String category) {
        String keyword = normalizeOptionalText(search);
        String normalizedCategory = normalizeOptionalText(category);
        return chatGroupRepository.searchGroups(keyword, normalizedCategory)
                .stream()
                .map(group -> toGroupResponse(group, getCurrentUserIdOrNull()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatGroupResponse> getMyChatGroups(String search) {
        User currentUser = getCurrentUser();
        String keyword = normalizeOptionalText(search);
        return membershipRepository.findAllByUserIdOrderByJoinedAtDesc(currentUser.getId())
                .stream()
                .map(UserGroupMembership::getGroup)
                .filter(group -> matchesGroupSearch(group, keyword))
                .map(group -> toGroupResponse(group, currentUser.getId()))
                .sorted(Comparator.comparing(
                        ChatGroupResponse::getLatestActivityAt,
                        Comparator.nullsLast(Comparator.reverseOrder())
                ))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatGroupResponse> getDiscoverChatGroups(String search) {
        User currentUser = getCurrentUser();
        String keyword = normalizeOptionalText(search);
        return chatGroupRepository.searchGroups(keyword, null)
                .stream()
                .filter(group -> !membershipRepository.existsByGroupIdAndUserId(group.getId(), currentUser.getId()))
                .map(group -> toGroupResponse(group, currentUser.getId()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ChatGroupPreviewResponse getChatGroupPreview(Long groupId) {
        User currentUser = getCurrentUser();
        ChatGroup group = getChatGroupOrThrow(groupId);
        return toGroupPreviewResponse(group, currentUser);
    }

    @Override
    @Transactional
    public ChatGroupPreviewResponse joinChatGroup(Long groupId) {
        User currentUser = getCurrentUser();
        ChatGroup group = getChatGroupOrThrow(groupId);

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
    public void leaveChatGroup(Long groupId) {
        User currentUser = getCurrentUser();
        UserGroupMembership membership = membershipRepository.findByGroupIdAndUserId(groupId, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("UserGroupMembership", "groupId", groupId.toString()));

        if (membership.getGroupRole() == GroupRole.HOST && currentUser.getRole() != Role.ADMIN) {
            throw new BadRequestException("Host khÃ´ng thá»ƒ rá»i phÃ²ng tÆ° váº¥n Ä‘ang quáº£n lÃ½");
        }

        membershipRepository.delete(membership);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<GroupPostResponse> getGroupPosts(Long groupId, Pageable pageable) {
        User currentUser = getCurrentUser();
        getChatGroupOrThrow(groupId);
        ensureCanEnterGroup(groupId, currentUser);

        Page<GroupPostResponse> page = groupPostRepository
                .findAllByChatGroupIdOrderByCreatedAtDesc(groupId, pageable)
                .map(this::toPostResponse);
        return PageResponse.of(page);
    }

    @Override
    @Transactional
    public GroupPostResponse createGroupPost(Long groupId, CreateGroupPostRequest request) {
        User currentUser = getCurrentUser();
        ChatGroup chatGroup = getChatGroupOrThrow(groupId);
        ensureCanEnterGroup(groupId, currentUser);
        enforceSlowMode(groupId, currentUser);

        GroupPost replyToPost = null;
        if (request.getReplyToPostId() != null) {
            replyToPost = groupPostRepository.findById(request.getReplyToPostId())
                    .orElseThrow(() -> new ResourceNotFoundException("GroupPost", request.getReplyToPostId()));
            if (replyToPost.getChatGroup() == null
                    || !groupId.equals(replyToPost.getChatGroup().getId())) {
                throw new BadRequestException("Tin nháº¯n Ä‘Æ°á»£c tráº£ lá»i khÃ´ng thuá»™c há»™i nhÃ³m nÃ y");
            }
        }

        GroupPost post = GroupPost.builder()
                .chatGroup(chatGroup)
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
        ChatGroup group = post.getChatGroup();
        if (group == null) {
            throw new ResourceNotFoundException("ChatGroup", "postId", postId.toString());
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
        ChatGroup group = getChatGroupOrThrow(groupId);
        UserGroupMembership targetMembership = membershipRepository.findByGroupIdAndUserId(groupId, targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("UserGroupMembership", "targetUserId", targetUserId.toString()));

        boolean isSystemAdmin = currentUser.getRole() == Role.ADMIN;
        boolean isHostOfThisGroup = membershipRepository.existsByGroupIdAndUserIdAndGroupRole(
                group.getId(),
                currentUser.getId(),
                GroupRole.HOST
        );

        if (!isSystemAdmin && !isHostOfThisGroup) {
            throw new AccessDeniedException("Báº¡n khÃ´ng cÃ³ quyá»n má»i thÃ nh viÃªn ra khá»i nhÃ³m nÃ y");
        }

        if (currentUser.getId().equals(targetUserId)) {
            throw new BadRequestException("Báº¡n khÃ´ng thá»ƒ tá»± má»i chÃ­nh mÃ¬nh ra khá»i nhÃ³m");
        }

        if (targetMembership.getGroupRole() == GroupRole.HOST && !isSystemAdmin) {
            throw new AccessDeniedException("Chá»‰ Admin há»‡ thá»‘ng má»›i cÃ³ quyá»n gá»¡ Host khá»i nhÃ³m");
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
                throw new BadRequestException("Vui lÃ²ng chá» " + remaining + " giÃ¢y trÆ°á»›c khi gá»­i tin nháº¯n tiáº¿p theo");
            }
        }
        lastUserPostAt.put(key, now);
    }

    private ChatGroup getChatGroupOrThrow(Long groupId) {
        return chatGroupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("ChatGroup", groupId));
    }

    private void ensureCanEnterGroup(Long groupId, User currentUser) {
        if (currentUser.getRole() == Role.ADMIN || membershipRepository.existsByGroupIdAndUserId(groupId, currentUser.getId())) {
            return;
        }
        throw new AccessDeniedException("Vui lÃ²ng tham gia nhÃ³m trÆ°á»›c khi xem hoáº·c gá»­i tin nháº¯n");
    }

    private boolean isLeadDoctor(ChatGroup group, User user) {
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
        ChatGroup privateGroup = author != null
                ? chatGroupRepository.findByLeadDoctorIdAndIsPrivateTrue(author.getId()).orElse(null)
                : null;
        ChatGroup specialtyGroup = specialty != null
                ? chatGroupRepository.findFirstByCategoryIgnoreCaseAndIsPrivateFalse(specialty).orElse(null)
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
            throw new UnauthorizedException("Vui lÃ²ng Ä‘Äƒng nháº­p");
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof User user) {
            return user;
        }
        String email = authentication.getName();
        if (email == null || "anonymousUser".equals(email)) {
            throw new UnauthorizedException("Vui lÃ²ng Ä‘Äƒng nháº­p");
        }
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("Vui lÃ²ng Ä‘Äƒng nháº­p"));
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

    private boolean matchesGroupSearch(ChatGroup group, String keyword) {
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

    private ChatGroupResponse toGroupResponse(ChatGroup group, Long currentUserId) {
        User leadDoctor = group.getLeadDoctor();
        GroupPost latestPost = groupPostRepository.findFirstByChatGroupIdOrderByCreatedAtDesc(group.getId()).orElse(null);
        return ChatGroupResponse.builder()
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
                .latestMessage(latestPost != null ? latestPost.getContent() : "NhÃ³m vá»«a Ä‘Æ°á»£c táº¡o")
                .latestActivityAt(latestPost != null ? latestPost.getCreatedAt() : group.getCreatedAt())
                .build();
    }

    private ChatGroupPreviewResponse toGroupPreviewResponse(ChatGroup group, User currentUser) {
        User leadDoctor = group.getLeadDoctor();
        var membership = membershipRepository.findByGroupIdAndUserId(group.getId(), currentUser.getId()).orElse(null);
        return ChatGroupPreviewResponse.builder()
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
                .rules("Trao Ä‘á»•i vÄƒn minh, khÃ´ng tá»± Ã½ cháº©n Ä‘oÃ¡n hoáº·c thay tháº¿ chá»‰ Ä‘á»‹nh cá»§a bÃ¡c sÄ©. Khi cÃ³ dáº¥u hiá»‡u nguy hiá»ƒm, hÃ£y liÃªn há»‡ cÆ¡ sá»Ÿ y táº¿ gáº§n nháº¥t.")
                .build();
    }

    private GroupPostResponse toPostResponse(GroupPost post) {
        User author = post.getAuthor();
        ChatGroup group = post.getChatGroup();
        return GroupPostResponse.builder()
                .id(post.getId())
                .chatGroupId(group != null ? group.getId() : null)
                .chatGroupName(group != null ? group.getName() : null)
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
