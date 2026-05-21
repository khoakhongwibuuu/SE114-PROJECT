package com.carenest.backend.module.community.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateGroupPostRequest {

    @NotBlank(message = "Vui lòng nhập nội dung")
    private String content;
}
