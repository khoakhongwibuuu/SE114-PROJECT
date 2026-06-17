package com.carenest.backend.features.appointment.controller;

import com.carenest.backend.core.api.ApiResponse;
import com.carenest.backend.features.appointment.dto.request.AppointmentCreateRequest;
import com.carenest.backend.features.appointment.dto.request.AppointmentMemberRequest;
import com.carenest.backend.features.appointment.dto.request.AppointmentNotesRequest;
import com.carenest.backend.features.appointment.dto.request.AppointmentUpdateRequest;
import com.carenest.backend.features.appointment.dto.response.AppointmentResponse;
import com.carenest.backend.features.appointment.service.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("")
@RequiredArgsConstructor
@org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('USER', 'DOCTOR', 'ADMIN')")
public class AppointmentController {

    private final AppointmentService appointmentService;

    @PostMapping("/appointments")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<AppointmentResponse> createAppointment(@Valid @RequestBody AppointmentCreateRequest request) {
        AppointmentResponse response = appointmentService.createAppointment(request);
        return ApiResponse.success("Tạo lịch khám thành công", response);
    }

    @GetMapping("/health-profiles/{id}/appointments")
    public ApiResponse<List<AppointmentResponse>> getProfileAppointments(@PathVariable("id") Long id) {
        List<AppointmentResponse> responses = appointmentService.getProfileAppointments(id);
        return ApiResponse.success("Lấy danh sách lịch khám của hồ sơ thành công", responses);
    }

    @GetMapping("/appointments/upcoming")
    public ApiResponse<List<AppointmentResponse>> getUpcomingAppointments(@RequestParam("profileId") Long profileId) {
        List<AppointmentResponse> responses = appointmentService.getUpcomingAppointments(profileId);
        return ApiResponse.success("Lấy danh sách lịch khám sắp tới thành công", responses);
    }

    @PutMapping("/appointments/{id}")
    public ApiResponse<AppointmentResponse> updateAppointment(
            @PathVariable("id") Long id,
            @Valid @RequestBody AppointmentUpdateRequest request) {
        AppointmentResponse response = appointmentService.updateAppointment(id, request);
        return ApiResponse.success("Cập nhật lịch khám thành công", response);
    }

    @PutMapping("/appointments/{id}/cancel")
    public ApiResponse<AppointmentResponse> cancelAppointment(@PathVariable("id") Long id) {
        AppointmentResponse response = appointmentService.cancelAppointment(id);
        return ApiResponse.success("Đã hủy lịch khám", response);
    }

    @PostMapping("/appointments/{id}/members")
    public ApiResponse<AppointmentResponse> assignMember(
            @PathVariable("id") Long id,
            @Valid @RequestBody AppointmentMemberRequest request) {
        AppointmentResponse response = appointmentService.assignMember(id, request);
        return ApiResponse.success("Đã gán thành viên vào lịch khám", response);
    }

    @GetMapping("/appointments/{id}/notes")
    public ApiResponse<String> getNotes(@PathVariable("id") Long id) {
        String notes = appointmentService.getNotes(id);
        return ApiResponse.success("Lấy ghi chú kết quả khám thành công", notes);
    }

    @PutMapping("/appointments/{id}/notes")
    public ApiResponse<AppointmentResponse> updateNotes(
            @PathVariable("id") Long id,
            @Valid @RequestBody AppointmentNotesRequest request) {
        AppointmentResponse response = appointmentService.updateNotes(id, request);
        return ApiResponse.success("Cập nhật ghi chú kết quả khám thành công", response);
    }
}
