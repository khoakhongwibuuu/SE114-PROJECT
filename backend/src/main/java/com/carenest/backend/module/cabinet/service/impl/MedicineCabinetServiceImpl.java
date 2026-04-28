package com.carenest.backend.module.cabinet.service.impl;

import com.carenest.backend.common.exception.ResourceNotFoundException;
import com.carenest.backend.module.cabinet.dto.request.CabinetCreateRequest;
import com.carenest.backend.module.cabinet.dto.request.CabinetMedicineRequest;
import com.carenest.backend.module.cabinet.dto.request.CabinetMedicineUpdateRequest;
import com.carenest.backend.module.cabinet.dto.response.CabinetMedicineResponse;
import com.carenest.backend.module.cabinet.dto.response.MedicineCabinetResponse;
import com.carenest.backend.module.cabinet.entity.CabinetMedicine;
import com.carenest.backend.module.cabinet.entity.MedicineCabinet;
import com.carenest.backend.module.cabinet.mapper.CabinetMapper;
import com.carenest.backend.module.cabinet.repository.CabinetMedicineRepository;
import com.carenest.backend.module.cabinet.repository.MedicineCabinetRepository;
import com.carenest.backend.module.cabinet.service.MedicineCabinetService;
import com.carenest.backend.module.family.entity.Family;
import com.carenest.backend.module.family.repository.FamilyRepository;
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

    @Override
    @Transactional
    public MedicineCabinetResponse createCabinet(CabinetCreateRequest request) {
        Family family = familyRepository.findById(request.getFamilyId())
                .orElseThrow(() -> new ResourceNotFoundException("Family", "id", request.getFamilyId().toString()));

        // Check if cabinet already exists for family
        Optional<MedicineCabinet> existing = cabinetRepository.findByFamilyId(request.getFamilyId());
        if (existing.isPresent()) {
            throw new IllegalArgumentException("Family already has a medicine cabinet");
        }

        MedicineCabinet cabinet = MedicineCabinet.builder()
                .family(family)
                .name(request.getName() != null ? request.getName() : "Tủ thuốc gia đình")
                .build();

        MedicineCabinet saved = cabinetRepository.save(cabinet);
        return cabinetMapper.toCabinetResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public MedicineCabinetResponse getFamilyCabinet(Long familyId) {
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

        if (!medicine.getCabinet().getId().equals(cabinetId)) {
            throw new IllegalArgumentException("Medicine does not belong to the specified cabinet");
        }

        medicineRepository.delete(medicine);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CabinetMedicineResponse> getMedicines(Long cabinetId, String status) {
        if (!cabinetRepository.existsById(cabinetId)) {
            throw new ResourceNotFoundException("MedicineCabinet", "id", cabinetId.toString());
        }

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
}
