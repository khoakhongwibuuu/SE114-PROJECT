package com.carenest.backend.features.vaccination.service;

import com.carenest.backend.features.vaccination.dto.request.AdministerDoseRequest;
import com.carenest.backend.features.vaccination.dto.request.CreateVaccinationRequest;
import com.carenest.backend.features.vaccination.dto.response.VaccinationRecordResponse;

import java.util.List;

public interface VaccinationService {

    VaccinationRecordResponse createVaccinationPlan(Long profileId, CreateVaccinationRequest request);

    VaccinationRecordResponse administerDose(Long doseId, AdministerDoseRequest request);

    List<VaccinationRecordResponse> getVaccinationHistory(Long profileId);
}
