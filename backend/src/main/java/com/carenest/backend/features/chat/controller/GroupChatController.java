package com.carenest.backend.features.chat.controller;

import com.carenest.backend.core.api.ApiResponse;
import com.carenest.backend.core.api.PageResponse;
import com.carenest.backend.features.auth.entity.User;
import com.carenest.backend.features.chat.dto.request.SendGroupMessageRequest;
import com.carenest.backend.features.chat.dto.response.ChatMessageResponse;
import com.carenest.backend.features.chat.service.ChatService;
import com.carenest.backend.features.community.dto.request.ReportPostRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/communities")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('USER', 'DOCTOR', 'ADMIN')")
public class GroupChatController {

    private final ChatService chatService;

    @GetMapping("/{groupId}/messages")
    public ApiResponse<PageResponse<ChatMessageResponse>> getGroupMessages(
            @PathVariable("groupId") Long groupId,
            @AuthenticationPrincipal User user,
            @PageableDefault(size = 30, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ApiResponse.success(PageResponse.of(chatService.getGroupMessages(groupId, user.getId(), pageable)));
    }

    @PostMapping("/{groupId}/messages")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ChatMessageResponse> sendGroupMessage(
            @PathVariable("groupId") Long groupId,
            @AuthenticationPrincipal User user,
            @Valid @RequestBody SendGroupMessageRequest request) {
        return ApiResponse.success(
                "Da gui tin nhan",
                chatService.saveGroupMessage(groupId, user.getId(), request.getContent())
        );
    }

    @PostMapping("/messages/{messageId}/report")
    public ApiResponse<Void> reportGroupMessage(
            @PathVariable("messageId") Long messageId,
            @AuthenticationPrincipal User user,
            @Valid @RequestBody ReportPostRequest request) {
        chatService.reportGroupMessage(messageId, user.getId(), request.getReason());
        return ApiResponse.success("Da gui bao cao tin nhan", null);
    }
}
