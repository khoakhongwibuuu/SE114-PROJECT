package com.carenest.backend.module.doctorverification.service;

import com.carenest.backend.module.doctorverification.dto.request.RejectDoctorVerificationRequest;
import com.carenest.backend.module.doctorverification.dto.request.SubmitDoctorVerificationRequest;
import com.carenest.backend.module.doctorverification.dto.response.DoctorVerificationResponse;

import java.util.List;

public interface DoctorVerificationService {

    DoctorVerificationResponse submitRequest(SubmitDoctorVerificationRequest request);

    DoctorVerificationResponse getMyRequest();

    List<DoctorVerificationResponse> getPendingRequests();

    DoctorVerificationResponse approveRequest(Long requestId);

    DoctorVerificationResponse rejectRequest(Long requestId, RejectDoctorVerificationRequest request);
}
