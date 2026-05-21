package com.carenest.backend.module.aichat.controller;

import com.carenest.backend.common.dto.ApiResponse;
import com.carenest.backend.common.dto.PageResponse;
import com.carenest.backend.module.aichat.dto.request.ChatRequest;
import com.carenest.backend.module.aichat.dto.response.AiChatMessageResponse;
import com.carenest.backend.module.aichat.dto.response.ChatResponse;
import com.carenest.backend.module.aichat.service.AiChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
@org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('USER', 'DOCTOR', 'ADMIN')")
public class AiChatController {

    private final AiChatService aiChatService;

    @PostMapping("/send")
    public ResponseEntity<ApiResponse<ChatResponse>> sendMessage(@RequestBody @Valid ChatRequest request) {
        ChatResponse response = aiChatService.sendMessage(request);
        return ResponseEntity.ok(ApiResponse.success("Đã nhận phản hồi từ AI", response));
    }

    @GetMapping("/sessions/{sessionId}/messages")
    public ApiResponse<PageResponse<AiChatMessageResponse>> getSessionMessages(
            @PathVariable("sessionId") Long sessionId,
            @PageableDefault(size = 30, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ApiResponse.success(aiChatService.getSessionMessages(sessionId, pageable));
    }
}
