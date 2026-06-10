package com.carenest.backend.features.booking.controller;

import com.carenest.backend.core.api.ApiResponse;
import com.carenest.backend.features.booking.dto.response.ConsultationMessageResponse;
import com.carenest.backend.features.booking.service.ConsultationMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/consultations")
@RequiredArgsConstructor
public class ConsultationMessageController {

    private final ConsultationMessageService consultationMessageService;

    @GetMapping("/threads/{threadId}/messages")
    public ResponseEntity<ApiResponse<List<ConsultationMessageResponse>>> getMessages(@PathVariable("threadId") Long threadId) {
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách tin nhắn thành công", consultationMessageService.getMessages(threadId)));
    }
}
