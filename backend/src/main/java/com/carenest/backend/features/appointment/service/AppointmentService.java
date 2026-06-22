package com.carenest.backend.features.appointment.service;

import com.carenest.backend.features.appointment.dto.request.AppointmentCreateRequest;
import com.carenest.backend.features.appointment.dto.request.AppointmentMemberRequest;
import com.carenest.backend.features.appointment.dto.request.AppointmentNotesRequest;
import com.carenest.backend.features.appointment.dto.request.AppointmentUpdateRequest;
import com.carenest.backend.features.appointment.dto.response.AppointmentResponse;

import java.util.List;

public interface AppointmentService {
    AppointmentResponse createAppointment(AppointmentCreateRequest request);
    List<AppointmentResponse> getProfileAppointments(Long profileId);
    List<AppointmentResponse> getUpcomingAppointments(Long profileId);
    AppointmentResponse updateAppointment(Long id, AppointmentUpdateRequest request);
    AppointmentResponse cancelAppointment(Long id);
    AppointmentResponse completeAppointment(Long id);
    AppointmentResponse assignMember(Long id, AppointmentMemberRequest request);
    String getNotes(Long id);
    AppointmentResponse updateNotes(Long id, AppointmentNotesRequest request);
}
