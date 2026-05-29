package com.carenest.backend.features.medication.mapper;

import com.carenest.backend.features.medication.dto.request.CreateMedicationRequest;
import com.carenest.backend.features.medication.dto.response.MedicationLogResponse;
import com.carenest.backend.features.medication.dto.response.MedicationResponse;
import com.carenest.backend.features.medication.entity.Medication;
import com.carenest.backend.features.medication.entity.MedicationLog;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, builder = @org.mapstruct.Builder(disableBuilder = true))
public interface MedicationMapper {

    @Mapping(source = "healthProfile.id", target = "healthProfileId")
    @Mapping(source = "timeSlots", target = "timeSlots", qualifiedByName = "stringToList")
    MedicationResponse toMedicationResponse(Medication medication);

    @Mapping(source = "medication.id", target = "medicationId")
    @Mapping(source = "medication.medicineName", target = "medicineName")
    @Mapping(source = "medication.dosage", target = "dosage")
    MedicationLogResponse toMedicationLogResponse(MedicationLog medicationLog);

    @Mapping(target = "healthProfile", ignore = true)
    @Mapping(source = "timeSlots", target = "timeSlots", qualifiedByName = "listToString")
    Medication toEntity(CreateMedicationRequest request);

    @Named("stringToList")
    default List<String> stringToList(String timeSlotsStr) {
        if (timeSlotsStr == null || timeSlotsStr.trim().isEmpty()) {
            return null;
        }
        return Arrays.stream(timeSlotsStr.split(","))
                .map(String::trim)
                .collect(Collectors.toList());
    }

    @Named("listToString")
    default String listToString(List<String> timeSlots) {
        if (timeSlots == null || timeSlots.isEmpty()) {
            return null;
        }
        return String.join(",", timeSlots);
    }
}
