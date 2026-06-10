package com.carenest.backend.core.exception;

import com.carenest.backend.features.booking.enums.BookingStatus;
import lombok.Getter;

@Getter
public class DuplicateActiveConsultationException extends RuntimeException {
    private final Long existingBookingId;
    private final BookingStatus status;

    public DuplicateActiveConsultationException(String message, Long existingBookingId, BookingStatus status) {
        super(message);
        this.existingBookingId = existingBookingId;
        this.status = status;
    }
}
