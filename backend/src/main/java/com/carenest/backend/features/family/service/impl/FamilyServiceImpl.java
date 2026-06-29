package com.carenest.backend.features.family.service.impl;

import com.carenest.backend.core.exception.BadRequestException;
import com.carenest.backend.core.exception.DuplicateResourceException;
import com.carenest.backend.core.exception.ResourceNotFoundException;
import com.carenest.backend.core.exception.UnauthorizedException;
import com.carenest.backend.features.auth.entity.User;
import com.carenest.backend.features.auth.repository.UserRepository;
import com.carenest.backend.features.family.dto.request.CreateFamilyRequest;
import com.carenest.backend.features.family.dto.request.InviteMemberRequest;
import com.carenest.backend.features.family.dto.request.JoinFamilyByCodeRequest;
import com.carenest.backend.features.family.dto.request.UpdateInvitationRequest;
import com.carenest.backend.features.family.dto.request.UpdateRoleRequest;
import com.carenest.backend.features.family.dto.response.FamilyDetailResponse;
import com.carenest.backend.features.family.dto.response.FamilyInvitationResponse;
import com.carenest.backend.features.family.dto.response.FamilyJoinCodeResponse;
import com.carenest.backend.features.family.dto.response.FamilyMemberResponse;
import com.carenest.backend.features.family.dto.response.FamilyResponse;
import com.carenest.backend.features.family.dto.response.FamilySummaryResponse;
import com.carenest.backend.features.family.context.FamilyRequestContext;
import com.carenest.backend.features.family.entity.Family;
import com.carenest.backend.features.family.entity.FamilyInvitation;
import com.carenest.backend.features.family.entity.FamilyMember;
import com.carenest.backend.features.family.enums.FamilyRole;
import com.carenest.backend.features.family.enums.InvitationStatus;
import com.carenest.backend.features.family.mapper.FamilyMapper;
import com.carenest.backend.features.family.repository.FamilyInvitationRepository;
import com.carenest.backend.features.family.repository.FamilyMemberRepository;
import com.carenest.backend.features.family.repository.FamilyRepository;
import com.carenest.backend.features.family.service.FamilyService;
import com.carenest.backend.features.healthprofile.entity.HealthProfile;
import com.carenest.backend.features.healthprofile.repository.HealthProfileRepository;
import com.carenest.backend.features.notification.enums.NotificationType;
import com.carenest.backend.features.notification.service.NotificationService;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
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
    private final HealthProfileRepository healthProfileRepository;
    private final NotificationService notificationService;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("Vui lòng đăng nhập"));
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
    @Transactional
    public FamilyDetailResponse getFamilyById(Long id) {
        User currentUser = getCurrentUser();
        if (!familyMemberRepository.existsByFamilyIdAndUserId(id, currentUser.getId())) {
            throw new AccessDeniedException("Bạn không thuộc gia đình này");
        }

        Family family = familyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Family", id));

        List<FamilyMember> members = familyMemberRepository.findAllByFamilyId(id);

        FamilyDetailResponse response = familyMapper.toFamilyDetailResponse(family);
        List<FamilyMemberResponse> memberResponses = members.stream()
                .map(member -> {
                    // Try to find the member's profile within this family first, then fall back to personal profile
                    HealthProfile profile = healthProfileRepository
                            .findFirstByFamilyIdAndUserIdAndDeletedAtIsNull(id, member.getUser().getId())
                            .orElse(healthProfileRepository.findFirstByUserIdAndFamilyIsNullAndDeletedAtIsNull(member.getUser().getId()).orElse(null));
                    FamilyMemberResponse memberResponse = familyMapper.toFamilyMemberResponse(member);
                    memberResponse.setIsChild(false);
                    memberResponse.setIsEditable(member.getUser().getId().equals(currentUser.getId()));
                    if (profile != null) {
                        memberResponse.setProfileId(profile.getId());
                        memberResponse.setFullName(
                                profile.getFullName() != null && !profile.getFullName().isBlank()
                                        ? profile.getFullName()
                                        : (member.getUser().getFullName() != null && !member.getUser().getFullName().isBlank()
                                        ? member.getUser().getFullName()
                                        : member.getUser().getEmail())
                        );
                        if (profile.getAvatarUrl() != null && !profile.getAvatarUrl().isBlank()) {
                            memberResponse.setAvatarUrl(profile.getAvatarUrl());
                        }
                    } else {
                        memberResponse.setFullName(member.getUser().getFullName() != null && !member.getUser().getFullName().isBlank() ? member.getUser().getFullName() : member.getUser().getEmail());
                        memberResponse.setAvatarUrl(member.getUser().getAvatarUrl());
                    }
                    return memberResponse;
                })
                .collect(Collectors.toList());

        List<HealthProfile> dependentProfiles = healthProfileRepository.findByFamilyIdAndIsChildTrueAndDeletedAtIsNull(id);
        List<FamilyMemberResponse> dependentResponses = dependentProfiles.stream()
                .map(profile -> {
                    FamilyMemberResponse r = new FamilyMemberResponse();
                    r.setId(profile.getId()); // Use profile ID as pseudo member ID
                    r.setProfileId(profile.getId());
                    r.setFullName(profile.getFullName());
                    r.setAvatarUrl(profile.getAvatarUrl());
                    r.setRole(com.carenest.backend.features.family.enums.FamilyRole.MEMBER);
                    r.setJoinedAt(profile.getCreatedAt() != null ? profile.getCreatedAt() : java.time.Instant.now());
                    r.setIsChild(true);
                    r.setIsEditable(profile.getUser() != null && profile.getUser().getId().equals(currentUser.getId()));
                    return r;
                }).collect(Collectors.toList());

        memberResponses.addAll(dependentResponses);
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
            throw new DuplicateResourceException("Thành viên đã tồn tại trong gia đình này");
        }
        if (familyInvitationRepository.existsByFamily_IdAndRecipientEmailIgnoreCaseAndStatus(
                familyId,
                email,
                InvitationStatus.PENDING
        )) {
            throw new DuplicateResourceException("Email này đã có lời mời đang chờ xử lý");
        }

        FamilyInvitation invitation = FamilyInvitation.builder()
                .family(family)
                .sender(currentUser)
                .recipient(recipient)
                .recipientEmail(email)
                .role(normalizeJoinRole(request.getRole()))
                .status(InvitationStatus.PENDING)
                .build();

        FamilyInvitation savedInvitation = familyInvitationRepository.save(invitation);
        notifyInvitationCreated(savedInvitation);
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
            throw new AccessDeniedException("Bạn không có quyền xử lý lời mời này");
        }

        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new BadRequestException("Lời mời này đã được xử lý");
        }

        if (request.getStatus() == InvitationStatus.PENDING) {
            throw new BadRequestException("Trạng thái xử lý lời mời không hợp lệ");
        }

        invitation.setStatus(request.getStatus());

        if (request.getStatus() == InvitationStatus.ACCEPTED) {
            if (familyMemberRepository.existsByFamilyIdAndUserId(invitation.getFamily().getId(), currentUser.getId())) {
                throw new DuplicateResourceException("Thành viên đã tồn tại trong gia đình này");
            }
            addMemberIfMissing(invitation.getFamily(), currentUser, invitation.getRole());
            if (invitation.getRecipient() == null) {
                invitation.setRecipient(currentUser);
            }
        }

        FamilyInvitation savedInvitation = familyInvitationRepository.save(invitation);
        notifyInvitationHandled(savedInvitation, currentUser);
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
            throw new BadRequestException("Mã gia đình đã hết hạn");
        }

        boolean wasMember = familyMemberRepository.existsByFamilyIdAndUserId(family.getId(), currentUser.getId());
        addMemberIfMissing(family, currentUser, normalizeJoinRole(request.getRole()));
        if (!wasMember) {
            notifyFamilyManagersMemberJoined(family, currentUser);
        }
        return getFamilyById(family.getId());
    }

    @Override
    @Transactional
    public FamilyDetailResponse joinByQr(MultipartFile image, FamilyRole role) {
        String qrPayload = decodeQrPayload(image);
        JoinFamilyByCodeRequest request = JoinFamilyByCodeRequest.builder()
                .joinCode(extractJoinCode(qrPayload))
                .role(role)
                .build();
        return joinByCode(request);
    }

    @Override
    @Transactional
    public void updateMemberRole(Long familyId, Long memberId, UpdateRoleRequest request) {
        User currentUser = getCurrentUser();

        FamilyMember requester = familyMemberRepository.findByFamilyIdAndUserId(familyId, currentUser.getId())
                .orElseThrow(() -> new AccessDeniedException("Bạn không thuộc gia đình này"));

        if (requester.getRole() != FamilyRole.OWNER && requester.getRole() != FamilyRole.ADMIN) {
            throw new AccessDeniedException("Chỉ chủ gia đình và quản trị viên mới có quyền cập nhật vai trò");
        }

        if (request.getRole() == null) {
            throw new BadRequestException("Vai trò không hợp lệ");
        }

        FamilyMember targetMember = familyMemberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("FamilyMember", memberId));

        if (!targetMember.getFamily().getId().equals(familyId)) {
            throw new BadRequestException("Thành viên không thuộc gia đình này");
        }

        if (request.getRole() == FamilyRole.OWNER || targetMember.getRole() == FamilyRole.OWNER) {
            throw new BadRequestException("Không thể thay đổi vai trò chủ gia đình bằng chức năng này");
        }

        targetMember.setRole(request.getRole());
        familyMemberRepository.save(targetMember);
    }

    private FamilyMember getCurrentFamilyMember(User currentUser) {
        Long activeFamilyId = FamilyRequestContext.getFamilyId();
        if (activeFamilyId != null) {
            return familyMemberRepository.findByFamilyIdAndUserId(activeFamilyId, currentUser.getId())
                    .orElseThrow(() -> new AccessDeniedException("Báº¡n khĂ´ng thuá»™c gia Ä‘Ă¬nh nĂ y"));
        }
        List<FamilyMember> memberships = familyMemberRepository.findAllByUserId(currentUser.getId());
        if (memberships.isEmpty()) {
            throw new ResourceNotFoundException("Family", "userId", String.valueOf(currentUser.getId()));
        }
        if (memberships.size() > 1) {
            throw new BadRequestException("Vui lĂ²ng chá» n gia Ä‘Ă¬nh Ä‘ang hoáº¡t Ä‘á»™ng trÆ°á»›c khi thá»±c hiá»‡n thao tĂ¡c nĂ y");
        }
        return memberships.get(0);
    }

    private FamilyMember getActiveFamilyMember(User currentUser) {
        Long activeFamilyId = FamilyRequestContext.getFamilyId();
        if (activeFamilyId != null) {
            return familyMemberRepository.findByFamilyIdAndUserId(activeFamilyId, currentUser.getId())
                    .orElseThrow(() -> new AccessDeniedException("Bạn không thuộc gia đình này"));
        }
        return getCurrentFamilyMember(currentUser);
    }

    private void assertCanManageFamily(Long familyId, Long userId) {
        FamilyMember member = familyMemberRepository.findByFamilyIdAndUserId(familyId, userId)
                .orElseThrow(() -> new AccessDeniedException("Bạn không thuộc gia đình này"));

        if (member.getRole() != FamilyRole.OWNER && member.getRole() != FamilyRole.ADMIN) {
            throw new AccessDeniedException("Chỉ chủ gia đình và quản trị viên mới có quyền quản lý lời mời");
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
        if (joinCode == null || joinCode.isBlank()) {
            throw new BadRequestException("MÃ£ gia Ä‘Ă¬nh khĂ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng");
        }
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
        String joinLink = "carenest://family/join?code=" + joinCode;
        return FamilyJoinCodeResponse.builder()
                .id(family.getId())
                .name(family.getName())
                .joinCode(joinCode)
                .joinLink(joinLink)
                .qrCodeBase64(generateQrCodeBase64(joinLink))
                .expiresAt(family.getJoinCodeExpiresAt())
                .build();
    }

    private String generateQrCodeBase64(String payload) {
        try {
            BitMatrix bitMatrix = new MultiFormatWriter().encode(payload, BarcodeFormat.QR_CODE, 256, 256);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (Exception e) {
            throw new BadRequestException("Không thể tạo mã QR gia đình");
        }
    }

    private String decodeQrPayload(MultipartFile image) {
        if (image == null || image.isEmpty()) {
            throw new BadRequestException("Vui lòng tải lên ảnh QR");
        }

        try {
            BufferedImage bufferedImage = ImageIO.read(image.getInputStream());
            if (bufferedImage == null) {
                throw new BadRequestException("Ảnh QR không hợp lệ");
            }
            BinaryBitmap bitmap = new BinaryBitmap(
                    new HybridBinarizer(new BufferedImageLuminanceSource(bufferedImage)));
            return new MultiFormatReader().decode(bitmap).getText();
        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new BadRequestException("Không thể đọc mã QR gia đình");
        }
    }

    private String extractJoinCode(String payload) {
        if (payload == null || payload.isBlank()) {
            throw new BadRequestException("Mã QR không chứa mã gia đình");
        }

        String trimmed = payload.trim();
        try {
            URI uri = URI.create(trimmed);
            String query = uri.getRawQuery();
            if (query != null) {
                for (String pair : query.split("&")) {
                    String[] parts = pair.split("=", 2);
                    if (parts.length == 2 && "code".equalsIgnoreCase(parts[0])) {
                        return URLDecoder.decode(parts[1], StandardCharsets.UTF_8);
                    }
                }
            }
        } catch (IllegalArgumentException ignored) {
            // Raw join-code payloads are accepted below.
        }

        return trimmed;
    }

    private void notifyInvitationCreated(FamilyInvitation invitation) {
        if (invitation.getRecipient() == null) {
            return;
        }

        notificationService.createNotificationForUser(
                invitation.getRecipient(),
                "Bạn có lời mời gia đình mới",
                displayName(invitation.getSender()) + " đã mời bạn tham gia gia đình " + invitation.getFamily().getName() + ".",
                NotificationType.FAMILY,
                "FAMILY_INVITATION",
                invitation.getId()
        );
    }

    private void notifyInvitationHandled(FamilyInvitation invitation, User actor) {
        String action = invitation.getStatus() == InvitationStatus.ACCEPTED ? "chấp nhận" : "từ chối";
        notificationService.createNotificationForUser(
                invitation.getSender(),
                "Lời mời gia đình đã được " + action,
                displayName(actor) + " đã " + action + " lời mời tham gia gia đình " + invitation.getFamily().getName() + ".",
                NotificationType.FAMILY,
                "FAMILY_INVITATION",
                invitation.getId()
        );
    }

    private void notifyFamilyManagersMemberJoined(Family family, User newMember) {
        List<User> managers = familyMemberRepository.findAllByFamilyId(family.getId()).stream()
                .filter(member -> member.getRole() == FamilyRole.OWNER || member.getRole() == FamilyRole.ADMIN)
                .map(FamilyMember::getUser)
                .filter(user -> !user.getId().equals(newMember.getId()))
                .toList();

        notificationService.createNotificationForUsers(
                managers,
                "Có thành viên mới trong gia đình",
                displayName(newMember) + " vừa tham gia gia đình " + family.getName() + ".",
                NotificationType.FAMILY,
                "FAMILY",
                family.getId()
        );
    }

    private String displayName(User user) {
        if (user.getFullName() != null && !user.getFullName().isBlank()) {
            return user.getFullName().trim();
        }
        return user.getEmail();
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
