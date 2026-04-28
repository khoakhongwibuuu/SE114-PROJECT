package com.carenest.backend.module.aichat.controller;

import com.carenest.backend.common.dto.ApiResponse;
import com.carenest.backend.module.aichat.dto.request.ChatRequest;
import com.carenest.backend.module.aichat.dto.response.ChatResponse;
import com.carenest.backend.module.aichat.service.AiChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class AiChatController {

    private final AiChatService aiChatService;

    @PostMapping("/send")
    public ResponseEntity<ApiResponse<ChatResponse>> sendMessage(@RequestBody @Valid ChatRequest request) {
        ChatResponse response = aiChatService.sendMessage(request);
        return ResponseEntity.ok(ApiResponse.success("Phản hồi từ AI", response));
    }
}
