package com.carenest.backend.features.aichat.controller;

import com.carenest.backend.core.exception.BadRequestException;
import com.carenest.backend.features.aichat.dto.request.ChatRequest;
import com.carenest.backend.features.aichat.service.AiChatService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class AiChatControllerTest {

    @Mock
    private AiChatService aiChatService;

    @InjectMocks
    private AiChatController aiChatController;

    @Test
    void sendMessage_rejectsWhenAiChatIsDisabledByDefault() {
        ChatRequest request = new ChatRequest();
        request.setMessage("Xin chao");

        assertThrows(BadRequestException.class, () -> aiChatController.sendMessage(request));

        verifyNoInteractions(aiChatService);
    }

    @Test
    void getSessionMessages_rejectsWhenAiChatIsDisabledByDefault() {
        assertThrows(
                BadRequestException.class,
                () -> aiChatController.getSessionMessages(1L, PageRequest.of(0, 10))
        );

        verifyNoInteractions(aiChatService);
    }
}
