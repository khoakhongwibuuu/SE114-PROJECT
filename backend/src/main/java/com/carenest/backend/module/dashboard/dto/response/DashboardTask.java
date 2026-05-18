package com.carenest.backend.module.dashboard.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardTask {
    @Schema(description = "Loại công việc", example = "MEDICATION")
    private String type; // "MEDICATION" or "VACCINATION"
    
    @Schema(description = "Tiêu đề công việc", example = "Paracetamol 500mg")
    private String title;
    
    @Schema(description = "Thời gian thực hiện", example = "2023-10-27T08:00:00Z")
    private String time;
    
    @Schema(description = "Tên thành viên gia đình", example = "Bé Na")
    private String memberName;
    
    @Schema(description = "ID tham chiếu của công việc (MedicationLog ID hoặc VaccinationDose ID)", example = "123")
    private Long referenceId;

    @Schema(description = "ID hồ sơ sức khỏe của thành viên", example = "3")
    private Long profileId;

    @Schema(description = "Mô tả phụ hoặc thẻ nhắc nhở", example = "⏳ Ngày mai")
    private String subtitle;
}
