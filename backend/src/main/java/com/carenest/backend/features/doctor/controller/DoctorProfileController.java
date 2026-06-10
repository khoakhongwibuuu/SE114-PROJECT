package com.carenest.backend.features.doctor.controller;

import com.carenest.backend.core.api.ApiResponse;
import com.carenest.backend.features.doctor.dto.DoctorPublicProfileResponse;
import com.carenest.backend.features.doctor.service.DoctorProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/doctors")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('USER', 'DOCTOR', 'ADMIN')")
public class DoctorProfileController {

    private final DoctorProfileService doctorProfileService;

    @GetMapping("/{id}/profile")
    public ApiResponse<DoctorPublicProfileResponse> getDoctorProfile(@PathVariable("id") Long id) {
        return ApiResponse.success(doctorProfileService.getDoctorPublicProfile(id));
    }
}
