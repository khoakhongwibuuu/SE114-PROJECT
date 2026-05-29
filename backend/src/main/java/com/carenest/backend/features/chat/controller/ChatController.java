package com.carenest.backend.features.chat.controller;

import com.carenest.backend.core.api.ApiResponse;
import com.carenest.backend.features.auth.entity.User;
import com.carenest.backend.features.chat.dto.response.ChatMessageResponse;
import com.carenest.backend.features.chat.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/families")
@RequiredArgsConstructor
@Tag(name = "Chat", description = "Chat box gia Ä‘Ã¬nh real-time")
@SecurityRequirement(name = "bearerAuth")
@org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('USER', 'DOCTOR', 'ADMIN')")
public class ChatController {

    private final ChatService chatService;

    // â”€â”€â”€ REST: Láº¥y lá»‹ch sá»­ tin nháº¯n â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    @GetMapping("/{familyId}/messages")
    @Operation(summary = "Láº¥y lá»‹ch sá»­ chat cá»§a gia Ä‘Ã¬nh (cÃ³ phÃ¢n trang)")
    public ApiResponse<Page<ChatMessageResponse>> getMessages(
            @PathVariable("familyId") Long familyId,
            @AuthenticationPrincipal User user,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<ChatMessageResponse> page = chatService.getFamilyMessages(familyId, user.getId(), pageable);
        return ApiResponse.success("Láº¥y lá»‹ch sá»­ tin nháº¯n thÃ nh cÃ´ng", page);
    }
}
