package com.carenest.backend.features.booking.service;

import com.carenest.backend.features.booking.dto.request.CancelBookingRequest;
import com.carenest.backend.features.booking.dto.request.ConfirmBookingScheduleRequest;
import com.carenest.backend.features.booking.dto.request.CreateBookingRequest;
import com.carenest.backend.features.booking.dto.request.RejectBookingRequest;
import com.carenest.backend.features.booking.dto.response.BookingResponse;
import com.carenest.backend.features.booking.dto.response.ConsultationThreadInboxResponse;
import com.carenest.backend.features.booking.dto.response.ConsultationThreadResponse;
import com.carenest.backend.features.doctorverification.dto.response.DoctorSummaryResponse;

import java.util.List;

public interface BookingService {
    List<DoctorSummaryResponse> getAvailableDoctors();
    BookingResponse createBookingRequest(CreateBookingRequest request);
    List<BookingResponse> getDoctorBookings();
    List<BookingResponse> getPatientBookings();
    List<ConsultationThreadInboxResponse> getConsultationInbox();
    BookingResponse approveBooking(Long id);
    BookingResponse confirmSchedule(Long bookingId, ConfirmBookingScheduleRequest request);
    BookingResponse rejectBooking(Long id, RejectBookingRequest request);
    BookingResponse cancelBooking(Long bookingId, CancelBookingRequest request);
    BookingResponse completeConsultation(Long id);
    BookingResponse restrictMessaging(Long id);
    BookingResponse unrestrictMessaging(Long id);
    ConsultationThreadResponse provisionConsultationThread(Long bookingId);
}
