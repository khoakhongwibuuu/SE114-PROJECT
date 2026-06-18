package com.carenest.backend.features.aichat.dto.response;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StructuredAiSafetyResponse {
    @JsonAlias("needs_doctor")
    private boolean needsDoctor;
    @JsonAlias("needs_emergency")
    private boolean needsEmergency;
    private String disclaimer;
}
