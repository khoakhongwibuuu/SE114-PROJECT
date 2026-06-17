package com.carenest.backend.features.aichat.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StructuredChatPayloadResponse {
    private String intent;
    private String summary;
    private List<String> advice;
    private String riskLevel;
    private List<String> followUpQuestions;
    private List<StructuredAiActionResponse> recommendedActions;
    private StructuredAiSafetyResponse safety;
}
