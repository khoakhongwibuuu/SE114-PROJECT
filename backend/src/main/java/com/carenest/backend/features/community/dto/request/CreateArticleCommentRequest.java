package com.carenest.backend.features.community.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateArticleCommentRequest {

    @NotBlank(message = "Vui lòng nhập bình luận")
    @Size(max = 2000, message = "Bình luận không được vượt quá 2000 ký tự")
    private String content;
}
