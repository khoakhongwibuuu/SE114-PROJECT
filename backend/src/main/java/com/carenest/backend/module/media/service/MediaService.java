package com.carenest.backend.module.media.service;

import com.carenest.backend.module.media.dto.response.MediaUploadResponse;
import org.springframework.web.multipart.MultipartFile;

public interface MediaService {
    MediaUploadResponse uploadImage(MultipartFile file, String category);
}
