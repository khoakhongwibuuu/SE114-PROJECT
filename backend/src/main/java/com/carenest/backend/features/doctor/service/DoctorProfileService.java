package com.carenest.backend.features.doctor.service;

import com.carenest.backend.features.doctor.dto.DoctorPublicProfileResponse;

public interface DoctorProfileService {
    DoctorPublicProfileResponse getDoctorPublicProfile(Long doctorId);
}
