package com.carenest.backend.features.aichat.dto.response;

import com.fasterxml.jackson.annotation.JsonAlias;
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
    @JsonAlias("risk_level")
    private String riskLevel;
    @JsonAlias("follow_up_questions")
    private List<String> followUpQuestions;
    @JsonAlias("recommended_actions")
    private List<StructuredAiActionResponse> recommendedActions;
    private StructuredAiSafetyResponse safety;
}
