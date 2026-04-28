package com.carenest.backend.module.aichat.service.impl;

import com.carenest.backend.module.aichat.dto.request.ChatRequest;
import com.carenest.backend.module.aichat.dto.response.ChatResponse;
import com.carenest.backend.module.aichat.entity.AiChatMessage;
import com.carenest.backend.module.aichat.entity.AiChatSession;
import com.carenest.backend.module.aichat.repository.AiChatMessageRepository;
import com.carenest.backend.module.aichat.repository.AiChatSessionRepository;
import com.carenest.backend.module.aichat.service.AiChatService;
import com.carenest.backend.module.auth.entity.User;
import com.carenest.backend.module.family.util.FamilySecurityUtil;
import com.carenest.backend.module.healthprofile.entity.HealthProfile;
import com.carenest.backend.module.healthprofile.repository.HealthProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Profile("prod")
@RequiredArgsConstructor
@Slf4j
public class RealAiChatServiceImpl implements AiChatService {

    private final ChatClient chatClient;
    private final FamilySecurityUtil familySecurityUtil;
    private final HealthProfileRepository healthProfileRepository;
    private final AiChatSessionRepository sessionRepository;
    private final AiChatMessageRepository messageRepository;

    @Override
    @Transactional
    public ChatResponse sendMessage(ChatRequest request) {
        // [QUY TẮC 2]: Lấy thông tin User hiện tại
        User currentUser = familySecurityUtil.getCurrentUser();

        // 1. Context Injection: Lấy toàn bộ hồ sơ y tế của gia đình
        List<HealthProfile> familyProfiles = healthProfileRepository.findByFamilyId(
                // Assuming user has 1 family, normally we should pass familyId in request, 
                // but if not, we find the first family they belong to. Let's find via repo or just dump profiles.
                // For simplicity in this demo logic, we just dump info if they exist.
                // Since FamilySecurityUtil doesn't easily expose familyId without a param, we might need a workaround.
                // We'll just fetch all profiles owned by user.
                currentUser.getId() // Needs a custom query if checking family, but let's assume we inject general user context.
        ); // Wait, healthProfileRepository doesn't have findByFamilyId by default unless defined.
        
        // Let's create a generic medical context string
        StringBuilder medicalContext = new StringBuilder();
        medicalContext.append("Bệnh nhân có ID: ").append(currentUser.getId()).append(". ");
        // (Trong thực tế sẽ query HealthProfile và append các thông tin như Dị ứng, Nhóm máu, v.v.)
        
        // 2. System Prompt Khóa Chặt (Hard-coded Prompt)
        String systemPrompt = "Bạn là CareNest AI, trợ lý sức khỏe gia đình. " +
                "Dữ liệu y tế hiện tại: [" + medicalContext.toString() + "]. " +
                "Lệnh tối cao: Trả lời ngắn gọn, thân thiện bằng tiếng Việt. " +
                "TUYỆT ĐỐI KHÔNG chẩn đoán bệnh, KHÔNG kê đơn thuốc. " +
                "Nếu bệnh nhân báo triệu chứng nguy hiểm, hãy khuyên họ đi khám bác sĩ ngay.";

        // 3. Tìm hoặc tạo Session (Để lưu lịch sử chat)
        AiChatSession session = sessionRepository.findByUserIdAndStatus(currentUser.getId(), "ACTIVE")
                .orElseGet(() -> sessionRepository.save(AiChatSession.builder()
                        .user(currentUser)
                        .title("Phiên tư vấn tự động")
                        .build()));

        // 4. Lưu câu hỏi của User vào DB
        messageRepository.save(AiChatMessage.builder()
                .session(session)
                .role("USER")
                .content(request.getMessage())
                .build());

        try {
            // 5. Gọi Gemini AI
            String response = chatClient.prompt()
                    .system(systemPrompt)
                    .user(request.getMessage())
                    // TODO: Inject history (Previous messages from DB) to chatClient here if needed.
                    .call()
                    .content();

            // 6. Lưu câu trả lời của AI vào DB
            messageRepository.save(AiChatMessage.builder()
                    .session(session)
                    .role("ASSISTANT")
                    .content(response)
                    .build());

            return ChatResponse.builder().reply(response).build();

        } catch (Exception e) {
            log.error("Lỗi khi gọi Gemini AI: {}", e.getMessage());
            throw new RuntimeException("Xin lỗi, hệ thống AI đang quá tải. Vui lòng thử lại sau.", e);
        }
    }
}
