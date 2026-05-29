package com.carenest.backend.features.doctorverification.service;

import com.carenest.backend.features.doctorverification.dto.request.RejectDoctorVerificationRequest;
import com.carenest.backend.features.doctorverification.dto.request.SubmitDoctorVerificationRequest;
import com.carenest.backend.features.doctorverification.dto.response.DoctorSummaryResponse;
import com.carenest.backend.features.doctorverification.dto.response.DoctorVerificationResponse;

import java.util.List;

public interface DoctorVerificationService {

    DoctorVerificationResponse submitRequest(SubmitDoctorVerificationRequest request);

    DoctorVerificationResponse getMyRequest();

    List<DoctorVerificationResponse> getPendingRequests();

    DoctorVerificationResponse approveRequest(Long requestId);

    DoctorVerificationResponse rejectRequest(Long requestId, RejectDoctorVerificationRequest request);

    List<DoctorSummaryResponse> getAllDoctors();

    void revokeDoctorRights(Long userId);
}
