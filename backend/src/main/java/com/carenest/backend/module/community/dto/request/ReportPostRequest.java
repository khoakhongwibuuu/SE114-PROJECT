package com.carenest.backend.module.community.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ReportPostRequest {

    @NotBlank(message = "Vui lòng nhập lý do báo cáo")
    @Size(max = 500, message = "Lý do báo cáo không được vượt quá 500 ký tự")
    private String reason;
}
