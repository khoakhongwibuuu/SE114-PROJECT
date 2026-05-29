package com.carenest.backend.features.cabinet.service.impl;

import com.carenest.backend.core.exception.ResourceNotFoundException;
import com.carenest.backend.features.cabinet.dto.request.CabinetCreateRequest;
import com.carenest.backend.features.cabinet.dto.request.CabinetMedicineRequest;
import com.carenest.backend.features.cabinet.dto.request.CabinetMedicineUpdateRequest;
import com.carenest.backend.features.cabinet.dto.response.CabinetMedicineResponse;
import com.carenest.backend.features.cabinet.dto.response.MedicineCabinetResponse;
import com.carenest.backend.features.cabinet.entity.CabinetMedicine;
import com.carenest.backend.features.cabinet.entity.MedicineCabinet;
import com.carenest.backend.features.cabinet.mapper.CabinetMapper;
import com.carenest.backend.features.cabinet.repository.CabinetMedicineRepository;
import com.carenest.backend.features.cabinet.repository.MedicineCabinetRepository;
import com.carenest.backend.features.cabinet.service.MedicineCabinetService;
import com.carenest.backend.features.family.entity.Family;
import com.carenest.backend.features.family.repository.FamilyRepository;
import com.carenest.backend.features.family.util.FamilySecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MedicineCabinetServiceImpl implements MedicineCabinetService {

    private final MedicineCabinetRepository cabinetRepository;
    private final CabinetMedicineRepository medicineRepository;
    private final FamilyRepository familyRepository;
    private final CabinetMapper cabinetMapper;
    private final FamilySecurityUtil familySecurityUtil;

    @Override
    @Transactional
    public MedicineCabinetResponse createCabinet(CabinetCreateRequest request) {
        familySecurityUtil.checkUserBelongsToFamily(request.getFamilyId());

        Family family = familyRepository.findById(request.getFamilyId())
                .orElseThrow(() -> new ResourceNotFoundException("Family", "id", request.getFamilyId().toString()));

        // Check if cabinet already exists for family
        Optional<MedicineCabinet> existing = cabinetRepository.findByFamilyId(request.getFamilyId());
        if (existing.isPresent()) {
            throw new IllegalArgumentException("Gia Ä‘Ã¬nh nÃ y Ä‘Ã£ cÃ³ tá»§ thuá»‘c");
        }

        MedicineCabinet cabinet = MedicineCabinet.builder()
                .family(family)
                .name(request.getName() != null ? request.getName() : "Tá»§ thuá»‘c gia Ä‘Ã¬nh")
                .build();

        MedicineCabinet saved = cabinetRepository.save(cabinet);
        return cabinetMapper.toCabinetResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public MedicineCabinetResponse getFamilyCabinet(Long familyId) {
        familySecurityUtil.checkUserBelongsToFamily(familyId);

        if (!familyRepository.existsById(familyId)) {
            throw new ResourceNotFoundException("Family", "id", familyId.toString());
        }

        MedicineCabinet cabinet = cabinetRepository.findByFamilyId(familyId)
                .orElseThrow(() -> new ResourceNotFoundException("MedicineCabinet", "familyId", familyId.toString()));

        MedicineCabinetResponse response = cabinetMapper.toCabinetResponse(cabinet);

        List<CabinetMedicine> medicines = medicineRepository.findByCabinetId(cabinet.getId());
        response.setMedicines(medicines.stream().map(cabinetMapper::toMedicineResponse).collect(Collectors.toList()));

        return response;
    }

    @Override
    @Transactional
    public CabinetMedicineResponse addMedicine(Long cabinetId, CabinetMedicineRequest request) {
        MedicineCabinet cabinet = cabinetRepository.findById(cabinetId)
                .orElseThrow(() -> new ResourceNotFoundException("MedicineCabinet", "id", cabinetId.toString()));
        assertCabinetAccess(cabinet);

        // Check if medicine with same name already exists in cabinet
        Optional<CabinetMedicine> existing = medicineRepository.findByCabinetIdAndMedicineNameIgnoreCase(cabinetId, request.getMedicineName());

        CabinetMedicine medicine;
        if (existing.isPresent()) {
            medicine = existing.get();
            medicine.setQuantity(medicine.getQuantity() + request.getQuantity());
            if (request.getUnit() != null) medicine.setUnit(request.getUnit());
            if (request.getExpiryDate() != null) medicine.setExpiryDate(request.getExpiryDate());
            if (request.getNotes() != null) medicine.setNotes(request.getNotes());
        } else {
            medicine = CabinetMedicine.builder()
                    .cabinet(cabinet)
                    .medicineName(request.getMedicineName())
                    .quantity(request.getQuantity())
                    .unit(request.getUnit())
                    .expiryDate(request.getExpiryDate())
                    .notes(request.getNotes())
                    .build();
        }

        CabinetMedicine saved = medicineRepository.save(medicine);
        return cabinetMapper.toMedicineResponse(saved);
    }

    @Override
    @Transactional
    public CabinetMedicineResponse updateMedicine(Long cabinetId, Long medicineId, CabinetMedicineUpdateRequest request) {
        CabinetMedicine medicine = medicineRepository.findById(medicineId)
                .orElseThrow(() -> new ResourceNotFoundException("CabinetMedicine", "id", medicineId.toString()));
        assertCabinetAccess(medicine.getCabinet());

        if (!medicine.getCabinet().getId().equals(cabinetId)) {
            throw new IllegalArgumentException("Medicine does not belong to the specified cabinet");
        }

        if (request.getMedicineName() != null) medicine.setMedicineName(request.getMedicineName());
        if (request.getQuantity() != null) medicine.setQuantity(request.getQuantity());
        if (request.getUnit() != null) medicine.setUnit(request.getUnit());
        if (request.getExpiryDate() != null) medicine.setExpiryDate(request.getExpiryDate());
        if (request.getNotes() != null) medicine.setNotes(request.getNotes());

        CabinetMedicine updated = medicineRepository.save(medicine);
        return cabinetMapper.toMedicineResponse(updated);
    }

    @Override
    @Transactional
    public void removeMedicine(Long cabinetId, Long medicineId) {
        CabinetMedicine medicine = medicineRepository.findById(medicineId)
                .orElseThrow(() -> new ResourceNotFoundException("CabinetMedicine", "id", medicineId.toString()));
        assertCabinetAccess(medicine.getCabinet());

        if (!medicine.getCabinet().getId().equals(cabinetId)) {
            throw new IllegalArgumentException("Medicine does not belong to the specified cabinet");
        }

        medicineRepository.delete(medicine);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CabinetMedicineResponse> getMedicines(Long cabinetId, String status) {
        MedicineCabinet cabinet = cabinetRepository.findById(cabinetId)
                .orElseThrow(() -> new ResourceNotFoundException("MedicineCabinet", "id", cabinetId.toString()));
        assertCabinetAccess(cabinet);

        List<CabinetMedicine> medicines;
        if ("expired".equalsIgnoreCase(status)) {
            medicines = medicineRepository.findByCabinetIdAndExpiryDateBefore(cabinetId, LocalDate.now());
        } else if ("low-stock".equalsIgnoreCase(status)) {
            medicines = medicineRepository.findByCabinetIdAndQuantityLessThanEqual(cabinetId, 5);
        } else {
            medicines = medicineRepository.findByCabinetId(cabinetId);
        }

        return medicines.stream()
                .map(cabinetMapper::toMedicineResponse)
                .collect(Collectors.toList());
    }

    private void assertCabinetAccess(MedicineCabinet cabinet) {
        familySecurityUtil.checkUserBelongsToFamily(cabinet.getFamily().getId());
    }
}
