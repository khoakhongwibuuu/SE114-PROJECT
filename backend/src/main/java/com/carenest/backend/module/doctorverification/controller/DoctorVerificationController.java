package com.carenest.backend.module.doctorverification.controller;

import com.carenest.backend.common.dto.ApiResponse;
import com.carenest.backend.module.doctorverification.dto.request.SubmitDoctorVerificationRequest;
import com.carenest.backend.module.doctorverification.dto.response.DoctorVerificationResponse;
import com.carenest.backend.module.doctorverification.service.DoctorVerificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/doctor-verifications")
@RequiredArgsConstructor
public class DoctorVerificationController {

    private final DoctorVerificationService doctorVerificationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<DoctorVerificationResponse> submitRequest(
            @Valid @RequestBody SubmitDoctorVerificationRequest request) {
        return ApiResponse.success("Doctor verification request submitted", doctorVerificationService.submitRequest(request));
    }

    @GetMapping("/me")
    public ApiResponse<DoctorVerificationResponse> getMyRequest() {
        return ApiResponse.success(doctorVerificationService.getMyRequest());
    }
}
