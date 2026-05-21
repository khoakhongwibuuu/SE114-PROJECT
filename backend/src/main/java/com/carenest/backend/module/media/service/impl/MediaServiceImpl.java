package com.carenest.backend.module.media.service.impl;

import com.carenest.backend.common.exception.BadRequestException;
import com.carenest.backend.module.media.dto.response.MediaUploadResponse;
import com.carenest.backend.module.media.service.MediaService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MediaServiceImpl implements MediaService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    @Value("${app.media.upload-dir:uploads/media}")
    private String uploadDir;

    @Value("${app.media.max-image-size-bytes:5242880}")
    private long maxImageSizeBytes;

    @Override
    public MediaUploadResponse uploadImage(MultipartFile file, String category) {
        validateImage(file);

        String safeCategory = sanitizePathSegment(category == null || category.isBlank() ? "general" : category);
        LocalDate today = LocalDate.now();
        String extension = resolveExtension(file);
        String storedName = UUID.randomUUID() + extension;
        Path relativePath = Path.of(
                safeCategory,
                String.valueOf(today.getYear()),
                String.format("%02d", today.getMonthValue()),
                storedName
        );
        Path root = Path.of(uploadDir).toAbsolutePath().normalize();
        Path target = root.resolve(relativePath).normalize();

        if (!target.startsWith(root)) {
            throw new BadRequestException("Đường dẫn tải lên không hợp lệ");
        }

        try {
            Files.createDirectories(target.getParent());
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new BadRequestException("Không thể lưu tệp đã tải lên");
        }

        String url = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/media/files/")
                .path(relativePath.toString().replace("\\", "/"))
                .toUriString();

        return MediaUploadResponse.builder()
                .fileName(storedName)
                .contentType(file.getContentType())
                .size(file.getSize())
                .url(url)
                .build();
    }

    private void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Vui lòng chọn ảnh cần tải lên");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase(Locale.ROOT))) {
            throw new BadRequestException("Chỉ hỗ trợ ảnh JPEG, PNG hoặc WEBP");
        }

        if (file.getSize() > maxImageSizeBytes) {
            throw new BadRequestException("Ảnh tải lên quá lớn");
        }
    }

    private String resolveExtension(MultipartFile file) {
        String originalName = StringUtils.cleanPath(file.getOriginalFilename() == null ? "" : file.getOriginalFilename());
        int dotIndex = originalName.lastIndexOf('.');
        if (dotIndex >= 0 && dotIndex < originalName.length() - 1) {
            String extension = originalName.substring(dotIndex).toLowerCase(Locale.ROOT);
            if (extension.matches("\\.(jpg|jpeg|png|webp)")) {
                return extension;
            }
        }

        String contentType = file.getContentType();
        if ("image/png".equalsIgnoreCase(contentType)) {
            return ".png";
        }
        if ("image/webp".equalsIgnoreCase(contentType)) {
            return ".webp";
        }
        return ".jpg";
    }

    private String sanitizePathSegment(String value) {
        String sanitized = value.trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_-]", "-");
        return sanitized.isBlank() ? "general" : sanitized;
    }
}
