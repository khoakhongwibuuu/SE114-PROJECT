package com.carenest.backend.module.doctorverification.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SubmitDoctorVerificationRequest {

    @NotBlank(message = "Vui lòng nhập số chứng chỉ hành nghề")
    @Size(max = 100, message = "Số chứng chỉ hành nghề không được vượt quá 100 ký tự")
    private String certificationNumber;

    @NotBlank(message = "Vui lòng nhập chuyên khoa")
    @Size(max = 100, message = "Chuyên khoa không được vượt quá 100 ký tự")
    private String specialty;

    @NotBlank(message = "Vui lòng nhập bệnh viện hoặc phòng khám")
    @Size(max = 200, message = "Tên bệnh viện hoặc phòng khám không được vượt quá 200 ký tự")
    private String hospitalName;

    @NotBlank(message = "Vui lòng tải lên ảnh chứng chỉ")
    @Size(max = 1000, message = "Đường dẫn tài liệu không được vượt quá 1000 ký tự")
    @Pattern(regexp = "^https?://[^\\s<>\"']+$", message = "Đường dẫn tài liệu phải là URL HTTP hoặc HTTPS hợp lệ")
    private String documentUrl;
}
