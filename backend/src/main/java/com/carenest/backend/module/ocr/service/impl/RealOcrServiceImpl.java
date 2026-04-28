package com.carenest.backend.module.ocr.service.impl;

import com.carenest.backend.module.ocr.dto.request.ParseOcrRequest;
import com.carenest.backend.module.ocr.dto.response.ParsedMedicationDto;
import com.carenest.backend.module.ocr.service.OcrService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.context.annotation.Profile;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Profile("prod")
@RequiredArgsConstructor
@Slf4j
public class RealOcrServiceImpl implements OcrService {

    private final ChatClient chatClient; // Bean do Spring AI tự động inject

    @Override
    public List<ParsedMedicationDto> parseRawTextToMedications(ParseOcrRequest request) {
        log.info("Bắt đầu Parse OCR Text sử dụng Gemini AI (PROD Mode)");
        try {
            // [QUY TẮC 3]: Ép kiểu JSON chặt chẽ bằng BeanOutputConverter
            BeanOutputConverter<List<ParsedMedicationDto>> converter = 
                new BeanOutputConverter<>(new ParameterizedTypeReference<List<ParsedMedicationDto>>() {});
            
            String jsonSchemaInstruction = converter.getFormat();

            String systemPrompt = "Bạn là một dược sĩ chuyên nghiệp. Nhiệm vụ của bạn là bóc tách văn bản thô từ ảnh đơn thuốc " +
                                  "thành một danh sách thuốc có cấu trúc. " +
                                  "Chỉ trả về định dạng JSON hợp lệ, không chứa mã code block hay markdown thừa. " +
                                  "Nếu không tìm thấy durationDays, hãy mặc định là 7. " +
                                  jsonSchemaInstruction;

            String response = chatClient.prompt()
                    .system(systemPrompt)
                    .user(request.getRawText())
                    .call()
                    .content();

            // Bẫy lỗi JSON Parser: Chặn đứng rủi ro ảo giác (Hallucination)
            return converter.convert(response);

        } catch (Exception e) {
            log.error("Lỗi khi parse JSON từ Gemini AI: {}", e.getMessage());
            throw new RuntimeException("Không thể phân tích đơn thuốc lúc này do lỗi định dạng AI. Vui lòng thử lại sau.", e);
        }
    }
}
