package com.carenest.backend.features.community.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateGroupPostRequest {

    @NotBlank(message = "Vui lÃ²ng nháº­p ná»™i dung")
    @Size(max = 4000, message = "Tin nháº¯n khÃ´ng Ä‘Æ°á»£c vÆ°á»£t quÃ¡ 4000 kÃ½ tá»±")
    private String content;

    private Long replyToPostId;

    @Size(max = 1000, message = "ÄÆ°á»ng dáº«n áº£nh khÃ´ng Ä‘Æ°á»£c vÆ°á»£t quÃ¡ 1000 kÃ½ tá»±")
    private String imageUrl;
}
