package com.carenest.backend.module.dashboard.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardTask {
    private String type; // "MEDICATION" or "VACCINATION"
    private String title;
    private String time;
    private String memberName;
    private Long referenceId;
}
