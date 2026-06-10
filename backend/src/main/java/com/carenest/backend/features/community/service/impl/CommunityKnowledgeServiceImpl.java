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
import com.carenest.backend.features.community.dto.response.GroupPostInteractionResponse;
import com.carenest.backend.features.community.dto.response.GroupPostCommentResponse;
import com.carenest.backend.features.community.dto.request.CreateGroupPostCommentRequest;
import com.carenest.backend.features.community.entity.Article;
import com.carenest.backend.features.community.entity.ArticleComment;
import com.carenest.backend.features.community.entity.ArticleLike;
import com.carenest.backend.features.community.entity.ChatGroup;
import com.carenest.backend.features.community.entity.GroupPost;
import com.carenest.backend.features.community.entity.GroupPostLike;
import com.carenest.backend.features.community.entity.GroupPostComment;
import com.carenest.backend.features.community.entity.ReportTicket;
import com.carenest.backend.features.community.entity.UserGroupMembership;
import com.carenest.backend.features.community.enums.GroupRole;
import com.carenest.backend.features.community.enums.PostStatus;
import com.carenest.backend.features.community.repository.ArticleCommentRepository;
import com.carenest.backend.features.community.repository.ArticleLikeRepository;
import com.carenest.backend.features.community.repository.ArticleRepository;
import com.carenest.backend.features.community.repository.ChatGroupRepository;
import com.carenest.backend.features.community.repository.GroupPostRepository;
import com.carenest.backend.features.community.repository.GroupPostLikeRepository;
import com.carenest.backend.features.community.repository.GroupPostCommentRepository;
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
    private final GroupPostLikeRepository groupPostLikeRepository;
    private final GroupPostCommentRepository groupPostCommentRepository;
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
            throw new BadRequestException("Host không thể rời phòng tư vấn đang quản lý");
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
                .findAllByChatGroupIdAndStatusOrderByCreatedAtDesc(groupId, PostStatus.APPROVED, pageable)
                .map(this::toPostResponse);
        return PageResponse.of(page);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<GroupPostResponse> getMyGroupPosts(Long groupId, Pageable pageable) {
        User currentUser = getCurrentUser();
        getChatGroupOrThrow(groupId);
        ensureCanEnterGroup(groupId, currentUser);

        Page<GroupPostResponse> page = groupPostRepository
                .findAllByChatGroupIdAndAuthorIdOrderByCreatedAtDesc(groupId, currentUser.getId(), pageable)
                .map(this::toPostResponse);
        return PageResponse.of(page);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<GroupPostResponse> getPendingGroupPosts(Long groupId, Pageable pageable) {
        User currentUser = getCurrentUser();
        ChatGroup group = getChatGroupOrThrow(groupId);
        ensureCanModerate(group, currentUser);

        Page<GroupPostResponse> page = groupPostRepository
                .findAllByChatGroupIdAndStatusOrderByCreatedAtDesc(groupId, PostStatus.PENDING_APPROVAL, pageable)
                .map(this::toPostResponse);
        return PageResponse.of(page);
    }

    @Override
    @Transactional
    public void approveGroupPost(Long postId) {
        User currentUser = getCurrentUser();
        GroupPost post = groupPostRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("GroupPost", postId));
        
        ensureCanModerate(post.getChatGroup(), currentUser);
        
        post.setStatus(PostStatus.APPROVED);
        post.setReviewer(currentUser);
        groupPostRepository.save(post);
    }

    @Override
    @Transactional
    public void rejectGroupPost(Long postId, String reason) {
        if (reason == null || reason.trim().isEmpty()) {
            throw new BadRequestException("Lý do từ chối không được để trống");
        }
        User currentUser = getCurrentUser();
        GroupPost post = groupPostRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("GroupPost", postId));
        
        ensureCanModerate(post.getChatGroup(), currentUser);
        
        post.setStatus(PostStatus.REJECTED);
        post.setRejectionReason(reason);
        post.setReviewer(currentUser);
        groupPostRepository.save(post);
    }

    @Override
    @Transactional
    public GroupPostResponse createGroupPost(Long groupId, CreateGroupPostRequest request) {
        User currentUser = getCurrentUser();
        ChatGroup chatGroup = getChatGroupOrThrow(groupId);
        ensureCanEnterGroup(groupId, currentUser);
        enforceSlowMode(groupId, currentUser);

        // Service-level whitespace guard (catches whitespace-only strings that pass @NotBlank)
        String trimmedTitle = request.getTitle() != null ? request.getTitle().trim() : "";
        String trimmedContent = request.getContent() != null ? request.getContent().trim() : "";
        if (trimmedTitle.isBlank()) {
            throw new BadRequestException("Tiêu đề không được để trống");
        }
        if (trimmedContent.isBlank()) {
            throw new BadRequestException("Nội dung không được để trống");
        }

        GroupPost replyToPost = null;
        if (request.getReplyToPostId() != null) {
            replyToPost = groupPostRepository.findById(request.getReplyToPostId())
                    .orElseThrow(() -> new ResourceNotFoundException("GroupPost", request.getReplyToPostId()));
            if (replyToPost.getChatGroup() == null
                    || !groupId.equals(replyToPost.getChatGroup().getId())) {
                throw new BadRequestException("Bài viết được trả lời không thuộc hội nhóm này");
            }
        }

        GroupPost post = GroupPost.builder()
                .chatGroup(chatGroup)
                .author(currentUser)
                .replyToPost(replyToPost)
                .title(trimmedTitle)
                .content(trimmedContent)
                .tags(normalizeOptionalText(request.getTags()))
                .imageUrl(normalizeOptionalText(request.getImageUrl()))
                .status(PostStatus.PENDING_APPROVAL)
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

    @Override
    @Transactional
    public GroupPostInteractionResponse toggleGroupPostLike(Long postId) {
        User currentUser = getCurrentUser();
        GroupPost post = groupPostRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("GroupPost", postId));
        
        if (post.getStatus() != PostStatus.APPROVED) {
            throw new BadRequestException("Chỉ có thể tương tác với bài viết đã được duyệt");
        }
        ensureCanEnterGroup(post.getChatGroup().getId(), currentUser);

        boolean likedByMe;
        var existing = groupPostLikeRepository.findByGroupPostIdAndUserId(postId, currentUser.getId());
        if (existing.isPresent()) {
            groupPostLikeRepository.delete(existing.get());
            likedByMe = false;
        } else {
            groupPostLikeRepository.save(GroupPostLike.builder()
                    .groupPost(post)
                    .user(currentUser)
                    .build());
            likedByMe = true;
        }

        return GroupPostInteractionResponse.builder()
                .postId(postId)
                .likedByMe(likedByMe)
                .likeCount(groupPostLikeRepository.countByGroupPostId(postId))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<GroupPostCommentResponse> getGroupPostComments(Long postId, Pageable pageable) {
        User currentUser = getCurrentUser();
        GroupPost post = groupPostRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("GroupPost", postId));
        
        if (post.getStatus() != PostStatus.APPROVED) {
            throw new BadRequestException("Chỉ có thể xem bình luận của bài viết đã được duyệt");
        }
        ensureCanEnterGroup(post.getChatGroup().getId(), currentUser);

        Page<GroupPostCommentResponse> page = groupPostCommentRepository
                .findByGroupPostIdOrderByCreatedAtDesc(postId, pageable)
                .map(this::toGroupPostCommentResponse);
        return PageResponse.of(page);
    }

    @Override
    @Transactional
    public GroupPostCommentResponse createGroupPostComment(Long postId, CreateGroupPostCommentRequest request) {
        User currentUser = getCurrentUser();
        GroupPost post = groupPostRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("GroupPost", postId));

        if (post.getStatus() != PostStatus.APPROVED) {
            throw new BadRequestException("Chỉ có thể bình luận trên bài viết đã được duyệt");
        }
        ensureCanEnterGroup(post.getChatGroup().getId(), currentUser);

        String trimmedContent = normalizeOptionalText(request.getContent());
        if (trimmedContent == null) {
            throw new BadRequestException("Nội dung bình luận không được để trống");
        }

        GroupPostComment comment = GroupPostComment.builder()
                .groupPost(post)
                .author(currentUser)
                .content(trimmedContent)
                .build();

        return toGroupPostCommentResponse(groupPostCommentRepository.save(comment));
    }

    private GroupPostCommentResponse toGroupPostCommentResponse(GroupPostComment comment) {
        User author = comment.getAuthor();
        GroupPost post = comment.getGroupPost();
        return GroupPostCommentResponse.builder()
                .id(comment.getId())
                .groupPostId(post != null ? post.getId() : null)
                .authorId(author != null ? author.getId() : null)
                .authorName(author != null ? author.getFullName() : null)
                .authorAvatarUrl(author != null ? author.getAvatarUrl() : null)
                .authorRole(author != null && author.getRole() != null ? author.getRole().name() : null)
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .build();
    }

    private void enforceSlowMode(Long groupId, User currentUser) {
        if (currentUser.getRole() == null || currentUser.getRole() == Role.DOCTOR || currentUser.getRole() == Role.ADMIN) {
            return;
        }

        String key = groupId + ":" + currentUser.getId();
        Instant now = Instant.now();
        Instant lastPostTime = lastUserPostAt.get(key);
        if (lastPostTime != null) {
            long secondsSinceLastPost = java.time.Duration.between(lastPostTime, now).getSeconds();
            if (secondsSinceLastPost < 60) {
                long remaining = 60 - secondsSinceLastPost;
                throw new BadRequestException("Vui lòng đợi " + remaining + " giây trước khi gửi bài viết tiếp theo");
            }
        }
        lastUserPostAt.put(key, now);
    }

    private ChatGroup getChatGroupOrThrow(Long groupId) {
        return chatGroupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("ChatGroup", groupId));
    }

    private void ensureCanEnterGroup(Long groupId, User user) {
        boolean isMember = membershipRepository.existsByGroupIdAndUserId(groupId, user.getId());
        if (!isMember && user.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Bạn phải tham gia nhóm trước khi xem hoặc gửi bài viết");
        }
    }

    private void ensureCanModerate(ChatGroup group, User currentUser) {
        if (currentUser.getRole() == Role.ADMIN) {
            return;
        }
        boolean isHost = membershipRepository.existsByGroupIdAndUserIdAndGroupRole(
                group.getId(),
                currentUser.getId(),
                GroupRole.HOST
        );
        if (!isHost) {
            throw new AccessDeniedException("Chỉ Quản trị viên và Host mới có quyền duyệt bài viết");
        }
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
                .authorRole(author != null && author.getRole() != null ? author.getRole().name() : null)
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
                .latestMessage(latestPost != null ? latestPost.getContent() : "Nhóm vừa được tạo")
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
                .rules("Trao đổi văn minh, không tự ý chẩn đoán hoặc thay thế chỉ định của bác sĩ. Khi có dấu hiệu nguy hiểm, hãy liên hệ cơ sở y tế gần nhất.")
                .build();
    }

    private GroupPostResponse toPostResponse(GroupPost post) {
        User author = post.getAuthor();
        ChatGroup group = post.getChatGroup();
        Long currentUserId = getCurrentUserIdOrNull();

        return GroupPostResponse.builder()
                .id(post.getId())
                .chatGroupId(group != null ? group.getId() : null)
                .chatGroupName(group != null ? group.getName() : null)
                .authorId(author != null ? author.getId() : null)
                .authorName(author != null ? author.getFullName() : null)
                .authorRole(author != null && author.getRole() != null ? author.getRole().name() : null)
                .title(post.getTitle())
                .content(post.getContent())
                .tags(post.getTags())
                .replyToPostId(post.getReplyToPost() != null ? post.getReplyToPost().getId() : null)
                .imageUrl(post.getImageUrl())
                .createdAt(post.getCreatedAt())
                .status(post.getStatus())
                .rejectionReason(post.getRejectionReason())
                .likeCount(groupPostLikeRepository.countByGroupPostId(post.getId()))
                .commentCount(groupPostCommentRepository.countByGroupPostId(post.getId()))
                .likedByMe(currentUserId != null && groupPostLikeRepository.existsByGroupPostIdAndUserId(post.getId(), currentUserId))
                .build();
    }

}



