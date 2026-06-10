package com.carenest.backend.features.booking.controller;

import com.carenest.backend.core.api.ApiResponse;
import com.carenest.backend.features.booking.dto.request.CreateBookingRequest;
import com.carenest.backend.features.booking.dto.request.RejectBookingRequest;
import com.carenest.backend.features.booking.dto.response.BookingResponse;
import com.carenest.backend.features.booking.dto.response.ConsultationThreadInboxResponse;
import com.carenest.backend.features.booking.dto.response.ConsultationThreadResponse;
import com.carenest.backend.features.booking.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('USER', 'DOCTOR', 'ADMIN')")
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<BookingResponse> createBookingRequest(@Valid @RequestBody CreateBookingRequest request) {
        BookingResponse response = bookingService.createBookingRequest(request);
        return ApiResponse.success("Yêu cầu đã được gửi. Đang chờ bác sĩ xác nhận.", response);
    }

    @GetMapping("/doctor")
    @PreAuthorize("hasRole('DOCTOR')")
    public ApiResponse<List<BookingResponse>> getDoctorBookings() {
        return ApiResponse.success("Lấy danh sách yêu cầu khám thành công", bookingService.getDoctorBookings());
    }

    @GetMapping("/patient")
    @PreAuthorize("hasAnyRole('USER', 'DOCTOR')")
    public ApiResponse<List<BookingResponse>> getPatientBookings() {
        return ApiResponse.success("Lấy lịch sử đặt khám thành công", bookingService.getPatientBookings());
    }

    @GetMapping("/consultation-inbox")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<ConsultationThreadInboxResponse>> getConsultationInbox() {
        return ApiResponse.success("Lấy danh sách tin nhắn tư vấn thành công", bookingService.getConsultationInbox());
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('DOCTOR')")
    public ApiResponse<BookingResponse> approveBooking(@PathVariable("id") Long id) {
        return ApiResponse.success("Đã chấp nhận yêu cầu khám", bookingService.approveBooking(id));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('DOCTOR')")
    public ApiResponse<BookingResponse> rejectBooking(@PathVariable("id") Long id, @Valid @RequestBody RejectBookingRequest request) {
        return ApiResponse.success("Đã từ chối yêu cầu khám", bookingService.rejectBooking(id, request));
    }

    @PostMapping("/{id}/complete")
    @PreAuthorize("hasRole('DOCTOR')")
    public ApiResponse<BookingResponse> completeConsultation(@PathVariable("id") Long id) {
        try {
            return ApiResponse.success("Phiên tư vấn đã kết thúc", bookingService.completeConsultation(id));
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.<BookingResponse>builder()
                    .success(false)
                    .message("Lỗi 500: " + e.toString())
                    .build();
        }
    }

    @PostMapping("/{id}/restrict")
    @PreAuthorize("hasRole('DOCTOR')")
    public ApiResponse<BookingResponse> restrictMessaging(@PathVariable("id") Long id) {
        try {
            return ApiResponse.success("Đã hạn chế nhắn tin", bookingService.restrictMessaging(id));
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.<BookingResponse>builder()
                    .success(false)
                    .message("Lỗi 500: " + e.toString())
                    .build();
        }
    }

    @PostMapping("/{id}/consultation-thread")
    public ApiResponse<ConsultationThreadResponse> provisionConsultationThread(@PathVariable("id") Long id) {
        try {
            return ApiResponse.success("Đã lấy thông tin luồng tư vấn", bookingService.provisionConsultationThread(id));
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.<ConsultationThreadResponse>builder()
                    .success(false)
                    .message("Lỗi 500: " + e.toString())
                    .build();
        }
    }

    @PostMapping("/{id}/unrestrict")
    @PreAuthorize("hasRole('DOCTOR')")
    public ApiResponse<BookingResponse> unrestrictMessaging(@PathVariable("id") Long id) {
        try {
            return ApiResponse.success("Đã hủy hạn chế nhắn tin", bookingService.unrestrictMessaging(id));
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.<BookingResponse>builder()
                    .success(false)
                    .message("Lỗi 500: " + e.toString())
                    .build();
        }
    }
}
