package com.carenest.backend.features.booking.service;

import com.carenest.backend.features.booking.dto.request.CreateBookingRequest;
import com.carenest.backend.features.booking.dto.request.RejectBookingRequest;
import com.carenest.backend.features.booking.dto.response.BookingResponse;
import com.carenest.backend.features.booking.dto.response.ConsultationThreadInboxResponse;
import com.carenest.backend.features.booking.dto.response.ConsultationThreadResponse;

import java.util.List;

public interface BookingService {
    BookingResponse createBookingRequest(CreateBookingRequest request);
    List<BookingResponse> getDoctorBookings();
    List<BookingResponse> getPatientBookings();
    List<ConsultationThreadInboxResponse> getConsultationInbox();
    BookingResponse approveBooking(Long id);
    BookingResponse rejectBooking(Long id, RejectBookingRequest request);
    BookingResponse completeConsultation(Long id);
    BookingResponse restrictMessaging(Long id);
    BookingResponse unrestrictMessaging(Long id);
    ConsultationThreadResponse provisionConsultationThread(Long bookingId);
}
