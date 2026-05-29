package com.carenest.backend.features.medication.service;

import com.carenest.backend.core.api.PageResponse;
import com.carenest.backend.features.medication.dto.request.CheckInMedicationRequest;
import com.carenest.backend.features.medication.dto.request.BatchCreateMedicationRequest;
import com.carenest.backend.features.medication.dto.request.CreateMedicationRequest;
import com.carenest.backend.features.medication.dto.request.UpdateMedicationRequest;
import com.carenest.backend.features.medication.dto.response.MedicationLogResponse;
import com.carenest.backend.features.medication.dto.response.MedicationResponse;

import java.util.List;

public interface MedicationService {

    MedicationResponse createMedication(Long profileId, CreateMedicationRequest request);

    MedicationResponse updateMedication(Long medicationId, UpdateMedicationRequest request);

    void completeMedication(Long medicationId);

    PageResponse<MedicationResponse> getMedicationsByProfile(Long profileId, org.springframework.data.domain.Pageable pageable);

    List<MedicationLogResponse> getMedicationsForToday(Long profileId);

    void checkInMedicationLog(Long logId, CheckInMedicationRequest request);

    void createBatchFromOcr(BatchCreateMedicationRequest request);

    void deleteMedication(Long id);
}
