package com.carenest.backend.module.community.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateArticleRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must be at most 200 characters")
    private String title;

    @NotBlank(message = "Content is required")
    private String content;

    @Size(max = 500, message = "Tags must be at most 500 characters")
    private String tags;
}
