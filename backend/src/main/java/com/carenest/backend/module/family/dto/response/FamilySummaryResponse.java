package com.carenest.backend.module.family.dto.response;

import com.carenest.backend.module.family.enums.FamilyRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Lightweight summary of a single family, used by the my-list endpoint.
 * Avoids the overhead of FamilyDetailResponse (which loads all members).
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FamilySummaryResponse {
    private Long       id;
    private String     name;
    private int        memberCount;
    private FamilyRole myRole;
    private String     ownerName;
}
