package com.example.carenest.feature.dashboard.presentation

import com.example.carenest.feature.dashboard.domain.model.DashboardTask
import com.example.carenest.feature.family.domain.model.FamilyDetailResponse

internal fun buildFallbackDashboardTasks(
    familyDetail: FamilyDetailResponse,
    activeProfileId: Long?
): List<DashboardTask> {
    val targetMember = familyDetail.members.firstOrNull { it.profileId == activeProfileId }
    val targetName = targetMember?.fullName

    return listOf(
        DashboardTask(
            id = "family_overview",
            type = "FAMILY",
            title = "Gia đình đang hoạt động",
            subtitle = familyDetail.name,
            memberName = targetName,
            badge = if (targetName != null) "Đang theo dõi" else "Cả nhà"
        )
    )
}

internal fun decorateDashboardTask(task: DashboardTask): DashboardTask {
    val normalizedType = task.type?.uppercase()
    val fallbackSubtitle = listOfNotNull(task.memberName, task.description).joinToString(" • ")

    return when (normalizedType) {
        "MEDICATION" -> task.copy(
            subtitle = (task.subtitle ?: "").ifBlank { fallbackSubtitle.ifBlank { "Lịch uống thuốc hôm nay" } },
            icon = "pill",
            iconBgColor = 0xFFE0F2FE,
            iconColor = 0xFF0EA5E9,
            badge = task.badge ?: "Thuốc"
        )

        "VACCINATION" -> task.copy(
            subtitle = (task.subtitle ?: "").ifBlank { fallbackSubtitle.ifBlank { "Lịch tiêm chủng sắp tới" } },
            icon = "syringe",
            iconBgColor = 0xFFF3E8FF,
            iconColor = 0xFF8B5CF6,
            badge = task.badge ?: (task.subtitle ?: "").ifBlank { "Tiêm" }
        )

        "APPOINTMENT" -> task.copy(
            subtitle = (task.subtitle ?: "").ifBlank { fallbackSubtitle.ifBlank { "Lịch khám hôm nay" } },
            icon = "calendar_month",
            iconBgColor = 0xFFE0F7FA,
            iconColor = 0xFF0097A7,
            badge = task.badge ?: "Khám"
        )

        else -> task.copy(
            subtitle = (task.subtitle ?: "").ifBlank { fallbackSubtitle.ifBlank { "Việc cần theo dõi" } },
            icon = (task.icon ?: "").ifBlank { "check_circle" },
            iconBgColor = if (task.iconBgColor == 0xFFFFFFFF) 0xFFF1F5F9 else task.iconBgColor,
            iconColor = if (task.iconColor == 0xFF000000) 0xFF64748B else task.iconColor
        )
    }
}

internal fun buildDashboardHealthSummary(tasks: List<DashboardTask>): String {
    if (tasks.isEmpty()) {
        return "Hôm nay chưa có việc sức khỏe cần xử lý. Bạn vẫn nên kiểm tra lịch thuốc, lịch khám và tiêm chủng định kỳ."
    }

    val medicationCount = tasks.count { it.type?.uppercase() == "MEDICATION" }
    val vaccineCount = tasks.count { it.type?.uppercase() == "VACCINATION" }
    val appointmentCount = tasks.count { it.type?.uppercase() == "APPOINTMENT" }

    val parts = buildList {
        if (medicationCount > 0) add("$medicationCount lịch thuốc")
        if (vaccineCount > 0) add("$vaccineCount lịch tiêm")
        if (appointmentCount > 0) add("$appointmentCount lịch khám")
    }

    return if (parts.isEmpty()) {
        "Hôm nay gia đình có ${tasks.size} việc cần theo dõi. Hãy xử lý từng việc để không bỏ sót."
    } else {
        "Hôm nay gia đình có ${parts.joinToString(", ")} cần theo dõi. Hãy xử lý từng việc để không bỏ sót."
    }
}

internal fun dashboardRoleLabel(role: String): String {
    return when (role.uppercase()) {
        "OWNER" -> "Chủ hộ"
        "FATHER" -> "Bố"
        "MOTHER" -> "Mẹ"
        "OLDER_BROTHER" -> "Anh"
        "OLDER_SISTER" -> "Chị"
        "YOUNGER" -> "Em"
        else -> "Thành viên"
    }
}
