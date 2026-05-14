package com.carenest.backend.module.chat.service.impl;

import com.carenest.backend.common.exception.BadRequestException;
import com.carenest.backend.common.exception.ResourceNotFoundException;
import com.carenest.backend.module.auth.entity.User;
import com.carenest.backend.module.auth.repository.UserRepository;
import com.carenest.backend.module.chat.dto.response.ChatMessageResponse;
import com.carenest.backend.module.chat.entity.ChatMessage;
import com.carenest.backend.module.chat.repository.ChatMessageRepository;
import com.carenest.backend.module.chat.service.ChatService;
import com.carenest.backend.module.family.entity.Family;
import com.carenest.backend.module.family.entity.FamilyMember;
import com.carenest.backend.module.family.repository.FamilyMemberRepository;
import com.carenest.backend.module.family.repository.FamilyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final FamilyRepository familyRepository;
    private final FamilyMemberRepository familyMemberRepository;
    private final UserRepository userRepository;

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
                .content(content)
                .build();

        ChatMessage saved = chatMessageRepository.save(message);
        log.info("[Chat] Lưu tin nhắn: family={}, sender={}", familyId, senderId);

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
                .findByFamilyIdOrderByCreatedAtDesc(familyId, pageable)
                .map(this::toResponse);
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
                        .build())
                .build();
    }
}
