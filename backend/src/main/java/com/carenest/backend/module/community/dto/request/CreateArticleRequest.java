package com.carenest.backend.module.community.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateArticleRequest {

    @NotBlank(message = "Vui lòng nhập tiêu đề")
    @Size(max = 200, message = "Tiêu đề không được vượt quá 200 ký tự")
    private String title;

    @NotBlank(message = "Vui lòng nhập nội dung")
    private String content;

    @Size(max = 500, message = "Thẻ không được vượt quá 500 ký tự")
    private String tags;

    @Size(max = 1000, message = "Đường dẫn ảnh không được vượt quá 1000 ký tự")
    private String imageUrl;
}
