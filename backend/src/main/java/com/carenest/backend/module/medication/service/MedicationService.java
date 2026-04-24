package com.carenest.backend.module.medication.service;

import com.carenest.backend.module.medication.dto.request.CheckInMedicationRequest;
import com.carenest.backend.module.medication.dto.request.BatchCreateMedicationRequest;
import com.carenest.backend.module.medication.dto.request.CreateMedicationRequest;
import com.carenest.backend.module.medication.dto.request.UpdateMedicationRequest;
import com.carenest.backend.module.medication.dto.response.MedicationLogResponse;
import com.carenest.backend.module.medication.dto.response.MedicationResponse;

import java.util.List;

public interface MedicationService {

    MedicationResponse createMedication(Long profileId, CreateMedicationRequest request);

    MedicationResponse updateMedication(Long medicationId, UpdateMedicationRequest request);

    void completeMedication(Long medicationId);

    List<MedicationResponse> getMedicationsByProfile(Long profileId);

    List<MedicationLogResponse> getMedicationsForToday(Long profileId);

    void checkInMedicationLog(Long logId, CheckInMedicationRequest request);

    void createBatchFromOcr(BatchCreateMedicationRequest request);
}
