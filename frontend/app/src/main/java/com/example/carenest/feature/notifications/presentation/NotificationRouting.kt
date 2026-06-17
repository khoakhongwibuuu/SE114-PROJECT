package com.example.carenest.feature.notifications.presentation

import com.example.carenest.feature.auth.domain.model.AppRole
import com.example.carenest.feature.notifications.domain.model.NotificationItem

internal enum class NotificationDestination {
    NONE,
    DOCTOR_VERIFICATION,
    DOCTOR_WORKSPACE,
    PATIENT_BOOKINGS,
    FAMILY_TAB,
    MEDICINE_SCHEDULE,
    HOME_TAB,
    PROFILE_TAB
}

internal data class NotificationRoutingDecision(
    val destination: NotificationDestination,
    val shouldRefreshUser: Boolean = false,
    val consumeOnly: Boolean = false
)

internal fun resolveNotificationRouting(
    notification: NotificationItem,
    role: AppRole?
): NotificationRoutingDecision {
    val referenceType = notification.referenceType?.trim()?.uppercase()
    val notificationType = notification.type.trim().uppercase()

    return when (referenceType) {
        "BOOKING_REQUEST" -> NotificationRoutingDecision(
            destination = if (role == AppRole.DOCTOR) {
                NotificationDestination.DOCTOR_WORKSPACE
            } else {
                NotificationDestination.PATIENT_BOOKINGS
            }
        )

        "DOCTOR_VERIFICATION" -> NotificationRoutingDecision(
            destination = NotificationDestination.DOCTOR_VERIFICATION,
            shouldRefreshUser = true
        )

        "ADMIN_USER_ROLE",
        "ADMIN_USER_STATUS" -> NotificationRoutingDecision(
            destination = NotificationDestination.NONE,
            shouldRefreshUser = true,
            consumeOnly = true
        )

        "FAMILY",
        "FAMILY_INVITATION",
        "FAMILY_CHAT" -> NotificationRoutingDecision(NotificationDestination.FAMILY_TAB)

        "MEDICATION_LOG" -> NotificationRoutingDecision(NotificationDestination.MEDICINE_SCHEDULE)

        "APPOINTMENT" -> NotificationRoutingDecision(NotificationDestination.HOME_TAB)

        "GROWTH_RECORD" -> NotificationRoutingDecision(NotificationDestination.PROFILE_TAB)

        else -> when (notificationType) {
            "FAMILY" -> NotificationRoutingDecision(NotificationDestination.FAMILY_TAB)
            "MEDICATION" -> NotificationRoutingDecision(NotificationDestination.MEDICINE_SCHEDULE)
            "APPOINTMENT" -> NotificationRoutingDecision(
                destination = if (role == AppRole.DOCTOR) {
                    NotificationDestination.DOCTOR_WORKSPACE
                } else {
                    NotificationDestination.HOME_TAB
                }
            )

            "GROWTH" -> NotificationRoutingDecision(NotificationDestination.PROFILE_TAB)
            "CHAT" -> NotificationRoutingDecision(
                destination = if (role == AppRole.DOCTOR) {
                    NotificationDestination.DOCTOR_WORKSPACE
                } else {
                    NotificationDestination.FAMILY_TAB
                }
            )

            else -> NotificationRoutingDecision(NotificationDestination.NONE)
        }
    }
}
