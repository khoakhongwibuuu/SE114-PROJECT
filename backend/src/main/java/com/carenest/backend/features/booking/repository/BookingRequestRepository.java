package com.carenest.backend.features.booking.repository;

import com.carenest.backend.features.booking.entity.BookingRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.carenest.backend.features.booking.enums.BookingRequestType;
import com.carenest.backend.features.booking.enums.BookingStatus;

import java.util.List;

@Repository
public interface BookingRequestRepository extends JpaRepository<BookingRequest, Long> {
    List<BookingRequest> findAllByDoctorIdOrderByCreatedAtDesc(Long doctorId);
    List<BookingRequest> findAllByPatientIdOrderByCreatedAtDesc(Long patientId);

    java.util.Optional<BookingRequest> findFirstByPatientIdAndDoctorIdAndRequestTypeAndStatusInOrderByCreatedAtDesc(
        Long patientId, Long doctorId, BookingRequestType requestType, List<BookingStatus> statuses
    );

    java.util.Optional<BookingRequest> findFirstByThreadIdAndRequestTypeAndStatusInOrderByCreatedAtDesc(
        Long threadId, BookingRequestType requestType, List<BookingStatus> statuses
    );

    @Query("SELECT b FROM BookingRequest b WHERE (b.patient.id = :userId OR b.doctor.id = :userId) AND b.requestType = :requestType AND b.status IN :statuses ORDER BY b.updatedAt DESC")
    List<BookingRequest> findConsultationInboxForUser(
        @Param("userId") Long userId,
        @Param("requestType") BookingRequestType requestType,
        @Param("statuses") List<BookingStatus> statuses
    );
}
