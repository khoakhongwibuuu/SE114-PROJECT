package com.carenest.backend.module.vaccination.service;

import com.carenest.backend.module.vaccination.dto.request.AdministerDoseRequest;
import com.carenest.backend.module.vaccination.dto.request.CreateVaccinationRequest;
import com.carenest.backend.module.vaccination.dto.response.VaccinationRecordResponse;

import java.util.List;

public interface VaccinationService {

    VaccinationRecordResponse createVaccinationPlan(Long profileId, CreateVaccinationRequest request);

    VaccinationRecordResponse administerDose(Long doseId, AdministerDoseRequest request);

    List<VaccinationRecordResponse> getVaccinationHistory(Long profileId);
}
