package com.carenest.backend.module.doctorverification.controller;

import com.carenest.backend.common.dto.ApiResponse;
import com.carenest.backend.module.doctorverification.dto.request.RejectDoctorVerificationRequest;
import com.carenest.backend.module.doctorverification.dto.response.DoctorSummaryResponse;
import com.carenest.backend.module.doctorverification.dto.response.DoctorVerificationResponse;
import com.carenest.backend.module.doctorverification.service.DoctorVerificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/doctors")
    public ApiResponse<List<DoctorSummaryResponse>> getAllDoctors() {
        return ApiResponse.success(doctorVerificationService.getAllDoctors());
    }

    @PatchMapping("/doctors/{userId}/revoke")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Void> revokeDoctor(@PathVariable("userId") Long userId) {
        doctorVerificationService.revokeDoctor(userId);
        return ApiResponse.success("Đã thu hồi quyền Bác sĩ", null);
    }
}

