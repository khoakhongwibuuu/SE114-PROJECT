package com.carenest.backend.features.community.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class AdminReportSummaryResponse {
    private Long id;
    private Long postId;
    private Long messageId;
    private Long commentId;
    private String contentType;
    private Long reporterId;
    private String reporterName;
    private String reporterEmail;
    private String reason;
    private String previewText;
    private String previewImageUrl;
    private String contentAuthorName;
    private String status;
    private Instant createdAt;
}
