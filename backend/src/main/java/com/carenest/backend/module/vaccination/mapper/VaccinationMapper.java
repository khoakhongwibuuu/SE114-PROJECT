package com.carenest.backend.module.vaccination.mapper;

import com.carenest.backend.module.vaccination.dto.request.CreateVaccinationRequest;
import com.carenest.backend.module.vaccination.dto.response.VaccinationDoseResponse;
import com.carenest.backend.module.vaccination.dto.response.VaccinationRecordResponse;
import com.carenest.backend.module.vaccination.entity.VaccinationDose;
import com.carenest.backend.module.vaccination.entity.VaccinationRecord;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, builder = @org.mapstruct.Builder(disableBuilder = true))
public interface VaccinationMapper {

    @Mapping(source = "healthProfile.id", target = "healthProfileId")
    VaccinationRecordResponse toRecordResponse(VaccinationRecord record);

    @Mapping(source = "record.healthProfile.id", target = "healthProfileId")
    @Mapping(source = "doses", target = "doses")
    VaccinationRecordResponse toRecordResponseWithDoses(VaccinationRecord record, List<VaccinationDose> doses);

    VaccinationDoseResponse toDoseResponse(VaccinationDose dose);

    @Mapping(target = "healthProfile", ignore = true)
    VaccinationRecord toEntity(CreateVaccinationRequest request);
}
