package com.example.carenest.core.presentation.navigation

import com.example.carenest.feature.auth.domain.model.AppRole
import com.example.carenest.feature.main.presentation.MainTabTarget
import com.example.carenest.feature.notifications.domain.model.NotificationItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationNavigationResolverTest {

    @Test
    fun resolveNotificationNavigationPlan_mapsAllCurrentBackendReferenceTypes() {
        assertEquals(
            NotificationNavigationPlan.OpenDoctorWorkspace,
            resolveNotificationNavigationPlan(notification(referenceType = "BOOKING_WORKSPACE"), AppRole.DOCTOR)
        )
        assertEquals(
            NotificationNavigationPlan.OpenPatientBookingCenter,
            resolveNotificationNavigationPlan(notification(referenceType = "BOOKING_HISTORY"), AppRole.USER)
        )
        assertEquals(
            NotificationNavigationPlan.OpenConsultationRoom(501L),
            resolveNotificationNavigationPlan(notification(referenceType = "CONSULTATION_ROOM", referenceId = 501L), AppRole.DOCTOR)
        )
        assertEquals(
            NotificationNavigationPlan.OpenDoctorWorkspace,
            resolveNotificationNavigationPlan(notification(referenceType = "BOOKING_REQUEST"), AppRole.DOCTOR)
        )
        assertEquals(
            NotificationNavigationPlan.OpenDoctorVerification,
            resolveNotificationNavigationPlan(notification(referenceType = "DOCTOR_VERIFICATION"), AppRole.USER)
        )
        assertEquals(
            NotificationNavigationPlan.ConsumeAdminAccountUpdate,
            resolveNotificationNavigationPlan(notification(referenceType = "ADMIN_USER_STATUS"), AppRole.USER)
        )
        assertEquals(
            NotificationNavigationPlan.ConsumeAdminAccountUpdate,
            resolveNotificationNavigationPlan(notification(referenceType = "ADMIN_USER_ROLE"), AppRole.USER)
        )
        assertEquals(
            NotificationNavigationPlan.OpenMainTab(MainTabTarget.FAMILY),
            resolveNotificationNavigationPlan(notification(referenceType = "FAMILY"), AppRole.USER)
        )
        assertEquals(
            NotificationNavigationPlan.OpenMainTab(MainTabTarget.FAMILY),
            resolveNotificationNavigationPlan(notification(referenceType = "FAMILY_INVITATION"), AppRole.USER)
        )
        assertEquals(
            NotificationNavigationPlan.OpenMainTab(MainTabTarget.FAMILY),
            resolveNotificationNavigationPlan(notification(referenceType = "FAMILY_CHAT"), AppRole.USER)
        )
        assertEquals(
            NotificationNavigationPlan.OpenMedicineSchedule,
            resolveNotificationNavigationPlan(notification(referenceType = "MEDICATION_LOG"), AppRole.USER)
        )
        assertEquals(
            NotificationNavigationPlan.OpenMainTab(MainTabTarget.HOME),
            resolveNotificationNavigationPlan(notification(referenceType = "APPOINTMENT"), AppRole.USER)
        )
        assertEquals(
            NotificationNavigationPlan.OpenMainTab(MainTabTarget.PROFILE),
            resolveNotificationNavigationPlan(notification(referenceType = "GROWTH_RECORD"), AppRole.USER)
        )
    }

    @Test
    fun resolveNotificationNavigationPlan_usesTypeFallbackWhenReferenceTypeMissing() {
        assertEquals(
            NotificationNavigationPlan.OpenMainTab(MainTabTarget.FAMILY),
            resolveNotificationNavigationPlan(notification(type = "FAMILY"), AppRole.USER)
        )
        assertEquals(
            NotificationNavigationPlan.OpenMedicineSchedule,
            resolveNotificationNavigationPlan(notification(type = "MEDICATION"), AppRole.USER)
        )
        assertEquals(
            NotificationNavigationPlan.OpenMainTab(MainTabTarget.HOME),
            resolveNotificationNavigationPlan(notification(type = "APPOINTMENT"), AppRole.USER)
        )
        assertEquals(
            NotificationNavigationPlan.OpenMainTab(MainTabTarget.PROFILE),
            resolveNotificationNavigationPlan(notification(type = "GROWTH"), AppRole.USER)
        )
    }

    @Test
    fun resolveNotificationNavigationPlan_returnsUnhandledForWrongRoleOrMissingBookingId() {
        assertEquals(
            NotificationNavigationPlan.Unhandled,
            resolveNotificationNavigationPlan(notification(referenceType = "BOOKING_WORKSPACE"), AppRole.USER)
        )
        assertEquals(
            NotificationNavigationPlan.Unhandled,
            resolveNotificationNavigationPlan(notification(referenceType = "CONSULTATION_ROOM", referenceId = null), AppRole.DOCTOR)
        )
        assertEquals(
            NotificationNavigationPlan.Unhandled,
            resolveNotificationNavigationPlan(notification(referenceType = "CONSULTATION_ROOM", referenceId = 0L), AppRole.DOCTOR)
        )
        assertEquals(
            NotificationNavigationPlan.Unhandled,
            resolveNotificationNavigationPlan(notification(referenceType = "UNKNOWN_REF", type = "UNKNOWN_TYPE"), AppRole.USER)
        )
    }

    @Test
    fun isBookingScopedNotification_detectsBookingReferenceTypes() {
        assertTrue(isBookingScopedNotification("BOOKING_REQUEST"))
        assertTrue(isBookingScopedNotification("BOOKING_WORKSPACE"))
        assertTrue(isBookingScopedNotification("BOOKING_HISTORY"))
        assertTrue(isBookingScopedNotification("CONSULTATION_ROOM"))
    }

    private fun notification(
        type: String = "SYSTEM",
        referenceType: String? = null,
        referenceId: Long? = null
    ): NotificationItem {
        return NotificationItem(
            id = 1L,
            userId = 1L,
            title = "title",
            message = "message",
            type = type,
            referenceType = referenceType,
            referenceId = referenceId,
            isRead = false,
            createdAt = null
        )
    }
}
