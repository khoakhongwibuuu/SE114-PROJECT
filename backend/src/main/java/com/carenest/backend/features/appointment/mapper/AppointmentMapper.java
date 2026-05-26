package com.carenest.backend.features.appointment.mapper;

import com.carenest.backend.features.appointment.dto.response.AppointmentResponse;
import com.carenest.backend.features.appointment.entity.Appointment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AppointmentMapper {

    @Mapping(source = "healthProfile.id", target = "healthProfileId")
    AppointmentResponse toResponse(Appointment appointment);
}
