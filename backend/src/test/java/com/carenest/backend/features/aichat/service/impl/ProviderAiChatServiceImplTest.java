package com.carenest.backend.features.aichat.service.impl;

import com.carenest.backend.features.ai.AiGatewayClient;
import com.carenest.backend.features.aichat.dto.request.ChatRequest;
import com.carenest.backend.features.aichat.dto.response.ChatResponse;
import com.carenest.backend.features.aichat.entity.AiChatMessage;
import com.carenest.backend.features.aichat.entity.AiChatSession;
import com.carenest.backend.features.aichat.repository.AiChatMessageRepository;
import com.carenest.backend.features.aichat.repository.AiChatSessionRepository;
import com.carenest.backend.features.auth.entity.User;
import com.carenest.backend.features.auth.enums.Role;
import com.carenest.backend.features.family.util.FamilySecurityUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProviderAiChatServiceImplTest {

    @Mock
    private FamilySecurityUtil familySecurityUtil;

    @Mock
    private AiChatSessionRepository sessionRepository;

    @Mock
    private AiChatMessageRepository messageRepository;

    @Mock
    private AiGatewayClient aiGatewayClient;

    @Test
    void sendMessage_createsSessionPersistsMessagesAndCallsAiGateway() {
        User user = User.builder()
                .email("patient@example.test")
                .passwordHash("hash")
                .fullName("Patient")
                .role(Role.USER)
                .build();
        user.setId(10L);
        AiChatSession savedSession = AiChatSession.builder()
                .user(user)
                .title("Con toi bi sot")
                .status("ACTIVE")
                .build();
        savedSession.setId(77L);
        ChatRequest request = new ChatRequest();
        request.setMessage("Con toi bi sot nen lam gi?");

        when(familySecurityUtil.getCurrentUser()).thenReturn(user);
        when(sessionRepository.findByUserIdAndStatus(10L, "ACTIVE")).thenReturn(Optional.empty());
        when(sessionRepository.save(any(AiChatSession.class))).thenReturn(savedSession);
        when(aiGatewayClient.chat(request.getMessage(), 77L)).thenReturn(ChatResponse.builder()
                .reply("Theo doi nhiet do va hoi bac si neu sot cao.")
                .conversationId(77L)
                .build());

        ProviderAiChatServiceImpl service = new ProviderAiChatServiceImpl(
                familySecurityUtil,
                sessionRepository,
                messageRepository,
                aiGatewayClient
        );

        ChatResponse response = service.sendMessage(request);

        assertEquals("Theo doi nhiet do va hoi bac si neu sot cao.", response.getReply());
        assertEquals(77L, response.getConversationId());
        verify(aiGatewayClient).chat(request.getMessage(), 77L);

        ArgumentCaptor<AiChatMessage> messageCaptor = ArgumentCaptor.forClass(AiChatMessage.class);
        verify(messageRepository, org.mockito.Mockito.times(2)).save(messageCaptor.capture());
        assertEquals("USER", messageCaptor.getAllValues().get(0).getRole());
        assertEquals("ASSISTANT", messageCaptor.getAllValues().get(1).getRole());
    }
}
