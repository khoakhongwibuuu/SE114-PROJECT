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
import com.carenest.backend.features.community.dto.request.CreateGroupCreationRequest;
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
import com.carenest.backend.features.community.dto.response.GroupPostResponse;
import com.carenest.backend.features.community.dto.response.GroupPostInteractionResponse;
import com.carenest.backend.features.community.dto.response.GroupPostCommentResponse;
import com.carenest.backend.features.community.dto.request.CreateGroupPostCommentRequest;
import com.carenest.backend.features.community.entity.Article;
import com.carenest.backend.features.community.entity.ArticleComment;
import com.carenest.backend.features.community.entity.ArticleLike;
import com.carenest.backend.features.community.entity.ChatGroup;
import com.carenest.backend.features.community.entity.GroupCreationAuditLog;
import com.carenest.backend.features.community.entity.GroupCreationRequest;
import com.carenest.backend.features.community.entity.GroupGovernanceAuditLog;
import com.carenest.backend.features.community.entity.GroupPost;
import com.carenest.backend.features.community.entity.GroupPostLike;
import com.carenest.backend.features.community.entity.GroupPostComment;
import com.carenest.backend.features.community.entity.ReportTicket;
import com.carenest.backend.features.community.entity.UserGroupMembership;
import com.carenest.backend.features.community.enums.GroupCreationAuditAction;
import com.carenest.backend.features.community.enums.GroupCreationRequestStatus;
import com.carenest.backend.features.community.enums.GroupGovernanceAuditAction;
import com.carenest.backend.features.community.enums.GroupRole;
import com.carenest.backend.features.community.enums.PostStatus;
import com.carenest.backend.features.community.enums.ReportStatus;
import com.carenest.backend.features.community.repository.ArticleCommentRepository;
import com.carenest.backend.features.community.repository.ArticleLikeRepository;
import com.carenest.backend.features.community.repository.ArticleRepository;
import com.carenest.backend.features.community.repository.ChatGroupRepository;
import com.carenest.backend.features.community.repository.GroupCreationAuditLogRepository;
import com.carenest.backend.features.community.repository.GroupCreationRequestRepository;
import com.carenest.backend.features.community.repository.GroupGovernanceAuditLogRepository;
import com.carenest.backend.features.community.repository.GroupPostRepository;
import com.carenest.backend.features.community.repository.GroupPostLikeRepository;
import com.carenest.backend.features.community.repository.GroupPostCommentRepository;
import com.carenest.backend.features.community.repository.ReportTicketRepository;
import com.carenest.backend.features.community.repository.UserGroupMembershipRepository;
import com.carenest.backend.features.community.service.CommunityKnowledgeService;
import com.carenest.backend.features.doctorverification.entity.DoctorVerification;
import com.carenest.backend.features.doctorverification.enums.VerificationStatus;
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
    private final GroupCreationRequestRepository groupCreationRequestRepository;
    private final GroupCreationAuditLogRepository groupCreationAuditLogRepository;
    private final GroupGovernanceAuditLogRepository groupGovernanceAuditLogRepository;
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
        User currentUser = getCurrentUser();
        String keyword = normalizeOptionalText(search);
        String normalizedCategory = normalizeOptionalText(category);
        return chatGroupRepository.searchGroups(keyword, normalizedCategory)
                .stream()
                .filter(group -> canDiscoverGroup(group, currentUser))
                .map(group -> toGroupResponse(group, currentUser.getId()))
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
                .filter(group -> !group.isPrivate())
                .filter(group -> !membershipRepository.existsByGroupIdAndUserId(group.getId(), currentUser.getId()))
                .map(group -> toGroupResponse(group, currentUser.getId()))
                .toList();
    }

    @Override
    @Transactional
    public GroupCreationRequestResponse createGroupRequest(CreateGroupCreationRequest request) {
        User currentUser = getCurrentUser();
        ensureEligibleDoctorCanCreateGroup(currentUser);

        if (groupCreationRequestRepository.existsByRequesterIdAndStatus(
                currentUser.getId(),
                GroupCreationRequestStatus.PENDING
        )) {
            throw new BadRequestException("Bạn đã có một yêu cầu tạo nhóm đang chờ duyệt");
        }

        String normalizedName = requireNormalizedText(request.getName(), "Tên nhóm không được để trống");
        if (chatGroupRepository.existsByNameIgnoreCase(normalizedName)
                || groupCreationRequestRepository.existsByNameIgnoreCaseAndStatus(normalizedName, GroupCreationRequestStatus.PENDING)) {
            throw new BadRequestException("Tên nhóm đã tồn tại hoặc đang chờ duyệt");
        }

        if (isDoctorClinicType(request.getGroupType())
                && chatGroupRepository.findByLeadDoctorIdAndIsPrivateTrue(currentUser.getId()).isPresent()) {
            throw new BadRequestException("Bác sĩ này đã có một phòng khám số đang hoạt động");
        }

        GroupCreationRequest groupRequest = groupCreationRequestRepository.save(GroupCreationRequest.builder()
                .requester(currentUser)
                .groupType(normalizeGroupType(request.getGroupType()))
                .name(normalizedName)
                .shortDescription(requireNormalizedText(request.getShortDescription(), "Mô tả ngắn không được để trống"))
                .detailedPurpose(requireNormalizedText(request.getDetailedPurpose(), "Mục đích chi tiết không được để trống"))
                .category(requireNormalizedText(request.getCategory(), "Chuyên khoa không được để trống"))
                .coverImageUrl(normalizeOptionalText(request.getCoverImageUrl()))
                .moderationIntent(normalizeOptionalText(request.getModerationIntent()))
                .communityRules(normalizeOptionalText(request.getCommunityRules()))
                .status(GroupCreationRequestStatus.PENDING)
                .build());

        saveGroupCreationAudit(groupRequest, currentUser, GroupCreationAuditAction.SUBMITTED, null);
        return toGroupCreationRequestResponse(groupRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GroupCreationRequestResponse> getMyGroupRequests() {
        User currentUser = getCurrentUser();
        ensureDoctorAccess(currentUser);
        return groupCreationRequestRepository.findAllByRequesterIdOrderByCreatedAtDesc(currentUser.getId())
                .stream()
                .map(this::toGroupCreationRequestResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<GroupCreationRequestResponse> getAdminGroupRequests() {
        ensureAdminAccess(getCurrentUser());
        return groupCreationRequestRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toGroupCreationRequestResponse)
                .toList();
    }

    @Override
    @Transactional
    public void approveGroupRequest(Long requestId) {
        User currentUser = getCurrentUser();
        ensureAdminAccess(currentUser);

        GroupCreationRequest groupRequest = groupCreationRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("GroupCreationRequest", requestId));
        ensurePendingGroupRequest(groupRequest);

        if (chatGroupRepository.existsByNameIgnoreCase(groupRequest.getName())) {
            throw new BadRequestException("Tên nhóm đã tồn tại trong hệ thống");
        }
        if (isDoctorClinicType(groupRequest.getGroupType())
                && chatGroupRepository.findByLeadDoctorIdAndIsPrivateTrue(groupRequest.getRequester().getId()).isPresent()) {
            throw new BadRequestException("Bác sĩ yêu cầu đã có một phòng khám số đang hoạt động");
        }

        ChatGroup chatGroup = chatGroupRepository.save(ChatGroup.builder()
                .name(groupRequest.getName())
                .description(groupRequest.getShortDescription())
                .category(groupRequest.getCategory())
                .tags(groupRequest.getModerationIntent())
                .isPrivate(isDoctorClinicType(groupRequest.getGroupType()))
                .leadDoctor(groupRequest.getRequester())
                .build());

        membershipRepository.findByGroupIdAndUserId(chatGroup.getId(), groupRequest.getRequester().getId())
                .orElseGet(() -> membershipRepository.save(UserGroupMembership.builder()
                        .group(chatGroup)
                        .user(groupRequest.getRequester())
                        .groupRole(GroupRole.HOST)
                        .build()));

        groupRequest.setStatus(GroupCreationRequestStatus.APPROVED);
        groupRequest.setReviewer(currentUser);
        groupRequest.setReviewedAt(Instant.now());
        groupRequest.setRejectionReason(null);
        groupCreationRequestRepository.save(groupRequest);
        saveGroupCreationAudit(groupRequest, currentUser, GroupCreationAuditAction.APPROVED, null);
    }

    @Override
    @Transactional
    public void rejectGroupRequest(Long requestId, String reason) {
        User currentUser = getCurrentUser();
        ensureAdminAccess(currentUser);

        String normalizedReason = requireNormalizedText(reason, "Lý do từ chối không được để trống");
        GroupCreationRequest groupRequest = groupCreationRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("GroupCreationRequest", requestId));
        ensurePendingGroupRequest(groupRequest);

        groupRequest.setStatus(GroupCreationRequestStatus.REJECTED);
        groupRequest.setReviewer(currentUser);
        groupRequest.setReviewedAt(Instant.now());
        groupRequest.setRejectionReason(normalizedReason);
        groupCreationRequestRepository.save(groupRequest);
        saveGroupCreationAudit(groupRequest, currentUser, GroupCreationAuditAction.REJECTED, normalizedReason);
    }

    @Override
    @Transactional(readOnly = true)
    public ChatGroupPreviewResponse getChatGroupPreview(Long groupId) {
        User currentUser = getCurrentUser();
        ChatGroup group = getChatGroupOrThrow(groupId);
        ensureCanPreviewGroup(group, currentUser);
        return toGroupPreviewResponse(group, currentUser);
    }

    @Override
    @Transactional
    public ChatGroupPreviewResponse joinChatGroup(Long groupId) {
        User currentUser = getCurrentUser();
        ChatGroup group = getChatGroupOrThrow(groupId);
        ensureCanJoinGroup(group, currentUser);
        ensureGroupWritable(group, "tham gia");

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
        ensureGroupWritable(chatGroup, "dang bai");
        enforceSlowMode(groupId, currentUser);

        String trimmedTitle = requireTrimmedTitle(request);
        String trimmedContent = requireTrimmedContent(request);
        GroupPost replyToPost = resolveReplyToPost(chatGroup.getId(), request.getReplyToPostId());

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
    public GroupPostResponse updateGroupPost(Long postId, CreateGroupPostRequest request) {
        User currentUser = getCurrentUser();
        GroupPost post = groupPostRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("GroupPost", postId));

        ensureAuthorOwnsPost(post, currentUser);
        ensureCanEnterGroup(post.getChatGroup().getId(), currentUser);
        ensureGroupWritable(post.getChatGroup(), "cap nhat bai viet");

        post.setTitle(requireTrimmedTitle(request));
        post.setContent(requireTrimmedContent(request));
        post.setTags(normalizeOptionalText(request.getTags()));
        post.setImageUrl(normalizeOptionalText(request.getImageUrl()));
        post.setReplyToPost(resolveReplyToPost(post.getChatGroup().getId(), request.getReplyToPostId()));
        post.setStatus(PostStatus.PENDING_APPROVAL);
        post.setRejectionReason(null);
        post.setReviewer(null);

        return toPostResponse(groupPostRepository.save(post));
    }

    @Override
    @Transactional
    public void deleteGroupPost(Long postId) {
        User currentUser = getCurrentUser();
        GroupPost post = groupPostRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("GroupPost", postId));

        ensureCanDeletePost(post, currentUser);
        cleanupPostDependencies(postId, currentUser);
        groupPostRepository.delete(post);
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
    @Transactional(readOnly = true)
    public List<GroupMemberResponse> getGroupMembers(Long groupId) {
        User currentUser = getCurrentUser();
        getChatGroupOrThrow(groupId);
        ensureCanEnterGroup(groupId, currentUser);

        return membershipRepository.findAllByGroupIdOrderByGroupRoleDescJoinedAtAsc(groupId)
                .stream()
                .map(this::toGroupMemberResponse)
                .toList();
    }

    @Override
    @Transactional
    public GroupMemberResponse updateGroupMemberRole(Long groupId, Long targetUserId, UpdateGroupMemberRoleRequest request) {
        User currentUser = getCurrentUser();
        ChatGroup group = getChatGroupOrThrow(groupId);
        ensureCanManageGroupMembers(group, currentUser);

        UserGroupMembership targetMembership = membershipRepository.findByGroupIdAndUserId(groupId, targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("UserGroupMembership", "targetUserId", targetUserId.toString()));
        GroupRole previousRole = targetMembership.getGroupRole();
        GroupRole targetRole = parseGroupRole(request.getRole());
        boolean isSystemAdmin = currentUser.getRole() == Role.ADMIN;

        if (currentUser.getId().equals(targetUserId) && targetMembership.getGroupRole() == GroupRole.HOST && targetRole != GroupRole.HOST) {
            throw new BadRequestException("Host không thể tự hạ quyền của chính mình");
        }
        if ((targetMembership.getGroupRole() == GroupRole.HOST || targetRole == GroupRole.HOST) && !isSystemAdmin) {
            throw new AccessDeniedException("Chỉ Admin hệ thống mới có quyền thay đổi vai trò Host");
        }

        if (previousRole == GroupRole.HOST && targetRole != GroupRole.HOST
                && membershipRepository.countByGroupIdAndGroupRole(groupId, GroupRole.HOST) <= 1) {
            throw new BadRequestException("Nhom phai luon co it nhat mot host");
        }

        targetMembership.setGroupRole(targetRole);
        UserGroupMembership savedMembership = membershipRepository.save(targetMembership);
        if (isSystemAdmin && previousRole != targetRole) {
            saveGovernanceAudit(group, currentUser, targetMembership.getUser(), GroupGovernanceAuditAction.ADMIN_ROLE_OVERRIDE, previousRole, targetRole, null);
        }
        return toGroupMemberResponse(savedMembership);
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

        if (targetMembership.getGroupRole() == GroupRole.HOST
                && membershipRepository.countByGroupIdAndGroupRole(groupId, GroupRole.HOST) <= 1) {
            throw new BadRequestException("Nhom phai luon co it nhat mot host");
        }

        membershipRepository.delete(targetMembership);
        if (isSystemAdmin) {
            saveGovernanceAudit(group, currentUser, targetMembership.getUser(), GroupGovernanceAuditAction.ADMIN_MEMBER_REMOVED, targetMembership.getGroupRole(), null, null);
        }
    }

    @Override
    @Transactional
    public void freezeGroup(Long groupId) {
        User currentUser = getCurrentUser();
        ensureAdminAccess(currentUser);
        ChatGroup group = getChatGroupOrThrow(groupId);
        if (!group.isFrozen()) {
            group.setFrozen(true);
            chatGroupRepository.save(group);
            saveGovernanceAudit(group, currentUser, null, GroupGovernanceAuditAction.GROUP_FROZEN, null, null, null);
        }
    }

    @Override
    @Transactional
    public void unfreezeGroup(Long groupId) {
        User currentUser = getCurrentUser();
        ensureAdminAccess(currentUser);
        ChatGroup group = getChatGroupOrThrow(groupId);
        if (group.isFrozen()) {
            group.setFrozen(false);
            chatGroupRepository.save(group);
            saveGovernanceAudit(group, currentUser, null, GroupGovernanceAuditAction.GROUP_UNFROZEN, null, null, null);
        }
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
        ensureGroupWritable(post.getChatGroup(), "binh luan");

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
            if (secondsSinceLastPost < USER_SLOW_MODE_SECONDS) {
                long remaining = USER_SLOW_MODE_SECONDS - secondsSinceLastPost;
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

    private boolean canDiscoverGroup(ChatGroup group, User user) {
        if (!group.isPrivate()) {
            return true;
        }
        return user.getRole() == Role.ADMIN
                || membershipRepository.existsByGroupIdAndUserId(group.getId(), user.getId());
    }

    private void ensureCanPreviewGroup(ChatGroup group, User user) {
        if (!canDiscoverGroup(group, user)) {
            throw new AccessDeniedException("Bạn không có quyền xem nhóm riêng tư này");
        }
    }

    private void ensureCanJoinGroup(ChatGroup group, User user) {
        if (!group.isPrivate() || user.getRole() == Role.ADMIN || isLeadDoctor(group, user)) {
            return;
        }
        throw new AccessDeniedException("Nhóm riêng tư chỉ dành cho thành viên được mời");
    }

    private void ensureGroupWritable(ChatGroup group, String actionLabel) {
        if (group.isFrozen()) {
            throw new BadRequestException("Nhom dang tam khoa, khong the " + actionLabel);
        }
    }

    private void ensureAuthorOwnsPost(GroupPost post, User user) {
        if (post.getAuthor() == null || !post.getAuthor().getId().equals(user.getId())) {
            throw new AccessDeniedException("Bạn chỉ có thể chỉnh sửa hoặc xóa bài viết của mình");
        }
    }

    private void ensureCanDeletePost(GroupPost post, User user) {
        if (post.getAuthor() != null && post.getAuthor().getId().equals(user.getId())) {
            ensureCanEnterGroup(post.getChatGroup().getId(), user);
            return;
        }
        ensureCanModerate(post.getChatGroup(), user);
    }

    private String requireTrimmedTitle(CreateGroupPostRequest request) {
        String title = normalizeOptionalText(request.getTitle());
        if (title == null) {
            throw new BadRequestException("Tiêu đề không được để trống");
        }
        return title;
    }

    private String requireTrimmedContent(CreateGroupPostRequest request) {
        String content = normalizeOptionalText(request.getContent());
        if (content == null) {
            throw new BadRequestException("Nội dung không được để trống");
        }
        return content;
    }

    private GroupPost resolveReplyToPost(Long groupId, Long replyToPostId) {
        if (replyToPostId == null) {
            return null;
        }
        GroupPost replyToPost = groupPostRepository.findById(replyToPostId)
                .orElseThrow(() -> new ResourceNotFoundException("GroupPost", replyToPostId));
        if (replyToPost.getChatGroup() == null || !replyToPost.getChatGroup().getId().equals(groupId)) {
            throw new BadRequestException("Bài viết trả lời không thuộc nhóm này");
        }
        if (replyToPost.getStatus() != PostStatus.APPROVED) {
            throw new BadRequestException("Chỉ có thể trả lời bài viết đã được duyệt");
        }
        return replyToPost;
    }

    private void cleanupPostDependencies(Long postId, User resolvedBy) {
        reportTicketRepository.findAllByReportedPostId(postId).forEach(report -> {
            report.setStatus(ReportStatus.RESOLVED);
            report.setResolvedBy(resolvedBy);
            report.setResolvedAt(Instant.now());
            report.setReportedPost(null);
            reportTicketRepository.save(report);
        });
        groupPostRepository.clearRepliesByPostId(postId);
        groupPostLikeRepository.deleteAllByGroupPostId(postId);
        groupPostCommentRepository.deleteAllByGroupPostId(postId);
    }

    private void ensureCanModerate(ChatGroup group, User currentUser) {
        if (currentUser.getRole() == Role.ADMIN) {
            return;
        }
        UserGroupMembership membership = membershipRepository.findByGroupIdAndUserId(group.getId(), currentUser.getId())
                .orElse(null);
        boolean canModerate = membership != null
                && (membership.getGroupRole() == GroupRole.HOST || membership.getGroupRole() == GroupRole.MODERATOR);
        if (!canModerate) {
            throw new AccessDeniedException("Chỉ Quản trị viên, Host và Moderator mới có quyền duyệt bài viết");
        }
    }

    private void ensureCanManageGroupMembers(ChatGroup group, User currentUser) {
        if (currentUser.getRole() == Role.ADMIN) {
            return;
        }
        boolean isHost = membershipRepository.existsByGroupIdAndUserIdAndGroupRole(
                group.getId(),
                currentUser.getId(),
                GroupRole.HOST
        );
        if (!isHost) {
            throw new AccessDeniedException("Chỉ Admin hệ thống và Host mới có quyền quản lý thành viên nhóm");
        }
    }

    private GroupRole parseGroupRole(String rawRole) {
        String normalized = rawRole == null ? "" : rawRole.trim().toUpperCase();
        try {
            return GroupRole.valueOf(normalized);
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Vai trò nhóm không hợp lệ");
        }
    }

    private GroupMemberResponse toGroupMemberResponse(UserGroupMembership membership) {
        User user = membership.getUser();
        return GroupMemberResponse.builder()
                .userId(user != null ? user.getId() : null)
                .fullName(user != null ? user.getFullName() : null)
                .email(user != null ? user.getEmail() : null)
                .avatarUrl(user != null ? user.getAvatarUrl() : null)
                .role(membership.getGroupRole())
                .active(user != null && Boolean.TRUE.equals(user.getIsActive()))
                .joinedAt(membership.getJoinedAt())
                .build();
    }

    private void ensureEligibleDoctorCanCreateGroup(User currentUser) {
        if (currentUser.getRole() != Role.DOCTOR) {
            throw new AccessDeniedException("Chỉ bác sĩ đã xác thực mới được gửi yêu cầu tạo nhóm");
        }
        if (!Boolean.TRUE.equals(currentUser.getIsActive())) {
            throw new BadRequestException("Tài khoản bác sĩ đang bị khóa");
        }
        if (!doctorVerificationRepository.existsByUserIdAndStatus(currentUser.getId(), VerificationStatus.APPROVED)) {
            throw new AccessDeniedException("Bác sĩ chưa được phê duyệt hồ sơ xác thực");
        }
    }

    private void ensureDoctorAccess(User currentUser) {
        if (currentUser.getRole() != Role.DOCTOR) {
            throw new AccessDeniedException("Chi bac si moi co the truy cap tinh nang nay");
        }
        if (!Boolean.TRUE.equals(currentUser.getIsActive())) {
            throw new BadRequestException("Tai khoan bac si da bi khoa");
        }
    }

    private void ensureAdminAccess(User currentUser) {
        if (currentUser.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Chỉ quản trị viên hệ thống mới có quyền xử lý yêu cầu nhóm");
        }
    }

    private void saveGovernanceAudit(
            ChatGroup group,
            User actor,
            User targetUser,
            GroupGovernanceAuditAction action,
            GroupRole previousRole,
            GroupRole newRole,
            String note
    ) {
        groupGovernanceAuditLogRepository.save(GroupGovernanceAuditLog.builder()
                .group(group)
                .actor(actor)
                .targetUser(targetUser)
                .action(action)
                .previousRole(previousRole)
                .newRole(newRole)
                .note(note)
                .build());
    }

    private void ensurePendingGroupRequest(GroupCreationRequest groupRequest) {
        if (groupRequest.getStatus() != GroupCreationRequestStatus.PENDING) {
            throw new BadRequestException("Yêu cầu tạo nhóm này đã được xử lý");
        }
    }

    private void saveGroupCreationAudit(
            GroupCreationRequest groupRequest,
            User actor,
            GroupCreationAuditAction action,
            String note
    ) {
        groupCreationAuditLogRepository.save(GroupCreationAuditLog.builder()
                .groupCreationRequest(groupRequest)
                .actor(actor)
                .action(action)
                .note(normalizeOptionalText(note))
                .build());
    }

    private String normalizeGroupType(String rawType) {
        String normalized = requireNormalizedText(rawType, "Loại nhóm không được để trống").toUpperCase();
        if (!normalized.equals("SPECIALTY_PUBLIC") && !normalized.equals("DOCTOR_CLINIC")) {
            throw new BadRequestException("Loại nhóm không hợp lệ");
        }
        return normalized;
    }

    private boolean isDoctorClinicType(String groupType) {
        return "DOCTOR_CLINIC".equals(normalizeGroupType(groupType));
    }

    private String requireNormalizedText(String value, String message) {
        String normalized = normalizeOptionalText(value);
        if (normalized == null) {
            throw new BadRequestException(message);
        }
        return normalized;
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
                .isFrozen(group.isFrozen())
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
                .isFrozen(group.isFrozen())
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

    private GroupCreationRequestResponse toGroupCreationRequestResponse(GroupCreationRequest groupRequest) {
        return GroupCreationRequestResponse.builder()
                .id(groupRequest.getId())
                .requesterId(groupRequest.getRequester() != null ? groupRequest.getRequester().getId() : null)
                .groupType(groupRequest.getGroupType())
                .name(groupRequest.getName())
                .shortDescription(groupRequest.getShortDescription())
                .detailedPurpose(groupRequest.getDetailedPurpose())
                .category(groupRequest.getCategory())
                .coverImageUrl(groupRequest.getCoverImageUrl())
                .status(groupRequest.getStatus() != null ? groupRequest.getStatus().name() : null)
                .rejectionReason(groupRequest.getRejectionReason())
                .reviewerId(groupRequest.getReviewer() != null ? groupRequest.getReviewer().getId() : null)
                .reviewedAt(groupRequest.getReviewedAt())
                .createdAt(groupRequest.getCreatedAt())
                .build();
    }

}
