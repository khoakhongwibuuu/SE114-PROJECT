package com.carenest.backend.module.healthprofile.mapper;

import com.carenest.backend.module.healthprofile.dto.response.HealthProfileResponse;
import com.carenest.backend.module.healthprofile.entity.HealthProfile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface HealthProfileMapper {

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "family.id", target = "familyId")
    HealthProfileResponse toResponse(HealthProfile healthProfile);
}
