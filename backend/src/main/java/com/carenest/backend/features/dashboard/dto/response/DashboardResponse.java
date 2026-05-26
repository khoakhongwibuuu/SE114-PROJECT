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
    @Schema(description = "Sá»‘ lÆ°á»£ng thÃ´ng bÃ¡o chÆ°a Ä‘á»c", example = "3")
    private long unreadNotifications;

    @Schema(description = "Danh sÃ¡ch cÃ´ng viá»‡c cáº§n lÃ m hÃ´m nay (uá»‘ng thuá»‘c, tiÃªm chá»§ng)")
    private List<DashboardTask> todayTasks;
}
