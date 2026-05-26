package com.carenest.backend.features.community.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateArticleRequest {

    @NotBlank(message = "Vui lÃ²ng nháº­p tiÃªu Ä‘á»")
    @Size(max = 200, message = "TiÃªu Ä‘á» khÃ´ng Ä‘Æ°á»£c vÆ°á»£t quÃ¡ 200 kÃ½ tá»±")
    private String title;

    @NotBlank(message = "Vui lÃ²ng nháº­p ná»™i dung")
    private String content;

    @Size(max = 500, message = "Tháº» khÃ´ng Ä‘Æ°á»£c vÆ°á»£t quÃ¡ 500 kÃ½ tá»±")
    private String tags;

    @Size(max = 1000, message = "ÄÆ°á»ng dáº«n áº£nh khÃ´ng Ä‘Æ°á»£c vÆ°á»£t quÃ¡ 1000 kÃ½ tá»±")
    private String imageUrl;
}
