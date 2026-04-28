package com.carenest.backend.module.cabinet.service;

import com.carenest.backend.module.cabinet.dto.request.CabinetCreateRequest;
import com.carenest.backend.module.cabinet.dto.request.CabinetMedicineRequest;
import com.carenest.backend.module.cabinet.dto.request.CabinetMedicineUpdateRequest;
import com.carenest.backend.module.cabinet.dto.response.CabinetMedicineResponse;
import com.carenest.backend.module.cabinet.dto.response.MedicineCabinetResponse;

import java.util.List;

public interface MedicineCabinetService {
    MedicineCabinetResponse createCabinet(CabinetCreateRequest request);
    MedicineCabinetResponse getFamilyCabinet(Long familyId);
    CabinetMedicineResponse addMedicine(Long cabinetId, CabinetMedicineRequest request);
    CabinetMedicineResponse updateMedicine(Long cabinetId, Long medicineId, CabinetMedicineUpdateRequest request);
    void removeMedicine(Long cabinetId, Long medicineId);
    List<CabinetMedicineResponse> getMedicines(Long cabinetId, String status);
}
