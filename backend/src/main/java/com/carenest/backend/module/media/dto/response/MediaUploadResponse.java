package com.carenest.backend.module.media.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MediaUploadResponse {
    private String fileName;
    private String contentType;
    private long size;
    private String url;
}
