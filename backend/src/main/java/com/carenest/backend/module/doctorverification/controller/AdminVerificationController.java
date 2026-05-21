package com.carenest.backend.module.doctorverification.controller;

import com.carenest.backend.common.dto.ApiResponse;
import com.carenest.backend.module.doctorverification.dto.request.RejectDoctorVerificationRequest;
import com.carenest.backend.module.doctorverification.dto.response.DoctorVerificationResponse;
import com.carenest.backend.module.doctorverification.service.DoctorVerificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/doctor-verifications")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminVerificationController {

    private final DoctorVerificationService doctorVerificationService;

    @GetMapping("/pending")
    public ApiResponse<List<DoctorVerificationResponse>> getPendingRequests() {
        return ApiResponse.success(doctorVerificationService.getPendingRequests());
    }

    @PatchMapping("/{id}/approve")
    public ApiResponse<DoctorVerificationResponse> approveRequest(@PathVariable("id") Long id) {
        return ApiResponse.success("Đã phê duyệt hồ sơ bác sĩ", doctorVerificationService.approveRequest(id));
    }

    @PatchMapping("/{id}/reject")
    public ApiResponse<DoctorVerificationResponse> rejectRequest(
            @PathVariable("id") Long id,
            @Valid @RequestBody RejectDoctorVerificationRequest request) {
        return ApiResponse.success("Đã từ chối hồ sơ bác sĩ", doctorVerificationService.rejectRequest(id, request));
    }
}
