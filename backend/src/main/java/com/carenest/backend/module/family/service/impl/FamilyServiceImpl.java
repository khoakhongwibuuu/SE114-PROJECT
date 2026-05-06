package com.carenest.backend.module.family.service.impl;

import com.carenest.backend.common.exception.BadRequestException;
import com.carenest.backend.common.exception.DuplicateResourceException;
import com.carenest.backend.common.exception.ResourceNotFoundException;
import com.carenest.backend.common.exception.UnauthorizedException;
import com.carenest.backend.module.auth.entity.User;
import com.carenest.backend.module.auth.repository.UserRepository;
import com.carenest.backend.module.family.dto.request.CreateFamilyRequest;
import com.carenest.backend.module.family.dto.request.InviteMemberRequest;
import com.carenest.backend.module.family.dto.request.UpdateInvitationRequest;
import com.carenest.backend.module.family.dto.request.UpdateRoleRequest;
import com.carenest.backend.module.family.dto.response.FamilyDetailResponse;
import com.carenest.backend.module.family.dto.response.FamilyMemberResponse;
import com.carenest.backend.module.family.dto.response.FamilyResponse;
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

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FamilyServiceImpl implements FamilyService {

    private final FamilyRepository familyRepository;
    private final FamilyMemberRepository familyMemberRepository;
    private final FamilyInvitationRepository familyInvitationRepository;
    private final UserRepository userRepository;
    private final FamilyMapper familyMapper;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("Vui lòng đăng nhập"));
    }

    @Override
    @Transactional(readOnly = true)
    public FamilyDetailResponse getMyFamily() {
        User currentUser = getCurrentUser();
        FamilyMember member = familyMemberRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Family", "userId", String.valueOf(currentUser.getId())));
        
        return getFamilyById(member.getFamily().getId());
    }

    @Override
    @Transactional
    public FamilyResponse createFamily(CreateFamilyRequest request) {
        User currentUser = getCurrentUser();

        Family family = Family.builder()
                .name(request.getName())
                .owner(currentUser)
                .build();
        
        family = familyRepository.save(family);

        // Add the creator as an OWNER in family_members
        FamilyMember member = FamilyMember.builder()
                .family(family)
                .user(currentUser)
                .role(FamilyRole.OWNER)
                .build();
        
        familyMemberRepository.save(member);

        return familyMapper.toFamilyResponse(family);
    }

    @Override
    @Transactional(readOnly = true)
    public FamilyDetailResponse getFamilyById(Long id) {
        Family family = familyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Family", id));

        // Get members
        List<FamilyMember> members = familyMemberRepository.findAllByFamilyId(id);
        
        // Map to Response
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

        // Check if current user has permission (OWNER or ADMIN)
        FamilyMember currentMember = familyMemberRepository.findByFamilyIdAndUserId(familyId, currentUser.getId())
                .orElseThrow(() -> new UnauthorizedException("Bạn không thuộc gia đình này"));
                
        if (currentMember.getRole() == FamilyRole.MEMBER) {
            throw new UnauthorizedException("Chỉ OWNER và ADMIN mới có quyền mời thành viên");
        }

        // Check if recipient is already in the family
        User recipient = userRepository.findByEmail(request.getEmail()).orElse(null);
        if (recipient != null) {
            boolean alreadyInFamily = familyMemberRepository.findByFamilyIdAndUserId(familyId, recipient.getId()).isPresent();
            if (alreadyInFamily) {
                throw new DuplicateResourceException("Thành viên đã có trong gia đình");
            }
        }

        // Create Invitation
        FamilyInvitation invitation = FamilyInvitation.builder()
                .family(family)
                .sender(currentUser)
                .recipient(recipient)
                .recipientEmail(request.getEmail())
                .status(InvitationStatus.PENDING)
                .build();
                
        familyInvitationRepository.save(invitation);
    }

    @Override
    @Transactional
    public void handleInvitation(Long invitationId, UpdateInvitationRequest request) {
        User currentUser = getCurrentUser();
        
        FamilyInvitation invitation = familyInvitationRepository.findById(invitationId)
                .orElseThrow(() -> new ResourceNotFoundException("Invitation", invitationId));

        // Validate recipient
        if (invitation.getRecipient() != null && !invitation.getRecipient().getId().equals(currentUser.getId())) {
             throw new UnauthorizedException("Bạn không có quyền xử lý lời mời này");
        } else if (invitation.getRecipient() == null && !invitation.getRecipientEmail().equals(currentUser.getEmail())) {
             throw new UnauthorizedException("Email lời mời không khớp với tài khoản của bạn");
        }

        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new BadRequestException("Lời mời đã được xử lý");
        }

        invitation.setStatus(request.getStatus());
        
        if (request.getStatus() == InvitationStatus.ACCEPTED) {
            // Check if already joined another way
            boolean alreadyJoined = familyMemberRepository.findByFamilyIdAndUserId(invitation.getFamily().getId(), currentUser.getId()).isPresent();
            if (!alreadyJoined) {
                FamilyMember newMember = FamilyMember.builder()
                        .family(invitation.getFamily())
                        .user(currentUser)
                        .role(FamilyRole.MEMBER)
                        .build();
                familyMemberRepository.save(newMember);
            }
            
            // If the recipient was previously null but invited by email, update it
            if (invitation.getRecipient() == null) {
                invitation.setRecipient(currentUser);
            }
        }
        
        familyInvitationRepository.save(invitation);
    }

    @Override
    @Transactional
    public void updateMemberRole(Long familyId, Long memberId, UpdateRoleRequest request) {
        User currentUser = getCurrentUser();
        
        FamilyMember requester = familyMemberRepository.findByFamilyIdAndUserId(familyId, currentUser.getId())
                .orElseThrow(() -> new UnauthorizedException("Bạn không thuộc gia đình này"));
                
        if (requester.getRole() != FamilyRole.OWNER && requester.getRole() != FamilyRole.ADMIN) {
             throw new UnauthorizedException("Chỉ OWNER hoặc ADMIN mới có quyền thay đổi vai trò");
        }

        FamilyMember targetMember = familyMemberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("FamilyMember", memberId));

        if (!targetMember.getFamily().getId().equals(familyId)) {
            throw new BadRequestException("Thành viên không thuộc gia đình này");
        }

        // Only OWNER can grant/revoke OWNER or ADMIN role
        if ((request.getRole() == FamilyRole.OWNER || targetMember.getRole() == FamilyRole.OWNER) 
            && requester.getRole() != FamilyRole.OWNER) {
            throw new UnauthorizedException("Chỉ OWNER mới có quyền thay đổi cấp bậc quản lý cao nhất");
        }

        targetMember.setRole(request.getRole());
        familyMemberRepository.save(targetMember);
    }
}
