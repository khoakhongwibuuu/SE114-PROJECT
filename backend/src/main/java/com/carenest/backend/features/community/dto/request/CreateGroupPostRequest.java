package com.carenest.backend.features.community.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateGroupPostRequest {

    @NotBlank(message = "Tiêu đề không được để trống")
    @Size(max = 500, message = "Tiêu đề không được vượt quá 500 ký tự")
    private String title;

    @NotBlank(message = "Nội dung không được để trống")
    @Size(max = 4000, message = "Nội dung không được vượt quá 4000 ký tự")
    private String content;

    private Long replyToPostId;

    @Size(max = 1000, message = "Đường dẫn ảnh không được vượt quá 1000 ký tự")
    private String imageUrl;

    @Size(max = 500, message = "Tags không được vượt quá 500 ký tự")
    private String tags;
}
