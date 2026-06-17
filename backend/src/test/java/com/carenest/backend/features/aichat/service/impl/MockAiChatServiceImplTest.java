package com.carenest.backend.features.aichat.service.impl;

import com.carenest.backend.core.exception.BadRequestException;
import com.carenest.backend.features.aichat.dto.request.ChatRequest;
import com.carenest.backend.features.aichat.repository.AiChatMessageRepository;
import com.carenest.backend.features.aichat.repository.AiChatSessionRepository;
import com.carenest.backend.features.family.util.FamilySecurityUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class MockAiChatServiceImplTest {

    @Mock
    private FamilySecurityUtil familySecurityUtil;

    @Mock
    private AiChatSessionRepository sessionRepository;

    @Mock
    private AiChatMessageRepository messageRepository;

    @Test
    void sendMessage_doesNotReturnMockMedicalAdviceInMvp() {
        MockAiChatServiceImpl service = new MockAiChatServiceImpl(
                familySecurityUtil,
                sessionRepository,
                messageRepository
        );
        ChatRequest request = new ChatRequest();
        request.setMessage("Con toi bi sot nen lam gi?");

        assertThrows(BadRequestException.class, () -> service.sendMessage(request));

        verifyNoInteractions(familySecurityUtil, sessionRepository, messageRepository);
    }
}
