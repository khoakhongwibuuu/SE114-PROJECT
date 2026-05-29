package com.carenest.backend.features.media.service;

import com.carenest.backend.features.media.dto.response.MediaUploadResponse;
import org.springframework.web.multipart.MultipartFile;

public interface MediaService {
    MediaUploadResponse uploadImage(MultipartFile file, String category);
}
