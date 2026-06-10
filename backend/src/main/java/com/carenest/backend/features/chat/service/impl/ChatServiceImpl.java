package com.carenest.backend.features.chat.service.impl;

import com.carenest.backend.core.exception.BadRequestException;
import com.carenest.backend.core.exception.ResourceNotFoundException;
import com.carenest.backend.features.auth.entity.User;
import com.carenest.backend.features.auth.repository.UserRepository;
import com.carenest.backend.features.chat.dto.response.ChatMessageResponse;
import com.carenest.backend.features.chat.entity.ChatMessage;
import com.carenest.backend.features.chat.repository.ChatMessageRepository;
import com.carenest.backend.features.chat.service.ChatService;
import com.carenest.backend.features.family.entity.Family;
import com.carenest.backend.features.family.entity.FamilyMember;
import com.carenest.backend.features.family.repository.FamilyMemberRepository;
import com.carenest.backend.features.family.repository.FamilyRepository;
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

        // Kiá»ƒm tra ngÆ°á»i gá»­i cÃ³ thuá»™c gia Ä‘Ã¬nh nÃ y khÃ´ng
        boolean isMember = familyMemberRepository.existsByFamilyIdAndUserId(familyId, senderId);
        if (!isMember) {
            throw new BadRequestException("Báº¡n khÃ´ng pháº£i thành viên cá»§a gia Ä‘Ã¬nh nÃ y.");
        }

        ChatMessage message = ChatMessage.builder()
                .family(family)
                .sender(sender)
                .content(content)
                .build();

        ChatMessage saved = chatMessageRepository.save(message);
        log.info("[Chat] LÆ°u tin nháº¯n: family={}, sender={}", familyId, senderId);

        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ChatMessageResponse> getFamilyMessages(Long familyId, Long requesterId, Pageable pageable) {
        // Kiá»ƒm tra ngÆ°á»i yÃªu cáº§u cÃ³ pháº£i thành viên khÃ´ng
        boolean isMember = familyMemberRepository.existsByFamilyIdAndUserId(familyId, requesterId);
        if (!isMember) {
            throw new BadRequestException("Báº¡n khÃ´ng pháº£i thành viên cá»§a gia Ä‘Ã¬nh nÃ y.");
        }

        return chatMessageRepository
                .findByFamilyId(familyId, pageable)
                .map(this::toResponse);
    }

    // â”€â”€â”€ Private Mapper â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

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
