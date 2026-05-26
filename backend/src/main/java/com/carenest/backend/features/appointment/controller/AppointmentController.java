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
        return ApiResponse.success("Táº¡o lá»‹ch khÃ¡m thÃ nh cÃ´ng", response);
    }

    @GetMapping("/health-profiles/{id}/appointments")
    public ApiResponse<List<AppointmentResponse>> getProfileAppointments(@PathVariable("id") Long id) {
        List<AppointmentResponse> responses = appointmentService.getProfileAppointments(id);
        return ApiResponse.success("Láº¥y danh sÃ¡ch lá»‹ch khÃ¡m cá»§a há»“ sÆ¡ thÃ nh cÃ´ng", responses);
    }

    @GetMapping("/appointments/upcoming")
    public ApiResponse<List<AppointmentResponse>> getUpcomingAppointments(@RequestParam("profileId") Long profileId) {
        List<AppointmentResponse> responses = appointmentService.getUpcomingAppointments(profileId);
        return ApiResponse.success("Láº¥y danh sÃ¡ch lá»‹ch khÃ¡m sáº¯p tá»›i thÃ nh cÃ´ng", responses);
    }

    @PutMapping("/appointments/{id}")
    public ApiResponse<AppointmentResponse> updateAppointment(
            @PathVariable("id") Long id,
            @Valid @RequestBody AppointmentUpdateRequest request) {
        AppointmentResponse response = appointmentService.updateAppointment(id, request);
        return ApiResponse.success("Cáº­p nháº­t lá»‹ch khÃ¡m thÃ nh cÃ´ng", response);
    }

    @PutMapping("/appointments/{id}/cancel")
    public ApiResponse<AppointmentResponse> cancelAppointment(@PathVariable("id") Long id) {
        AppointmentResponse response = appointmentService.cancelAppointment(id);
        return ApiResponse.success("ÄÃ£ há»§y lá»‹ch khÃ¡m", response);
    }

    @PostMapping("/appointments/{id}/members")
    public ApiResponse<AppointmentResponse> assignMember(
            @PathVariable("id") Long id,
            @Valid @RequestBody AppointmentMemberRequest request) {
        AppointmentResponse response = appointmentService.assignMember(id, request);
        return ApiResponse.success("ÄÃ£ gÃ¡n thÃ nh viÃªn vÃ o lá»‹ch khÃ¡m", response);
    }

    @GetMapping("/appointments/{id}/notes")
    public ApiResponse<String> getNotes(@PathVariable("id") Long id) {
        String notes = appointmentService.getNotes(id);
        return ApiResponse.success("Láº¥y ghi chÃº káº¿t quáº£ khÃ¡m thÃ nh cÃ´ng", notes);
    }

    @PutMapping("/appointments/{id}/notes")
    public ApiResponse<AppointmentResponse> updateNotes(
            @PathVariable("id") Long id,
            @Valid @RequestBody AppointmentNotesRequest request) {
        AppointmentResponse response = appointmentService.updateNotes(id, request);
        return ApiResponse.success("Cáº­p nháº­t ghi chÃº káº¿t quáº£ khÃ¡m thÃ nh cÃ´ng", response);
    }
}
