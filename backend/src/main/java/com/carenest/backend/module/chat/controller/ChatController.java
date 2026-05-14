package com.carenest.backend.module.chat.controller;

import com.carenest.backend.common.dto.ApiResponse;
import com.carenest.backend.module.auth.entity.User;
import com.carenest.backend.module.chat.dto.request.SendMessageRequest;
import com.carenest.backend.module.chat.dto.response.ChatMessageResponse;
import com.carenest.backend.module.chat.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/families")
@RequiredArgsConstructor
@Tag(name = "Chat", description = "Chat box gia đình real-time")
@SecurityRequirement(name = "bearerAuth")
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;

    // ─── REST: Lấy lịch sử tin nhắn ──────────────────────────────────────────

    @GetMapping("/{familyId}/messages")
    @Operation(summary = "Lấy lịch sử chat của gia đình (có phân trang)")
    public ApiResponse<Page<ChatMessageResponse>> getMessages(
            @PathVariable Long familyId,
            @AuthenticationPrincipal User user,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<ChatMessageResponse> page = chatService.getFamilyMessages(familyId, user.getId(), pageable);
        return ApiResponse.success("Lấy lịch sử tin nhắn thành công", page);
    }

    // ─── STOMP WebSocket: Nhận và broadcast tin nhắn ─────────────────────────

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload @Valid SendMessageRequest request,
                            @AuthenticationPrincipal User sender) {

        // 1. Lưu vào DB
        ChatMessageResponse saved = chatService.saveMessage(
                request.getFamilyId(),
                sender.getId(),
                request.getContent()
        );

        // 2. Broadcast đến tất cả thành viên đang subscribe kênh này
        messagingTemplate.convertAndSend(
                "/topic/family/" + request.getFamilyId(),
                saved
        );

        log.info("[Chat] Broadcast: family={}, sender={}", request.getFamilyId(), sender.getEmail());
    }
}
