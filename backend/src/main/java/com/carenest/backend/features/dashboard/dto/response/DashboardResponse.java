package com.carenest.backend.features.dashboard.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {
    @Schema(description = "Số lượng thông báo chưa đọc", example = "3")
    private long unreadNotifications;

    @Schema(description = "Danh sách công việc cần làm hôm nay (uống thuốc, tiêm chủng)")
    private List<DashboardTask> todayTasks;
}
