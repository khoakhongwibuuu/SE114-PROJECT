package com.carenest.backend.features.family.service.impl;

import com.carenest.backend.core.exception.BadRequestException;
import com.carenest.backend.features.auth.entity.User;
import com.carenest.backend.features.auth.repository.UserRepository;
import com.carenest.backend.features.family.dto.request.UpdateRoleRequest;
import com.carenest.backend.features.family.entity.Family;
import com.carenest.backend.features.family.entity.FamilyMember;
import com.carenest.backend.features.family.enums.FamilyRole;
import com.carenest.backend.features.family.mapper.FamilyMapper;
import com.carenest.backend.features.family.repository.FamilyInvitationRepository;
import com.carenest.backend.features.family.repository.FamilyMemberRepository;
import com.carenest.backend.features.family.repository.FamilyRepository;
import com.carenest.backend.features.healthprofile.repository.HealthProfileRepository;
import com.carenest.backend.features.notification.service.NotificationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FamilyServiceRoleContractTest {

    private static final long FAMILY_ID = 10L;
    private static final long TARGET_MEMBER_ID = 99L;
    private static final String REQUESTER_EMAIL = "owner@example.com";

    @Mock
    private FamilyRepository familyRepository;
    @Mock
    private FamilyMemberRepository familyMemberRepository;
    @Mock
    private FamilyInvitationRepository familyInvitationRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private FamilyMapper familyMapper;
    @Mock
    private HealthProfileRepository healthProfileRepository;
    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private FamilyServiceImpl familyService;

    private User requester;
    private User targetUser;
    private Family family;

    @BeforeEach
    void setUp() {
        requester = user(1L, REQUESTER_EMAIL);
        targetUser = user(2L, "member@example.com");
        family = Family.builder()
                .name("Nguyen family")
                .owner(requester)
                .build();
        family.setId(FAMILY_ID);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(REQUESTER_EMAIL, "n/a"));
        when(userRepository.findByEmail(REQUESTER_EMAIL)).thenReturn(Optional.of(requester));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void updateMemberRole_rejectsMemberRequesterBeforeMutation() {
        FamilyMember requesterMember = member(1L, requester, FamilyRole.MEMBER);
        when(familyMemberRepository.findByFamilyIdAndUserId(FAMILY_ID, requester.getId()))
                .thenReturn(Optional.of(requesterMember));

        UpdateRoleRequest request = UpdateRoleRequest.builder()
                .role(FamilyRole.ADMIN)
                .build();

        assertThrows(
                AccessDeniedException.class,
                () -> familyService.updateMemberRole(FAMILY_ID, TARGET_MEMBER_ID, request));

        verify(familyMemberRepository, never()).findById(any());
        verify(familyMemberRepository, never()).save(any());
    }

    @Test
    void updateMemberRole_rejectsAssigningOwnerRole() {
        FamilyMember requesterMember = member(1L, requester, FamilyRole.OWNER);
        FamilyMember targetMember = member(TARGET_MEMBER_ID, targetUser, FamilyRole.MEMBER);
        when(familyMemberRepository.findByFamilyIdAndUserId(FAMILY_ID, requester.getId()))
                .thenReturn(Optional.of(requesterMember));
        when(familyMemberRepository.findById(TARGET_MEMBER_ID)).thenReturn(Optional.of(targetMember));

        UpdateRoleRequest request = UpdateRoleRequest.builder()
                .role(FamilyRole.OWNER)
                .build();

        assertThrows(
                BadRequestException.class,
                () -> familyService.updateMemberRole(FAMILY_ID, TARGET_MEMBER_ID, request));

        verify(familyMemberRepository, never()).save(any());
    }

    @Test
    void updateMemberRole_rejectsChangingExistingOwnerRole() {
        FamilyMember requesterMember = member(1L, requester, FamilyRole.OWNER);
        FamilyMember targetMember = member(TARGET_MEMBER_ID, targetUser, FamilyRole.OWNER);
        when(familyMemberRepository.findByFamilyIdAndUserId(FAMILY_ID, requester.getId()))
                .thenReturn(Optional.of(requesterMember));
        when(familyMemberRepository.findById(TARGET_MEMBER_ID)).thenReturn(Optional.of(targetMember));

        UpdateRoleRequest request = UpdateRoleRequest.builder()
                .role(FamilyRole.ADMIN)
                .build();

        assertThrows(
                BadRequestException.class,
                () -> familyService.updateMemberRole(FAMILY_ID, TARGET_MEMBER_ID, request));

        verify(familyMemberRepository, never()).save(any());
    }

    @Test
    void getJoinCode_requiresExplicitActiveFamilyWhenUserBelongsToMultipleFamilies() {
        FamilyMember firstMembership = member(1L, requester, FamilyRole.OWNER);
        Family secondFamily = Family.builder().name("Pham family").owner(requester).build();
        secondFamily.setId(11L);
        FamilyMember secondMembership = FamilyMember.builder()
                .family(secondFamily)
                .user(requester)
                .role(FamilyRole.ADMIN)
                .build();
        secondMembership.setId(2L);

        when(familyMemberRepository.findAllByUserId(requester.getId()))
                .thenReturn(java.util.List.of(firstMembership, secondMembership));

        assertThrows(BadRequestException.class, () -> familyService.getJoinCode());
    }

    @Test
    void joinByCode_rejectsBlankJoinCodeBeforeRepositoryLookup() {
        assertThrows(BadRequestException.class, () -> familyService.joinByCode(
                com.carenest.backend.features.family.dto.request.JoinFamilyByCodeRequest.builder()
                        .joinCode("   ")
                        .build()
        ));

        verify(familyRepository, never()).findByJoinCode(anyString());
    }

    private User user(Long id, String email) {
        User user = User.builder()
                .email(email)
                .passwordHash("hash")
                .fullName("Test User")
                .build();
        user.setId(id);
        return user;
    }

    private FamilyMember member(Long id, User user, FamilyRole role) {
        FamilyMember member = FamilyMember.builder()
                .family(family)
                .user(user)
                .role(role)
                .build();
        member.setId(id);
        return member;
    }
}
