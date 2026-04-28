package com.carenest.backend.module.growth.mapper;

import com.carenest.backend.module.growth.dto.response.GrowthRecordResponse;
import com.carenest.backend.module.growth.entity.GrowthRecord;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface GrowthMapper {

    // Note: weightPercentile, heightPercentile, and isAnomalous are not in the entity
    // They will be set by the service after mapping
    @Mapping(target = "weightPercentile", ignore = true)
    @Mapping(target = "heightPercentile", ignore = true)
    @Mapping(target = "isAnomalous", ignore = true)
    GrowthRecordResponse toResponse(GrowthRecord entity);
}
