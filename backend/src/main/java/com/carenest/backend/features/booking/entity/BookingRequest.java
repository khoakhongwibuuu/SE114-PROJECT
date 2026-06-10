package com.carenest.backend.features.booking.entity;

import com.carenest.backend.core.entity.BaseEntity;
import com.carenest.backend.features.appointment.entity.Appointment;
import com.carenest.backend.features.auth.entity.User;
import com.carenest.backend.features.booking.enums.BookingRequestType;
import com.carenest.backend.features.booking.enums.BookingStatus;
import com.carenest.backend.features.healthprofile.entity.HealthProfile;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "booking_requests", indexes = {
        @Index(name = "idx_booking_requests_patient", columnList = "patient_id"),
        @Index(name = "idx_booking_requests_doctor", columnList = "doctor_id"),
        @Index(name = "idx_booking_requests_profile", columnList = "health_profile_id"),
        @Index(name = "idx_booking_requests_status", columnList = "status"),
        @Index(name = "idx_booking_requests_scheduled_at", columnList = "scheduled_at")
})

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingRequest extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private User patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private User doctor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "health_profile_id")
    private HealthProfile healthProfile;

    @Enumerated(EnumType.STRING)
    @Column(name = "request_type", nullable = false, length = 30)
    private BookingRequestType requestType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private BookingStatus status = BookingStatus.PENDING;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String note;

    @Column(name = "preferred_time_note", length = 500)
    private String preferredTimeNote;

    @Column(name = "reject_reason", length = 1000)
    private String rejectReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consultation_thread_id")
    private ConsultationThread thread;

    @Column(name = "scheduled_at")
    private Instant scheduledAt;

    @Column(name = "confirmed_location", length = 300)
    private String confirmedLocation;

    @Column(name = "confirmed_note", columnDefinition = "TEXT")
    private String confirmedNote;

    @Column(name = "cancellation_reason", columnDefinition = "TEXT")
    private String cancellationReason;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id")
    private Appointment appointment;
}
