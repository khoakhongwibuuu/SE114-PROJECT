package com.carenest.backend.module.family.service.impl;

import com.carenest.backend.common.exception.BadRequestException;
import com.carenest.backend.common.exception.DuplicateResourceException;
import com.carenest.backend.common.exception.ResourceNotFoundException;
import com.carenest.backend.common.exception.UnauthorizedException;
import com.carenest.backend.module.auth.entity.User;
import com.carenest.backend.module.auth.repository.UserRepository;
import com.carenest.backend.module.family.dto.request.CreateFamilyRequest;
import com.carenest.backend.module.family.dto.request.InviteMemberRequest;
import com.carenest.backend.module.family.dto.request.JoinFamilyByCodeRequest;
import com.carenest.backend.module.family.dto.request.UpdateInvitationRequest;
import com.carenest.backend.module.family.dto.request.UpdateRoleRequest;
import com.carenest.backend.module.family.dto.response.FamilyDetailResponse;
import com.carenest.backend.module.family.dto.response.FamilyInvitationResponse;
import com.carenest.backend.module.family.dto.response.FamilyJoinCodeResponse;
import com.carenest.backend.module.family.dto.response.FamilyMemberResponse;
import com.carenest.backend.module.family.dto.response.FamilyResponse;
import com.carenest.backend.module.family.dto.response.FamilySummaryResponse;
import com.carenest.backend.module.family.context.FamilyRequestContext;
import com.carenest.backend.module.family.entity.Family;
import com.carenest.backend.module.family.entity.FamilyInvitation;
import com.carenest.backend.module.family.entity.FamilyMember;
import com.carenest.backend.module.family.enums.FamilyRole;
import com.carenest.backend.module.family.enums.InvitationStatus;
import com.carenest.backend.module.family.mapper.FamilyMapper;
import com.carenest.backend.module.family.repository.FamilyInvitationRepository;
import com.carenest.backend.module.family.repository.FamilyMemberRepository;
import com.carenest.backend.module.family.repository.FamilyRepository;
import com.carenest.backend.module.family.service.FamilyService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FamilyServiceImpl implements FamilyService {

    private static final String JOIN_CODE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int JOIN_CODE_LENGTH = 6;
    private static final int JOIN_CODE_TTL_DAYS = 7;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final FamilyRepository familyRepository;
    private final FamilyMemberRepository familyMemberRepository;
    private final FamilyInvitationRepository familyInvitationRepository;
    private final UserRepository userRepository;
    private final FamilyMapper familyMapper;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("Please sign in"));
    }

    @Override
    @Transactional(readOnly = true)
    public FamilyDetailResponse getMyFamily() {
        User currentUser = getCurrentUser();
        FamilyMember member = getCurrentFamilyMember(currentUser);

        return getFamilyById(member.getFamily().getId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<FamilySummaryResponse> getMyFamilies() {
        User currentUser = getCurrentUser();
        List<FamilyMember> memberships = familyMemberRepository.findAllByUserIdWithFamily(currentUser.getId());

        return memberships.stream().map(fm -> {
            Family family = fm.getFamily();
            int memberCount = familyMemberRepository.findAllByFamilyId(family.getId()).size();
            return FamilySummaryResponse.builder()
                    .id(family.getId())
                    .name(family.getName())
                    .memberCount(memberCount)
                    .myRole(fm.getRole())
                    .ownerName(family.getOwner().getFullName())
                    .build();
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public FamilyResponse createFamily(CreateFamilyRequest request) {
        User currentUser = getCurrentUser();
        // Multi-family support: users may create multiple families without restriction

        Family family = Family.builder()
                .name(request.getName())
                .owner(currentUser)
                .build();

        ensureJoinCode(family);
        family = familyRepository.save(family);

        FamilyMember member = FamilyMember.builder()
                .family(family)
                .user(currentUser)
                .role(FamilyRole.OWNER)
                .build();

        familyMemberRepository.save(Objects.requireNonNull(member));

        return familyMapper.toFamilyResponse(family);
    }

    @Override
    @Transactional(readOnly = true)
    public FamilyDetailResponse getFamilyById(Long id) {
        User currentUser = getCurrentUser();
        if (!familyMemberRepository.existsByFamilyIdAndUserId(id, currentUser.getId())) {
            throw new UnauthorizedException("You are not in this family");
        }

        Family family = familyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Family", id));

        List<FamilyMember> members = familyMemberRepository.findAllByFamilyId(id);

        FamilyDetailResponse response = familyMapper.toFamilyDetailResponse(family);
        List<FamilyMemberResponse> memberResponses = members.stream()
                .map(familyMapper::toFamilyMemberResponse)
                .collect(Collectors.toList());
        response.setMembers(memberResponses);

        return response;
    }

    @Override
    @Transactional
    public void inviteMember(Long familyId, InviteMemberRequest request) {
        User currentUser = getCurrentUser();
        Family family = familyRepository.findById(familyId)
                .orElseThrow(() -> new ResourceNotFoundException("Family", familyId));

        assertCanManageFamily(familyId, currentUser.getId());

        String email = request.getEmail().trim().toLowerCase(Locale.ROOT);
        User recipient = userRepository.findByEmail(email).orElse(null);
        if (recipient != null && familyMemberRepository.existsByFamilyIdAndUserId(familyId, recipient.getId())) {
            throw new DuplicateResourceException("Member already exists in this family");
        }
        if (familyInvitationRepository.existsByFamily_IdAndRecipientEmailIgnoreCaseAndStatus(
                familyId,
                email,
                InvitationStatus.PENDING
        )) {
            throw new DuplicateResourceException("A pending invitation already exists for this email");
        }

        FamilyInvitation invitation = FamilyInvitation.builder()
                .family(family)
                .sender(currentUser)
                .recipient(recipient)
                .recipientEmail(email)
                .role(normalizeJoinRole(request.getRole()))
                .status(InvitationStatus.PENDING)
                .build();

        familyInvitationRepository.save(invitation);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FamilyInvitationResponse> getReceivedInvitations() {
        User currentUser = getCurrentUser();
        return familyInvitationRepository.findReceivedInvitations(currentUser.getId(), currentUser.getEmail()).stream()
                .map(this::toInvitationResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<FamilyInvitationResponse> getSentInvitations() {
        User currentUser = getCurrentUser();
        return familyInvitationRepository.findAllBySender_IdOrderByCreatedAtDesc(currentUser.getId()).stream()
                .map(this::toInvitationResponse)
                .toList();
    }

    @Override
    @Transactional
    public void handleInvitation(Long invitationId, UpdateInvitationRequest request) {
        User currentUser = getCurrentUser();
        FamilyInvitation invitation = familyInvitationRepository.findById(invitationId)
                .orElseThrow(() -> new ResourceNotFoundException("Invitation", invitationId));

        boolean belongsToCurrentUser = invitation.getRecipient() != null
                && invitation.getRecipient().getId().equals(currentUser.getId());
        boolean invitedByEmail = invitation.getRecipient() == null
                && invitation.getRecipientEmail().equalsIgnoreCase(currentUser.getEmail());

        if (!belongsToCurrentUser && !invitedByEmail) {
            throw new UnauthorizedException("You cannot handle this invitation");
        }

        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new BadRequestException("Invitation has already been handled");
        }

        invitation.setStatus(request.getStatus());

        if (request.getStatus() == InvitationStatus.ACCEPTED) {
            if (familyMemberRepository.existsByFamilyIdAndUserId(invitation.getFamily().getId(), currentUser.getId())) {
                throw new DuplicateResourceException("Member already exists in this family");
            }
            addMemberIfMissing(invitation.getFamily(), currentUser, invitation.getRole());
            if (invitation.getRecipient() == null) {
                invitation.setRecipient(currentUser);
            }
        }

        familyInvitationRepository.save(invitation);
    }

    @Override
    @Transactional
    public FamilyJoinCodeResponse getJoinCode() {
        User currentUser = getCurrentUser();
        FamilyMember member = getActiveFamilyMember(currentUser);
        assertCanManageFamily(member.getFamily().getId(), currentUser.getId());

        Family family = member.getFamily();
        ensureJoinCode(family);
        family = familyRepository.save(family);
        return toJoinCodeResponse(family);
    }

    @Override
    @Transactional
    public FamilyJoinCodeResponse rotateJoinCode() {
        User currentUser = getCurrentUser();
        FamilyMember member = getActiveFamilyMember(currentUser);
        assertCanManageFamily(member.getFamily().getId(), currentUser.getId());

        Family family = member.getFamily();
        family.setJoinCode(generateUniqueJoinCode());
        family.setJoinCodeExpiresAt(Instant.now().plus(JOIN_CODE_TTL_DAYS, ChronoUnit.DAYS));
        family = familyRepository.save(family);
        return toJoinCodeResponse(family);
    }

    @Override
    @Transactional
    public FamilyDetailResponse joinByCode(JoinFamilyByCodeRequest request) {
        User currentUser = getCurrentUser();
        // Multi-family support: users may join additional families without restriction
        String joinCode = normalizeJoinCode(request.getJoinCode());

        Family family = familyRepository.findByJoinCode(joinCode)
                .orElseThrow(() -> new ResourceNotFoundException("Family", "joinCode", joinCode));

        if (family.getJoinCodeExpiresAt() != null && family.getJoinCodeExpiresAt().isBefore(Instant.now())) {
            throw new BadRequestException("Join code has expired");
        }

        addMemberIfMissing(family, currentUser, normalizeJoinRole(request.getRole()));
        return getFamilyById(family.getId());
    }

    @Override
    @Transactional
    public void updateMemberRole(Long familyId, Long memberId, UpdateRoleRequest request) {
        User currentUser = getCurrentUser();

        FamilyMember requester = familyMemberRepository.findByFamilyIdAndUserId(familyId, currentUser.getId())
                .orElseThrow(() -> new UnauthorizedException("You are not in this family"));

        if (requester.getRole() != FamilyRole.OWNER && requester.getRole() != FamilyRole.ADMIN) {
            throw new UnauthorizedException("Only owners and admins can update roles");
        }

        FamilyMember targetMember = familyMemberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("FamilyMember", memberId));

        if (!targetMember.getFamily().getId().equals(familyId)) {
            throw new BadRequestException("Member does not belong to this family");
        }

        if ((request.getRole() == FamilyRole.OWNER || targetMember.getRole() == FamilyRole.OWNER)
                && requester.getRole() != FamilyRole.OWNER) {
            throw new UnauthorizedException("Only owners can change owner role");
        }

        targetMember.setRole(request.getRole());
        familyMemberRepository.save(targetMember);
    }

    private FamilyMember getCurrentFamilyMember(User currentUser) {
        List<FamilyMember> memberships = familyMemberRepository.findAllByUserId(currentUser.getId());
        if (memberships.isEmpty()) {
            throw new ResourceNotFoundException("Family", "userId", String.valueOf(currentUser.getId()));
        }
        return memberships.get(0);
    }

    private FamilyMember getActiveFamilyMember(User currentUser) {
        Long activeFamilyId = FamilyRequestContext.getFamilyId();
        if (activeFamilyId != null) {
            return familyMemberRepository.findByFamilyIdAndUserId(activeFamilyId, currentUser.getId())
                    .orElseThrow(() -> new UnauthorizedException("You are not in this family"));
        }
        return getCurrentFamilyMember(currentUser);
    }

    private void assertCanManageFamily(Long familyId, Long userId) {
        FamilyMember member = familyMemberRepository.findByFamilyIdAndUserId(familyId, userId)
                .orElseThrow(() -> new UnauthorizedException("You are not in this family"));

        if (member.getRole() != FamilyRole.OWNER && member.getRole() != FamilyRole.ADMIN) {
            throw new UnauthorizedException("Only owners and admins can manage invitations");
        }
    }

    private void addMemberIfMissing(Family family, User user, FamilyRole role) {
        if (familyMemberRepository.existsByFamilyIdAndUserId(family.getId(), user.getId())) {
            return;
        }

        FamilyMember member = FamilyMember.builder()
                .family(family)
                .user(user)
                .role(role)
                .build();
        familyMemberRepository.save(member);
    }

    private FamilyRole normalizeRole(FamilyRole role) {
        return role == null ? FamilyRole.MEMBER : role;
    }

    private FamilyRole normalizeJoinRole(FamilyRole role) {
        FamilyRole normalized = normalizeRole(role);
        if (normalized == FamilyRole.OWNER || normalized == FamilyRole.ADMIN) {
            return FamilyRole.MEMBER;
        }
        return normalized;
    }

    private String normalizeJoinCode(String joinCode) {
        return joinCode.trim().replaceAll("\\s+", "").toUpperCase(Locale.ROOT);
    }

    private void ensureJoinCode(Family family) {
        if (family.getJoinCode() != null
                && family.getJoinCodeExpiresAt() != null
                && family.getJoinCodeExpiresAt().isAfter(Instant.now())) {
            return;
        }

        family.setJoinCode(generateUniqueJoinCode());
        family.setJoinCodeExpiresAt(Instant.now().plus(JOIN_CODE_TTL_DAYS, ChronoUnit.DAYS));
    }

    private String generateUniqueJoinCode() {
        String code;
        do {
            StringBuilder builder = new StringBuilder(JOIN_CODE_LENGTH);
            for (int i = 0; i < JOIN_CODE_LENGTH; i++) {
                builder.append(JOIN_CODE_ALPHABET.charAt(RANDOM.nextInt(JOIN_CODE_ALPHABET.length())));
            }
            code = builder.toString();
        } while (familyRepository.existsByJoinCode(code));
        return code;
    }

    private FamilyJoinCodeResponse toJoinCodeResponse(Family family) {
        String joinCode = family.getJoinCode();
        return FamilyJoinCodeResponse.builder()
                .id(family.getId())
                .name(family.getName())
                .joinCode(joinCode)
                .joinLink("carenest://family/join?code=" + joinCode)
                .qrCodeBase64(null)
                .expiresAt(family.getJoinCodeExpiresAt())
                .build();
    }

    private FamilyInvitationResponse toInvitationResponse(FamilyInvitation invitation) {
        return FamilyInvitationResponse.builder()
                .inviteId(invitation.getId())
                .familyId(invitation.getFamily().getId())
                .name(invitation.getFamily().getName())
                .senderEmail(invitation.getSender().getEmail())
                .receiverEmail(invitation.getRecipientEmail())
                .role(invitation.getRole())
                .status(invitation.getStatus())
                .createdAt(invitation.getCreatedAt())
                .build();
    }
}
