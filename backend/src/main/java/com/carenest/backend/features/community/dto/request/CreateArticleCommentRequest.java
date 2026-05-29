package com.carenest.backend.features.community.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateArticleCommentRequest {

    @NotBlank(message = "Vui lÃ²ng nháº­p bÃ¬nh luáº­n")
    @Size(max = 2000, message = "BÃ¬nh luáº­n khÃ´ng Ä‘Æ°á»£c vÆ°á»£t quÃ¡ 2000 kÃ½ tá»±")
    private String content;
}
