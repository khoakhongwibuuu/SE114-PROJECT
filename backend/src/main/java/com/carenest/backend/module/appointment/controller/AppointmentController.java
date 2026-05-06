package com.carenest.backend.module.appointment.controller;

import com.carenest.backend.common.dto.ApiResponse;
import com.carenest.backend.module.appointment.dto.request.AppointmentCreateRequest;
import com.carenest.backend.module.appointment.dto.request.AppointmentMemberRequest;
import com.carenest.backend.module.appointment.dto.request.AppointmentNotesRequest;
import com.carenest.backend.module.appointment.dto.request.AppointmentUpdateRequest;
import com.carenest.backend.module.appointment.dto.response.AppointmentResponse;
import com.carenest.backend.module.appointment.service.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    @PostMapping("/appointments")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<AppointmentResponse> createAppointment(@Valid @RequestBody AppointmentCreateRequest request) {
        AppointmentResponse response = appointmentService.createAppointment(request);
        return ApiResponse.success("Created appointment successfully", response);
    }

    @GetMapping("/health-profiles/{id}/appointments")
    public ApiResponse<List<AppointmentResponse>> getProfileAppointments(@PathVariable Long id) {
        List<AppointmentResponse> responses = appointmentService.getProfileAppointments(id);
        return ApiResponse.success("Fetched profile appointments", responses);
    }

    @GetMapping("/appointments/upcoming")
    public ApiResponse<List<AppointmentResponse>> getUpcomingAppointments(@RequestParam Long profileId) {
        List<AppointmentResponse> responses = appointmentService.getUpcomingAppointments(profileId);
        return ApiResponse.success("Fetched upcoming appointments", responses);
    }

    @PutMapping("/appointments/{id}")
    public ApiResponse<AppointmentResponse> updateAppointment(
            @PathVariable Long id,
            @Valid @RequestBody AppointmentUpdateRequest request) {
        AppointmentResponse response = appointmentService.updateAppointment(id, request);
        return ApiResponse.success("Updated appointment successfully", response);
    }

    @PutMapping("/appointments/{id}/cancel")
    public ApiResponse<AppointmentResponse> cancelAppointment(@PathVariable Long id) {
        AppointmentResponse response = appointmentService.cancelAppointment(id);
        return ApiResponse.success("Cancelled appointment successfully", response);
    }

    @PostMapping("/appointments/{id}/members")
    public ApiResponse<AppointmentResponse> assignMember(
            @PathVariable Long id,
            @Valid @RequestBody AppointmentMemberRequest request) {
        AppointmentResponse response = appointmentService.assignMember(id, request);
        return ApiResponse.success("Assigned member to appointment successfully", response);
    }

    @GetMapping("/appointments/{id}/notes")
    public ApiResponse<String> getNotes(@PathVariable Long id) {
        String notes = appointmentService.getNotes(id);
        return ApiResponse.success("Fetched appointment result notes", notes);
    }

    @PutMapping("/appointments/{id}/notes")
    public ApiResponse<AppointmentResponse> updateNotes(
            @PathVariable Long id,
            @Valid @RequestBody AppointmentNotesRequest request) {
        AppointmentResponse response = appointmentService.updateNotes(id, request);
        return ApiResponse.success("Updated appointment result notes successfully", response);
    }
}
