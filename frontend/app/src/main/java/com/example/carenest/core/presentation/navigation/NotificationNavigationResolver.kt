package com.example.carenest.core.presentation.navigation

import com.example.carenest.feature.auth.domain.model.AppRole
import com.example.carenest.feature.main.presentation.MainTabTarget
import com.example.carenest.feature.notifications.domain.model.NotificationItem

internal sealed interface NotificationNavigationPlan {
    data class OpenMainTab(val target: MainTabTarget) : NotificationNavigationPlan
    data object OpenDoctorWorkspace : NotificationNavigationPlan
    data object OpenPatientBookingCenter : NotificationNavigationPlan
    data object OpenMedicineSchedule : NotificationNavigationPlan
    data class OpenConsultationRoom(val bookingId: Long) : NotificationNavigationPlan
    data object OpenDoctorVerification : NotificationNavigationPlan
    data object ConsumeAdminAccountUpdate : NotificationNavigationPlan
    data object Unhandled : NotificationNavigationPlan
}

internal fun resolveNotificationNavigationPlan(
    notification: NotificationItem,
    role: AppRole?
): NotificationNavigationPlan {
    val referenceType = notification.referenceType?.uppercase()
    return when (referenceType) {
        "BOOKING_WORKSPACE" -> {
            if (role == AppRole.DOCTOR) NotificationNavigationPlan.OpenDoctorWorkspace
            else NotificationNavigationPlan.Unhandled
        }

        "BOOKING_HISTORY" -> {
            if (role == AppRole.USER || role == AppRole.DOCTOR) {
                NotificationNavigationPlan.OpenPatientBookingCenter
            } else {
                NotificationNavigationPlan.Unhandled
            }
        }

        "CONSULTATION_ROOM" -> {
            val bookingId = notification.referenceId
            if ((role == AppRole.USER || role == AppRole.DOCTOR) && bookingId != null && bookingId > 0L) {
                NotificationNavigationPlan.OpenConsultationRoom(bookingId)
            } else {
                NotificationNavigationPlan.Unhandled
            }
        }

        "BOOKING_REQUEST" -> when (role) {
            AppRole.DOCTOR -> NotificationNavigationPlan.OpenDoctorWorkspace
            AppRole.USER -> NotificationNavigationPlan.OpenPatientBookingCenter
            else -> NotificationNavigationPlan.Unhandled
        }

        "DOCTOR_VERIFICATION" -> NotificationNavigationPlan.OpenDoctorVerification
        "ADMIN_USER_ROLE", "ADMIN_USER_STATUS" -> NotificationNavigationPlan.ConsumeAdminAccountUpdate
        "FAMILY", "FAMILY_INVITATION", "FAMILY_CHAT" -> NotificationNavigationPlan.OpenMainTab(MainTabTarget.FAMILY)
        "MEDICATION_LOG" -> NotificationNavigationPlan.OpenMedicineSchedule
        "APPOINTMENT" -> NotificationNavigationPlan.OpenMainTab(MainTabTarget.HOME)
        "GROWTH_RECORD" -> NotificationNavigationPlan.OpenMainTab(MainTabTarget.PROFILE)
        else -> resolveNotificationTypeFallback(notification.type)
    }
}

internal fun resolveNotificationTypeFallback(type: String?): NotificationNavigationPlan {
    return when (type?.uppercase()) {
        "FAMILY" -> NotificationNavigationPlan.OpenMainTab(MainTabTarget.FAMILY)
        "MEDICATION" -> NotificationNavigationPlan.OpenMedicineSchedule
        "APPOINTMENT" -> NotificationNavigationPlan.OpenMainTab(MainTabTarget.HOME)
        "GROWTH" -> NotificationNavigationPlan.OpenMainTab(MainTabTarget.PROFILE)
        else -> NotificationNavigationPlan.Unhandled
    }
}

internal fun isBookingScopedNotification(referenceType: String?): Boolean {
    return when (referenceType?.uppercase()) {
        "BOOKING_REQUEST",
        "BOOKING_WORKSPACE",
        "BOOKING_HISTORY",
        "CONSULTATION_ROOM" -> true
        else -> false
    }
}
