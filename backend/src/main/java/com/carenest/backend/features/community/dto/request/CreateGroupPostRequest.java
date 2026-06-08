package com.carenest.backend.features.community.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateGroupPostRequest {

    @NotBlank(message = "Vui lòng nhập nội dung")
    @Size(max = 4000, message = "Tin nhắn không được vượt quá 4000 ký tự")
    private String content;

    private Long replyToPostId;

    @Size(max = 1000, message = "Đường dẫn ảnh không được vượt quá 1000 ký tự")
    private String imageUrl;
}
