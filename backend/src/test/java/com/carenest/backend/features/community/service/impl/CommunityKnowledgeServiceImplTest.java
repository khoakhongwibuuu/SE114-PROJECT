package com.carenest.backend.features.community.service.impl;

import com.carenest.backend.features.auth.entity.User;
import com.carenest.backend.features.auth.enums.Role;
import com.carenest.backend.features.auth.repository.UserRepository;
import com.carenest.backend.features.community.entity.ChatGroup;
import com.carenest.backend.features.community.entity.GroupCreationRequest;
import com.carenest.backend.features.community.entity.GroupPost;
import com.carenest.backend.features.community.entity.UserGroupMembership;
import com.carenest.backend.features.community.enums.GroupCreationRequestStatus;
import com.carenest.backend.features.community.enums.GroupRole;
import com.carenest.backend.features.community.repository.ArticleCommentRepository;
import com.carenest.backend.features.community.repository.ArticleLikeRepository;
import com.carenest.backend.features.community.repository.ArticleRepository;
import com.carenest.backend.features.community.repository.ChatGroupRepository;
import com.carenest.backend.features.community.repository.GroupCreationAuditLogRepository;
import com.carenest.backend.features.community.repository.GroupCreationRequestRepository;
import com.carenest.backend.features.community.repository.GroupGovernanceAuditLogRepository;
import com.carenest.backend.features.community.repository.GroupPostCommentRepository;
import com.carenest.backend.features.community.repository.GroupPostLikeRepository;
import com.carenest.backend.features.community.repository.GroupPostRepository;
import com.carenest.backend.features.community.repository.ReportTicketRepository;
import com.carenest.backend.features.community.repository.UserGroupMembershipRepository;
import com.carenest.backend.features.community.dto.request.CreateGroupCreationRequest;
import com.carenest.backend.features.doctorverification.enums.VerificationStatus;
import com.carenest.backend.features.doctorverification.repository.DoctorVerificationRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommunityKnowledgeServiceImplTest {

    @Mock private ArticleRepository articleRepository;
    @Mock private ArticleLikeRepository articleLikeRepository;
    @Mock private ArticleCommentRepository articleCommentRepository;
    @Mock private ChatGroupRepository chatGroupRepository;
    @Mock private GroupCreationRequestRepository groupCreationRequestRepository;
    @Mock private GroupCreationAuditLogRepository groupCreationAuditLogRepository;
    @Mock private GroupGovernanceAuditLogRepository groupGovernanceAuditLogRepository;
    @Mock private GroupPostRepository groupPostRepository;
    @Mock private GroupPostLikeRepository groupPostLikeRepository;
    @Mock private GroupPostCommentRepository groupPostCommentRepository;
    @Mock private UserGroupMembershipRepository membershipRepository;
    @Mock private ReportTicketRepository reportTicketRepository;
    @Mock private UserRepository userRepository;
    @Mock private DoctorVerificationRepository doctorVerificationRepository;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void deleteGroupPost_allowsModeratorToRemoveApprovedPostFromGroup() {
        CommunityKnowledgeServiceImpl service = service();
        User moderator = user(2L, "moderator@example.com", Role.USER);
        User author = user(3L, "author@example.com", Role.USER);
        ChatGroup group = group(10L);
        GroupPost post = post(99L, group, author);
        SecurityContextHolder.getContext().setAuthentication(
                authenticated(moderator.getEmail())
        );

        when(userRepository.findByEmail(moderator.getEmail())).thenReturn(Optional.of(moderator));
        when(groupPostRepository.findById(99L)).thenReturn(Optional.of(post));
        when(membershipRepository.findByGroupIdAndUserId(10L, 2L))
                .thenReturn(Optional.of(UserGroupMembership.builder()
                        .group(group)
                        .user(moderator)
                        .groupRole(GroupRole.MODERATOR)
                        .build()));
        when(reportTicketRepository.findAllByReportedPostId(99L)).thenReturn(List.of());

        service.deleteGroupPost(99L);

        verify(groupPostRepository).clearRepliesByPostId(99L);
        verify(groupPostLikeRepository).deleteAllByGroupPostId(99L);
        verify(groupPostCommentRepository).deleteAllByGroupPostId(99L);
        verify(groupPostRepository).delete(post);
    }

    @Test
    void deleteGroupPost_rejectsNonAuthorNonModerator() {
        CommunityKnowledgeServiceImpl service = service();
        User member = user(4L, "member@example.com", Role.USER);
        User author = user(3L, "author@example.com", Role.USER);
        ChatGroup group = group(10L);
        GroupPost post = post(99L, group, author);
        SecurityContextHolder.getContext().setAuthentication(
                authenticated(member.getEmail())
        );

        when(userRepository.findByEmail(member.getEmail())).thenReturn(Optional.of(member));
        when(groupPostRepository.findById(99L)).thenReturn(Optional.of(post));
        when(membershipRepository.findByGroupIdAndUserId(10L, 4L))
                .thenReturn(Optional.of(UserGroupMembership.builder()
                        .group(group)
                        .user(member)
                        .groupRole(GroupRole.MEMBER)
                        .build()));

        assertThatThrownBy(() -> service.deleteGroupPost(99L))
                .isInstanceOf(AccessDeniedException.class);
        verify(groupPostRepository, never()).delete(post);
    }

    @Test
    void createGroupRequest_rejectsUnverifiedDoctor() {
        CommunityKnowledgeServiceImpl service = service();
        User doctor = user(6L, "doctor@example.com", Role.DOCTOR);
        SecurityContextHolder.getContext().setAuthentication(authenticated(doctor.getEmail()));

        when(userRepository.findByEmail(doctor.getEmail())).thenReturn(Optional.of(doctor));
        when(doctorVerificationRepository.existsByUserIdAndStatus(6L, VerificationStatus.APPROVED)).thenReturn(false);

        assertThatThrownBy(() -> service.createGroupRequest(groupRequestPayload()))
                .isInstanceOf(AccessDeniedException.class);
        verify(groupCreationRequestRepository, never()).save(any());
    }

    @Test
    void getMyGroupRequests_returnsRequesterHistory() {
        CommunityKnowledgeServiceImpl service = service();
        User doctor = user(6L, "doctor@example.com", Role.DOCTOR);
        GroupCreationRequest request = GroupCreationRequest.builder()
                .requester(doctor)
                .groupType("SPECIALTY_PUBLIC")
                .name("Cong dong Nhi")
                .shortDescription("Mo ta")
                .detailedPurpose("Muc dich")
                .category("Nhi khoa")
                .status(GroupCreationRequestStatus.PENDING)
                .build();
        request.setId(15L);

        SecurityContextHolder.getContext().setAuthentication(authenticated(doctor.getEmail()));
        when(userRepository.findByEmail(doctor.getEmail())).thenReturn(Optional.of(doctor));
        when(groupCreationRequestRepository.findAllByRequesterIdOrderByCreatedAtDesc(6L)).thenReturn(List.of(request));

        assertThat(service.getMyGroupRequests())
                .hasSize(1)
                .first()
                .extracting("id", "name", "status")
                .containsExactly(15L, "Cong dong Nhi", GroupCreationRequestStatus.PENDING.name());
    }

    @Test
    void approveGroupRequest_createsActiveGroupAndHostMembership() {
        CommunityKnowledgeServiceImpl service = service();
        User admin = user(1L, "admin@example.com", Role.ADMIN);
        User doctor = user(6L, "doctor@example.com", Role.DOCTOR);
        GroupCreationRequest request = GroupCreationRequest.builder()
                .requester(doctor)
                .groupType("DOCTOR_CLINIC")
                .name("Phong kham Nhi")
                .shortDescription("Tu van Nhi khoa")
                .detailedPurpose("Dong hanh cha me")
                .category("Nhi khoa")
                .status(GroupCreationRequestStatus.PENDING)
                .build();
        request.setId(11L);
        ChatGroup savedGroup = ChatGroup.builder()
                .name("Phong kham Nhi")
                .isPrivate(true)
                .leadDoctor(doctor)
                .build();
        savedGroup.setId(100L);

        SecurityContextHolder.getContext().setAuthentication(authenticated(admin.getEmail()));
        when(userRepository.findByEmail(admin.getEmail())).thenReturn(Optional.of(admin));
        when(groupCreationRequestRepository.findById(11L)).thenReturn(Optional.of(request));
        when(chatGroupRepository.existsByNameIgnoreCase("Phong kham Nhi")).thenReturn(false);
        when(chatGroupRepository.findByLeadDoctorIdAndIsPrivateTrue(6L)).thenReturn(Optional.empty());
        when(chatGroupRepository.save(any(ChatGroup.class))).thenReturn(savedGroup);
        when(membershipRepository.findByGroupIdAndUserId(100L, 6L)).thenReturn(Optional.empty());
        when(groupCreationRequestRepository.save(any(GroupCreationRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.approveGroupRequest(11L);

        assertThat(request.getStatus()).isEqualTo(GroupCreationRequestStatus.APPROVED);
        assertThat(request.getReviewer()).isEqualTo(admin);
        verify(chatGroupRepository).save(any(ChatGroup.class));
        verify(membershipRepository).save(any(UserGroupMembership.class));
        verify(groupCreationAuditLogRepository).save(any());
    }

    @Test
    void host_canPromoteMemberToModerator() {
        CommunityKnowledgeServiceImpl service = service();
        User host = user(2L, "host@example.com", Role.USER);
        User member = user(3L, "member@example.com", Role.USER);
        ChatGroup group = group(10L);
        UserGroupMembership membership = UserGroupMembership.builder()
                .group(group)
                .user(member)
                .groupRole(GroupRole.MEMBER)
                .build();

        SecurityContextHolder.getContext().setAuthentication(authenticated(host.getEmail()));
        when(userRepository.findByEmail(host.getEmail())).thenReturn(Optional.of(host));
        when(chatGroupRepository.findById(10L)).thenReturn(Optional.of(group));
        when(membershipRepository.existsByGroupIdAndUserIdAndGroupRole(10L, 2L, GroupRole.HOST)).thenReturn(true);
        when(membershipRepository.findByGroupIdAndUserId(10L, 3L)).thenReturn(Optional.of(membership));
        when(membershipRepository.save(any(UserGroupMembership.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var request = new com.carenest.backend.features.community.dto.request.UpdateGroupMemberRoleRequest();
        request.setRole("MODERATOR");
        request.setReason("Need moderation coverage");

        assertThat(service.updateGroupMemberRole(10L, 3L, request).getRole()).isEqualTo(GroupRole.MODERATOR);
        verify(groupGovernanceAuditLogRepository).save(any());
    }

    @Test
    void admin_cannotDemoteLastHost() {
        CommunityKnowledgeServiceImpl service = service();
        User admin = user(1L, "admin@example.com", Role.ADMIN);
        User host = user(2L, "host@example.com", Role.USER);
        ChatGroup group = group(10L);
        UserGroupMembership membership = UserGroupMembership.builder()
                .group(group)
                .user(host)
                .groupRole(GroupRole.HOST)
                .build();

        SecurityContextHolder.getContext().setAuthentication(authenticated(admin.getEmail()));
        when(userRepository.findByEmail(admin.getEmail())).thenReturn(Optional.of(admin));
        when(chatGroupRepository.findById(10L)).thenReturn(Optional.of(group));
        when(membershipRepository.findByGroupIdAndUserId(10L, 2L)).thenReturn(Optional.of(membership));
        when(membershipRepository.countByGroupIdAndGroupRole(10L, GroupRole.HOST)).thenReturn(1L);

        var request = new com.carenest.backend.features.community.dto.request.UpdateGroupMemberRoleRequest();
        request.setRole("MEMBER");
        request.setReason("Host is no longer active");

        assertThatThrownBy(() -> service.updateGroupMemberRole(10L, 2L, request))
                .isInstanceOf(com.carenest.backend.core.exception.BadRequestException.class);
    }

    @Test
    void freezeGroup_marksGroupFrozenAndStoresAudit() {
        CommunityKnowledgeServiceImpl service = service();
        User admin = user(1L, "admin@example.com", Role.ADMIN);
        ChatGroup group = group(10L);

        SecurityContextHolder.getContext().setAuthentication(authenticated(admin.getEmail()));
        when(userRepository.findByEmail(admin.getEmail())).thenReturn(Optional.of(admin));
        when(chatGroupRepository.findById(10L)).thenReturn(Optional.of(group));
        when(chatGroupRepository.save(any(ChatGroup.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.freezeGroup(10L, "moderation escalation");

        assertThat(group.isFrozen()).isTrue();
        verify(groupGovernanceAuditLogRepository).save(any());
    }

    @Test
    void rejectGroupRequest_requiresPendingRequestAndStoresReason() {
        CommunityKnowledgeServiceImpl service = service();
        User admin = user(1L, "admin@example.com", Role.ADMIN);
        User doctor = user(6L, "doctor@example.com", Role.DOCTOR);
        GroupCreationRequest request = GroupCreationRequest.builder()
                .requester(doctor)
                .groupType("SPECIALTY_PUBLIC")
                .name("Cong dong Nhi")
                .shortDescription("Mo ta")
                .detailedPurpose("Muc dich")
                .category("Nhi khoa")
                .status(GroupCreationRequestStatus.PENDING)
                .build();
        request.setId(12L);

        SecurityContextHolder.getContext().setAuthentication(authenticated(admin.getEmail()));
        when(userRepository.findByEmail(admin.getEmail())).thenReturn(Optional.of(admin));
        when(groupCreationRequestRepository.findById(12L)).thenReturn(Optional.of(request));
        when(groupCreationRequestRepository.save(any(GroupCreationRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.rejectGroupRequest(12L, "Khong phu hop");

        assertThat(request.getStatus()).isEqualTo(GroupCreationRequestStatus.REJECTED);
        assertThat(request.getRejectionReason()).isEqualTo("Khong phu hop");
        assertThat(request.getReviewer()).isEqualTo(admin);
        verify(groupCreationAuditLogRepository).save(any());
    }

    private CommunityKnowledgeServiceImpl service() {
        return new CommunityKnowledgeServiceImpl(
                articleRepository,
                articleLikeRepository,
                articleCommentRepository,
                chatGroupRepository,
                groupCreationRequestRepository,
                groupCreationAuditLogRepository,
                groupGovernanceAuditLogRepository,
                groupPostRepository,
                groupPostLikeRepository,
                groupPostCommentRepository,
                membershipRepository,
                reportTicketRepository,
                userRepository,
                doctorVerificationRepository
        );
    }

    private static CreateGroupCreationRequest groupRequestPayload() {
        CreateGroupCreationRequest request = new CreateGroupCreationRequest();
        request.setName("Cong dong Nhi");
        request.setShortDescription("Mo ta");
        request.setDetailedPurpose("Muc dich");
        request.setCategory("Nhi khoa");
        request.setGroupType("SPECIALTY_PUBLIC");
        return request;
    }

    private static User user(Long id, String email, Role role) {
        User user = User.builder()
                .email(email)
                .passwordHash("hashed")
                .fullName(email)
                .role(role)
                .isActive(true)
                .build();
        user.setId(id);
        return user;
    }

    private static ChatGroup group(Long id) {
        ChatGroup group = ChatGroup.builder()
                .name("Pediatrics")
                .isPrivate(false)
                .build();
        group.setId(id);
        return group;
    }

    private static GroupPost post(Long id, ChatGroup group, User author) {
        GroupPost post = GroupPost.builder()
                .chatGroup(group)
                .author(author)
                .title("Title")
                .content("Content")
                .build();
        post.setId(id);
        return post;
    }

    private static UsernamePasswordAuthenticationToken authenticated(String email) {
        return new UsernamePasswordAuthenticationToken(
                email,
                "n/a",
                AuthorityUtils.NO_AUTHORITIES
        );
    }
}
