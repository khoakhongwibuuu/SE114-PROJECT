package com.carenest.backend.features.chat.service.impl;

import com.carenest.backend.core.exception.BadRequestException;
import com.carenest.backend.core.exception.ResourceNotFoundException;
import com.carenest.backend.features.auth.entity.User;
import com.carenest.backend.features.auth.enums.Role;
import com.carenest.backend.features.auth.repository.UserRepository;
import com.carenest.backend.features.chat.dto.response.ChatMessageResponse;
import com.carenest.backend.features.chat.entity.ChatMessage;
import com.carenest.backend.features.chat.entity.GroupChatMessage;
import com.carenest.backend.features.chat.repository.ChatMessageRepository;
import com.carenest.backend.features.chat.repository.GroupChatMessageRepository;
import com.carenest.backend.features.chat.service.ChatService;
import com.carenest.backend.features.community.entity.ChatGroup;
import com.carenest.backend.features.community.entity.ReportTicket;
import com.carenest.backend.features.community.repository.ChatGroupRepository;
import com.carenest.backend.features.community.repository.ReportTicketRepository;
import com.carenest.backend.features.community.repository.UserGroupMembershipRepository;
import com.carenest.backend.features.family.entity.Family;
import com.carenest.backend.features.family.entity.FamilyMember;
import com.carenest.backend.features.family.repository.FamilyMemberRepository;
import com.carenest.backend.features.family.repository.FamilyRepository;
import com.carenest.backend.features.notification.enums.NotificationType;
import com.carenest.backend.features.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final GroupChatMessageRepository groupChatMessageRepository;
    private final FamilyRepository familyRepository;
    private final FamilyMemberRepository familyMemberRepository;
    private final ChatGroupRepository chatGroupRepository;
    private final UserGroupMembershipRepository userGroupMembershipRepository;
    private final ReportTicketRepository reportTicketRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public ChatMessageResponse saveMessage(Long familyId, Long senderId, String content) {
        Family family = familyRepository.findById(familyId)
                .orElseThrow(() -> new ResourceNotFoundException("Family", familyId));

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new ResourceNotFoundException("User", senderId));

        // Kiểm tra người gửi có thuộc gia đình này không
        boolean isMember = familyMemberRepository.existsByFamilyIdAndUserId(familyId, senderId);
        if (!isMember) {
            throw new BadRequestException("Bạn không phải thành viên của gia đình này.");
        }

        ChatMessage message = ChatMessage.builder()
                .family(family)
                .sender(sender)
                .content(normalizeContent(content))
                .build();

        ChatMessage saved = chatMessageRepository.save(message);
        notifyFamilyMessage(saved);
        log.info("[Chat] Lưu tin nhắn: family={}, sender={}", familyId, senderId);

        return toResponse(saved);
    }

    @Override
    @Transactional
    public ChatMessageResponse saveGroupMessage(Long groupId, Long senderId, String content) {
        ChatGroup group = chatGroupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("ChatGroup", groupId));
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new ResourceNotFoundException("User", senderId));
        ensureCanEnterGroup(groupId, sender);
        if (group.isFrozen()) {
            throw new BadRequestException("Nhom dang tam khoa, khong the gui tin nhan moi");
        }

        String trimmedContent = normalizeContent(content);
        GroupChatMessage message = GroupChatMessage.builder()
                .group(group)
                .sender(sender)
                .content(trimmedContent)
                .build();

        GroupChatMessage saved = groupChatMessageRepository.save(message);
        log.info("[GroupChat] Saved message: group={}, sender={}", groupId, senderId);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ChatMessageResponse> getFamilyMessages(Long familyId, Long requesterId, Pageable pageable) {
        // Kiểm tra người yêu cầu có phải thành viên không
        boolean isMember = familyMemberRepository.existsByFamilyIdAndUserId(familyId, requesterId);
        if (!isMember) {
            throw new BadRequestException("Bạn không phải thành viên của gia đình này.");
        }

        return chatMessageRepository
                .findByFamilyId(familyId, pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ChatMessageResponse> getGroupMessages(Long groupId, Long requesterId, Pageable pageable) {
        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new ResourceNotFoundException("User", requesterId));
        chatGroupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("ChatGroup", groupId));
        ensureCanEnterGroup(groupId, requester);

        return groupChatMessageRepository
                .findByGroupId(groupId, pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional
    public void reportGroupMessage(Long messageId, Long reporterId, String reason) {
        User reporter = userRepository.findById(reporterId)
                .orElseThrow(() -> new ResourceNotFoundException("User", reporterId));
        GroupChatMessage message = groupChatMessageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("GroupChatMessage", messageId));
        ChatGroup group = message.getGroup();
        if (group == null) {
            throw new ResourceNotFoundException("ChatGroup", "messageId", messageId.toString());
        }
        ensureCanEnterGroup(group.getId(), reporter);

        String trimmedReason = normalizeContent(reason);
        reportTicketRepository.save(ReportTicket.builder()
                .reportedChatMessage(message)
                .reporter(reporter)
                .reason(trimmedReason)
                .build());
    }

    // ─── Private Mapper ───────────────────────────────────────────────────────

    private ChatMessageResponse toResponse(ChatMessage msg) {
        User sender = msg.getSender();
        return ChatMessageResponse.builder()
                .id(msg.getId())
                .text(msg.getContent())
                .createdAt(msg.getCreatedAt())
                .user(ChatMessageResponse.ChatUserDto.builder()
                        .id(sender.getId())
                        .name(sender.getFullName())
                        .avatar(sender.getAvatarUrl())
                        .role(sender.getRole() != null ? sender.getRole().name() : null)
                        .build())
                .build();
    }

    private ChatMessageResponse toResponse(GroupChatMessage msg) {
        User sender = msg.getSender();
        return ChatMessageResponse.builder()
                .id(msg.getId())
                .text(msg.getContent())
                .createdAt(msg.getCreatedAt())
                .user(ChatMessageResponse.ChatUserDto.builder()
                        .id(sender.getId())
                        .name(sender.getFullName())
                        .avatar(sender.getAvatarUrl())
                        .role(sender.getRole() != null ? sender.getRole().name() : null)
                        .build())
                .build();
    }

    private void ensureCanEnterGroup(Long groupId, User user) {
        boolean isMember = userGroupMembershipRepository.existsByGroupIdAndUserId(groupId, user.getId());
        if (!isMember && user.getRole() != Role.ADMIN) {
            throw new BadRequestException("Bạn không phải thành viên của gia đình này.");
        }
    }

    private void notifyFamilyMessage(ChatMessage message) {
        List<User> recipients = familyMemberRepository.findAllByFamilyId(message.getFamily().getId()).stream()
                .map(FamilyMember::getUser)
                .filter(user -> !user.getId().equals(message.getSender().getId()))
                .toList();

        notificationService.createNotificationForUsers(
                recipients,
                "Tin nhắn gia đình mới",
                displayName(message.getSender()) + ": " + preview(message.getContent()),
                NotificationType.CHAT,
                "FAMILY_CHAT",
                message.getFamily().getId()
        );
    }

    private String displayName(User user) {
        if (user.getFullName() != null && !user.getFullName().isBlank()) {
            return user.getFullName().trim();
        }
        return user.getEmail();
    }

    private String preview(String content) {
        if (content.length() <= 80) {
            return content;
        }
        return content.substring(0, 77) + "...";
    }

    private String normalizeContent(String content) {
        if (content == null || content.isBlank()) {
            throw new BadRequestException("Nội dung tin nhắn không được để trống");
        }
        return content.trim();
    }
}
