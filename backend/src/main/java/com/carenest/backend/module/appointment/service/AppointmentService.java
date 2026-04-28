package com.carenest.backend.module.appointment.service;

import com.carenest.backend.module.appointment.dto.request.AppointmentCreateRequest;
import com.carenest.backend.module.appointment.dto.request.AppointmentMemberRequest;
import com.carenest.backend.module.appointment.dto.request.AppointmentNotesRequest;
import com.carenest.backend.module.appointment.dto.request.AppointmentUpdateRequest;
import com.carenest.backend.module.appointment.dto.response.AppointmentResponse;

import java.util.List;

public interface AppointmentService {
    AppointmentResponse createAppointment(AppointmentCreateRequest request);
    List<AppointmentResponse> getProfileAppointments(Long profileId);
    List<AppointmentResponse> getUpcomingAppointments(Long profileId);
    AppointmentResponse updateAppointment(Long id, AppointmentUpdateRequest request);
    AppointmentResponse cancelAppointment(Long id);
    AppointmentResponse assignMember(Long id, AppointmentMemberRequest request);
    String getNotes(Long id);
    AppointmentResponse updateNotes(Long id, AppointmentNotesRequest request);
}
