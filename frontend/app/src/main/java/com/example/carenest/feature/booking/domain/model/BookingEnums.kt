package com.example.carenest.feature.booking.domain.model

enum class BookingRequestType {
    ONLINE_CHAT,
    OFFLINE_CLINIC
}

enum class BookingStatus {
    PENDING,
    APPROVED,
    REJECTED,
    CANCELLED,
    ACTIVE,
    COMPLETED,
    RESTRICTED
}
