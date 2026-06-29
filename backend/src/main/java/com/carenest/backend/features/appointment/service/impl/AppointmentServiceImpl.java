package com.carenest.backend.features.appointment.service.impl;

import com.carenest.backend.core.exception.BadRequestException;
import com.carenest.backend.core.exception.ResourceNotFoundException;
import com.carenest.backend.features.appointment.dto.request.AppointmentCreateRequest;
import com.carenest.backend.features.appointment.dto.request.AppointmentMemberRequest;
import com.carenest.backend.features.appointment.dto.request.AppointmentNotesRequest;
import com.carenest.backend.features.appointment.dto.request.AppointmentUpdateRequest;
import com.carenest.backend.features.appointment.dto.response.AppointmentResponse;
import com.carenest.backend.features.appointment.entity.Appointment;
import com.carenest.backend.features.appointment.enums.AppointmentStatus;
import com.carenest.backend.features.appointment.mapper.AppointmentMapper;
import com.carenest.backend.features.appointment.repository.AppointmentRepository;
import com.carenest.backend.features.appointment.service.AppointmentService;
import com.carenest.backend.features.family.util.FamilySecurityUtil;
import com.carenest.backend.features.healthprofile.entity.HealthProfile;
import com.carenest.backend.features.healthprofile.repository.HealthProfileRepository;
import com.carenest.backend.features.booking.entity.BookingRequest;
import com.carenest.backend.features.booking.enums.BookingStatus;
import com.carenest.backend.features.booking.repository.BookingRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final HealthProfileRepository healthProfileRepository;
    private final AppointmentMapper appointmentMapper;
    private final FamilySecurityUtil familySecurityUtil;
    private final BookingRequestRepository bookingRequestRepository;

    @Override
    @Transactional
    public AppointmentResponse createAppointment(AppointmentCreateRequest request) {
        familySecurityUtil.checkCanWriteHealthProfile(request.getHealthProfileId());

        HealthProfile healthProfile = healthProfileRepository.findByIdAndDeletedAtIsNull(request.getHealthProfileId())
                .orElseThrow(() -> new ResourceNotFoundException("HealthProfile", "id", request.getHealthProfileId().toString()));

        Appointment appointment = Appointment.builder()
                .healthProfile(healthProfile)
                .doctorName(request.getDoctorName())
                .hospitalName(request.getHospitalName())
                .address(request.getAddress())
                .appointmentDate(request.getAppointmentDate())
                .notes(request.getNotes())
                .build();

        Appointment saved = appointmentRepository.save(appointment);
        return appointmentMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getProfileAppointments(Long profileId) {
        familySecurityUtil.checkCanReadHealthProfile(profileId);

        if (!healthProfileRepository.existsById(profileId)) {
            throw new ResourceNotFoundException("HealthProfile", "id", profileId.toString());
        }

        List<Appointment> appointments = appointmentRepository.findByHealthProfileIdOrderByAppointmentDateDesc(profileId);
        return appointments.stream()
                .map(appointmentMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getUpcomingAppointments(Long profileId) {
        familySecurityUtil.checkCanReadHealthProfile(profileId);

        if (!healthProfileRepository.existsById(profileId)) {
            throw new ResourceNotFoundException("HealthProfile", "id", profileId.toString());
        }

        List<Appointment> appointments = appointmentRepository.findByHealthProfileIdAndAppointmentDateAfterOrderByAppointmentDateAsc(profileId, Instant.now());
        return appointments.stream()
                .map(appointmentMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AppointmentResponse updateAppointment(Long id, AppointmentUpdateRequest request) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", "id", id.toString()));
        assertAppointmentAccess(appointment);

        if (request.getDoctorName() != null) appointment.setDoctorName(request.getDoctorName());
        if (request.getHospitalName() != null) appointment.setHospitalName(request.getHospitalName());
        if (request.getAddress() != null) appointment.setAddress(request.getAddress());
        if (request.getAppointmentDate() != null) appointment.setAppointmentDate(request.getAppointmentDate());
        if (request.getNotes() != null) appointment.setNotes(request.getNotes());

        Appointment saved = appointmentRepository.save(appointment);

        bookingRequestRepository.findByAppointment(saved).ifPresent(booking -> {
            if (booking.getStatus() != BookingStatus.COMPLETED
                    && booking.getStatus() != BookingStatus.REJECTED
                    && booking.getStatus() != BookingStatus.CANCELLED) {
                if (request.getAppointmentDate() != null) {
                    booking.setScheduledAt(request.getAppointmentDate());
                }
                if (request.getAddress() != null) {
                    booking.setConfirmedLocation(request.getAddress());
                }
                if (request.getNotes() != null) {
                    booking.setConfirmedNote(request.getNotes());
                }
                bookingRequestRepository.save(booking);
            }
        });

        return appointmentMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public AppointmentResponse cancelAppointment(Long id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", "id", id.toString()));
        assertAppointmentAccess(appointment);

        appointment.setStatus(AppointmentStatus.CANCELLED);
        Appointment saved = appointmentRepository.save(appointment);

        bookingRequestRepository.findByAppointment(saved).ifPresent(booking -> {
            if (booking.getStatus() != BookingStatus.COMPLETED
                    && booking.getStatus() != BookingStatus.REJECTED
                    && booking.getStatus() != BookingStatus.CANCELLED) {
                booking.setStatus(BookingStatus.CANCELLED);
                booking.setCancellationReason("Hủy thông qua lịch hẹn");
                bookingRequestRepository.save(booking);
            }
        });

        return appointmentMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public AppointmentResponse completeAppointment(Long id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", "id", id.toString()));
        assertAppointmentAccess(appointment);

        appointment.setStatus(AppointmentStatus.COMPLETED);
        Appointment saved = appointmentRepository.save(appointment);

        bookingRequestRepository.findByAppointment(saved).ifPresent(booking -> {
            if (booking.getStatus() != BookingStatus.REJECTED
                    && booking.getStatus() != BookingStatus.CANCELLED) {
                booking.setStatus(BookingStatus.COMPLETED);
                bookingRequestRepository.save(booking);
            }
        });

        return appointmentMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public AppointmentResponse assignMember(Long id, AppointmentMemberRequest request) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", "id", id.toString()));
        assertAppointmentAccess(appointment);
        familySecurityUtil.checkCanWriteHealthProfile(request.getHealthProfileId());

        HealthProfile healthProfile = healthProfileRepository.findByIdAndDeletedAtIsNull(request.getHealthProfileId())
                .orElseThrow(() -> new ResourceNotFoundException("HealthProfile", "id", request.getHealthProfileId().toString()));

        if (appointment.getHealthProfile().getFamily() != null
                && healthProfile.getFamily() != null
                && !appointment.getHealthProfile().getFamily().getId().equals(healthProfile.getFamily().getId())) {
            throw new BadRequestException("Không thể chuyển lịch khám sang gia đình khác");
        }

        appointment.setHealthProfile(healthProfile);
        Appointment saved = appointmentRepository.save(appointment);
        return appointmentMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public String getNotes(Long id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", "id", id.toString()));
        assertAppointmentAccess(appointment);
        return appointment.getResultNotes();
    }

    @Override
    @Transactional
    public AppointmentResponse updateNotes(Long id, AppointmentNotesRequest request) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", "id", id.toString()));
        assertAppointmentAccess(appointment);

        appointment.setResultNotes(request.getResultNotes());
        appointment.setStatus(AppointmentStatus.COMPLETED); // Assuming adding result notes means it's completed
        Appointment saved = appointmentRepository.save(appointment);

        bookingRequestRepository.findByAppointment(saved).ifPresent(booking -> {
            if (booking.getStatus() == BookingStatus.APPROVED
                    || booking.getStatus() == BookingStatus.ACTIVE
                    || booking.getStatus() == BookingStatus.RESTRICTED) {
                booking.setStatus(BookingStatus.COMPLETED);
            }
            if (booking.getStatus() != BookingStatus.REJECTED && booking.getStatus() != BookingStatus.CANCELLED) {
                booking.setConfirmedNote(request.getResultNotes());
                bookingRequestRepository.save(booking);
            }
        });

        return appointmentMapper.toResponse(saved);
    }

    private void assertAppointmentAccess(Appointment appointment) {
        familySecurityUtil.checkCanWriteHealthProfile(appointment.getHealthProfile().getId());
    }
}
