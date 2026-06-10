package com.carenest.backend.features.booking.entity;

import com.carenest.backend.core.entity.BaseEntity;
import com.carenest.backend.features.auth.entity.User;
import com.carenest.backend.features.booking.enums.BookingRequestType;
import com.carenest.backend.features.booking.enums.BookingStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "booking_requests")
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

    @Enumerated(EnumType.STRING)
    @Column(name = "request_type", nullable = false, length = 20)
    private BookingRequestType requestType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
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
}
