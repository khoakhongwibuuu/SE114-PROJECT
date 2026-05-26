package com.carenest.backend.features.media.controller;

import com.carenest.backend.core.api.ApiResponse;
import com.carenest.backend.features.media.dto.response.MediaUploadResponse;
import com.carenest.backend.features.media.service.MediaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/media")
@RequiredArgsConstructor
@org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('USER', 'DOCTOR', 'ADMIN')")
public class MediaController {

    private final MediaService mediaService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<MediaUploadResponse> uploadImage(
            @RequestPart("file") MultipartFile file,
            @RequestParam(value = "category", required = false) String category) {
        return ApiResponse.success("Táº£i áº£nh lÃªn thÃ nh cÃ´ng", mediaService.uploadImage(file, category));
    }
}
