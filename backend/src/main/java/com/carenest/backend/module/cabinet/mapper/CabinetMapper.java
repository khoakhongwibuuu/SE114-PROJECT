package com.carenest.backend.module.cabinet.mapper;

import com.carenest.backend.module.cabinet.dto.response.CabinetMedicineResponse;
import com.carenest.backend.module.cabinet.dto.response.MedicineCabinetResponse;
import com.carenest.backend.module.cabinet.entity.CabinetMedicine;
import com.carenest.backend.module.cabinet.entity.MedicineCabinet;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.time.LocalDate;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CabinetMapper {

    @Mapping(source = "family.id", target = "familyId")
    @Mapping(target = "medicines", ignore = true)
    MedicineCabinetResponse toCabinetResponse(MedicineCabinet cabinet);

    @Mapping(target = "isExpired", expression = "java(medicine.getExpiryDate() != null && medicine.getExpiryDate().isBefore(java.time.LocalDate.now()))")
    @Mapping(target = "isLowStock", expression = "java(medicine.getQuantity() != null && medicine.getQuantity() <= 5)")
    CabinetMedicineResponse toMedicineResponse(CabinetMedicine medicine);
}
