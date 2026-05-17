package com.carenest.backend.module.family.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FamilyJoinCodeResponse {
    private Long id;
    private String name;
    private String joinCode;
    private String joinLink;
    private String qrCodeBase64;
    private Instant expiresAt;
}
