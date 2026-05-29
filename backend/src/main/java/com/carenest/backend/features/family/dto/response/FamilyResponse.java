package com.carenest.backend.features.family.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FamilyResponse {
    private Long id;
    private String name;
    private Long ownerId;
    private Instant createdAt;
}
